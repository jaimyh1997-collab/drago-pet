package com.dragonomous.dragopet;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DragoPetPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DragoPetPlugin.class);
		RuneLite.main(args);
	}
}
