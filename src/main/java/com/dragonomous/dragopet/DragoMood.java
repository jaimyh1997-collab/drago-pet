package com.dragonomous.dragopet;

/**
 * Every mood Drago can be in, with its sprite strip (32x32 frames side by side)
 * and how long each frame is shown.
 */
public enum DragoMood
{
	IDLE("idle.png", 350),
	LEVEL_UP("gz.png", 150),
	DEATH("cry.png", 150),
	DROP("purple.png", 120),
	LOW_HP("cope.png", 250),
	SLEEP("sleep.png", 250);

	private final String file;
	private final int frameMillis;

	DragoMood(String file, int frameMillis)
	{
		this.file = file;
		this.frameMillis = frameMillis;
	}

	public String getFile()
	{
		return file;
	}

	public int getFrameMillis()
	{
		return frameMillis;
	}
}
