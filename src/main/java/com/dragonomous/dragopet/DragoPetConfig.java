package com.dragonomous.dragopet;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(DragoPetConfig.GROUP)
public interface DragoPetConfig extends Config
{
	String GROUP = "dragopet";

	@Range(min = 1, max = 4)
	@ConfigItem(
		keyName = "size",
		name = "Size",
		description = "How big Drago is (1 = 32px, 2 = 64px, ...)",
		position = 0
	)
	default int size()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "speechBubbles",
		name = "Speech bubbles",
		description = "Let Drago say something when he reacts",
		position = 1
	)
	default boolean speechBubbles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "levelUps",
		name = "Cheer on level-ups",
		description = "Drago parties when you gain a level",
		position = 2
	)
	default boolean levelUps()
	{
		return true;
	}

	@ConfigItem(
		keyName = "virtualLevels",
		name = "Virtual level-ups",
		description = "Also cheer for virtual levels past 99 (up to 126)",
		position = 3
	)
	default boolean virtualLevels()
	{
		return true;
	}

	@ConfigItem(
		keyName = "drops",
		name = "Celebrate drops",
		description = "Drago gets excited about valuable drops, new collection log slots and pets",
		position = 4
	)
	default boolean drops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "deaths",
		name = "Cry on death",
		description = "Drago cries when you die",
		position = 5
	)
	default boolean deaths()
	{
		return true;
	}

	@ConfigItem(
		keyName = "lowHp",
		name = "Sweat on low HP",
		description = "Drago gets nervous when your hitpoints are low",
		position = 6
	)
	default boolean lowHp()
	{
		return true;
	}

	@Range(min = 1, max = 99)
	@ConfigItem(
		keyName = "lowHpPercent",
		name = "Low HP threshold (%)",
		description = "Below this percentage of your max hitpoints Drago starts sweating",
		position = 7
	)
	default int lowHpPercent()
	{
		return 25;
	}

	@ConfigItem(
		keyName = "sleep",
		name = "Fall asleep when idle",
		description = "Drago naps when you stand still for a minute",
		position = 8
	)
	default boolean sleep()
	{
		return true;
	}
}
