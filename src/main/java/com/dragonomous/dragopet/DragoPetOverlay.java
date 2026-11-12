package com.dragonomous.dragopet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

class DragoPetOverlay extends Overlay
{
	private static final int FRAME = 32;
	private static final int BUBBLE_HEIGHT = 18;
	private static final int MIN_WIDTH = 96;
	private static final Color BUBBLE_FILL = new Color(255, 255, 255, 235);
	private static final Color BUBBLE_EDGE = new Color(20, 16, 22);

	private final DragoPetPlugin plugin;
	private final DragoPetConfig config;
	private final Map<DragoMood, BufferedImage[]> frames = new EnumMap<>(DragoMood.class);

	@Inject
	DragoPetOverlay(DragoPetPlugin plugin, DragoPetConfig config)
	{
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);

		for (DragoMood mood : DragoMood.values())
		{
			BufferedImage strip = ImageUtil.loadImageResource(DragoPetPlugin.class, mood.getFile());
			int count = Math.max(1, strip.getWidth() / FRAME);
			BufferedImage[] list = new BufferedImage[count];
			for (int i = 0; i < count; i++)
			{
				list[i] = strip.getSubimage(i * FRAME, 0, FRAME, FRAME);
			}
			frames.put(mood, list);
		}
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		DragoMood mood = plugin.getMood();
		BufferedImage[] list = frames.get(mood);
		if (list == null)
		{
			return null;
		}

		int index = (int) ((System.currentTimeMillis() / mood.getFrameMillis()) % list.length);
		int size = FRAME * Math.max(1, Math.min(4, config.size()));
		boolean bubbles = config.speechBubbles();
		int width = bubbles ? Math.max(size, MIN_WIDTH) : size;
		int top = bubbles ? BUBBLE_HEIGHT : 0;

		String speech = plugin.getSpeech();
		if (bubbles && speech != null && !speech.isEmpty())
		{
			drawBubble(graphics, speech, width);
		}

		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		graphics.drawImage(list[index], (width - size) / 2, top, size, size, null);

		return new Dimension(width, top + size);
	}

	private void drawBubble(Graphics2D graphics, String text, int width)
	{
		Font font = FontManager.getRunescapeSmallFont();
		graphics.setFont(font);
		FontMetrics metrics = graphics.getFontMetrics();
		int textWidth = metrics.stringWidth(text);
		int bubbleWidth = Math.min(width, textWidth + 10);
		int x = (width - bubbleWidth) / 2;
		int height = BUBBLE_HEIGHT - 4;

		graphics.setColor(BUBBLE_FILL);
		graphics.fillRoundRect(x, 0, bubbleWidth, height, 8, 8);
		graphics.setColor(BUBBLE_EDGE);
		graphics.setStroke(new BasicStroke(1));
		graphics.drawRoundRect(x, 0, bubbleWidth - 1, height - 1, 8, 8);

		// small tail pointing down at Drago
		int cx = width / 2;
		graphics.setColor(BUBBLE_FILL);
		graphics.fillPolygon(new int[]{cx - 3, cx + 3, cx}, new int[]{height - 1, height - 1, height + 3}, 3);
		graphics.setColor(BUBBLE_EDGE);
		graphics.drawLine(cx - 3, height - 1, cx, height + 3);
		graphics.drawLine(cx + 3, height - 1, cx, height + 3);

		int textY = (height - metrics.getHeight()) / 2 + metrics.getAscent();
		graphics.drawString(text, x + (bubbleWidth - textWidth) / 2, textY);
	}
}
