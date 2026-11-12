package com.dragonomous.dragopet;

import com.google.inject.Provides;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Drago Pet",
	description = "A tiny pixel dragon companion that reacts to your level-ups, drops, deaths and low HP",
	tags = {"pet", "companion", "dragon", "fun", "overlay", "dragonomous"}
)
public class DragoPetPlugin extends Plugin
{
	/** Game ticks Drago stays in a reaction mood (1 tick = 0.6s). */
	private static final int LEVEL_UP_TICKS = 8;
	private static final int DROP_TICKS = 10;
	private static final int DEATH_TICKS = 10;
	/** Standing still this long (100 ticks = 60s) sends Drago to sleep. */
	private static final int SLEEP_AFTER_TICKS = 100;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DragoPetOverlay overlay;

	@Inject
	private DragoPetConfig config;

	private final Map<Skill, Integer> knownLevels = new EnumMap<>(Skill.class);

	private DragoMood mood = DragoMood.IDLE;
	private String speech;
	private int reactionTicksLeft;
	private int stillTicks;
	private WorldPoint lastLocation;

	@Provides
	DragoPetConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DragoPetConfig.class);
	}

	@Override
	protected void startUp()
	{
		knownLevels.clear();
		setIdle();
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		knownLevels.clear();
	}

	DragoMood getMood()
	{
		return mood;
	}

	String getSpeech()
	{
		return speech;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.LOGGING_IN)
		{
			// Levels are re-sent on login; forget them so that isn't mistaken for a level-up.
			knownLevels.clear();
			setIdle();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		Skill skill = event.getSkill();
		int level = event.getLevel();
		Integer previous = knownLevels.put(skill, level);
		if (previous != null && level > previous && config.levelUps())
		{
			react(DragoMood.LEVEL_UP, LEVEL_UP_TICKS, "gz! " + skill.getName() + " " + level);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (config.deaths() && event.getActor() == client.getLocalPlayer())
		{
			react(DragoMood.DEATH, DEATH_TICKS, "sit...");
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!config.drops())
		{
			return;
		}
		ChatMessageType type = event.getType();
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());
		if (message.startsWith("You have a funny feeling like")
			|| message.startsWith("You feel something weird sneaking into your backpack"))
		{
			react(DragoMood.DROP, DROP_TICKS, "PET!!");
		}
		else if (message.startsWith("New item added to your collection log"))
		{
			react(DragoMood.DROP, DROP_TICKS, "new slot!");
		}
		else if (message.startsWith("Valuable drop"))
		{
			react(DragoMood.DROP, DROP_TICKS, "PURPLE?!");
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (reactionTicksLeft > 0)
		{
			reactionTicksLeft--;
			if (reactionTicksLeft == 0)
			{
				setIdle();
			}
			return;
		}

		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}

		// Low HP has priority over idling and sleeping.
		if (config.lowHp() && isLowHp())
		{
			if (mood != DragoMood.LOW_HP)
			{
				mood = DragoMood.LOW_HP;
				speech = config.speechBubbles() ? "uh oh" : null;
			}
			stillTicks = 0;
			return;
		}
		if (mood == DragoMood.LOW_HP)
		{
			setIdle();
		}

		WorldPoint location = player.getWorldLocation();
		boolean standingStill = location != null && location.equals(lastLocation) && player.getAnimation() == -1;
		lastLocation = location;

		if (!standingStill)
		{
			stillTicks = 0;
			if (mood == DragoMood.SLEEP)
			{
				setIdle();
			}
			return;
		}

		stillTicks++;
		if (config.sleep() && stillTicks >= SLEEP_AFTER_TICKS && mood != DragoMood.SLEEP)
		{
			mood = DragoMood.SLEEP;
			speech = null;
		}
	}

	private boolean isLowHp()
	{
		int current = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int max = client.getRealSkillLevel(Skill.HITPOINTS);
		return max > 0 && current > 0 && current * 100 < max * config.lowHpPercent();
	}

	private void react(DragoMood newMood, int ticks, String text)
	{
		mood = newMood;
		reactionTicksLeft = ticks;
		stillTicks = 0;
		speech = config.speechBubbles() ? text : null;
	}

	private void setIdle()
	{
		mood = DragoMood.IDLE;
		speech = null;
		reactionTicksLeft = 0;
		stillTicks = 0;
	}
}
