package com.nushi.archiv.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nushi.archiv.Archiv;
import com.nushi.archiv.client.screen.ArchivHomeScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ArchivClient implements ClientModInitializer {
	private static final KeyMapping.Category ARCHIV_CATEGORY = KeyMapping.Category.register(
			ResourceLocation.fromNamespaceAndPath(Archiv.MOD_ID, "main")
	);

	private static final KeyMapping OPEN_ARCHIV_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.archiv.open_browser",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_K,
					ARCHIV_CATEGORY
			)
	);

	@Override
	public void onInitializeClient() {
		Archiv.LOGGER.info("Archiv client initialized successfully.");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_ARCHIV_KEY.consumeClick()) {
				client.setScreen(new ArchivHomeScreen(Component.literal("Archiv"), client.screen));
			}
		});
	}
}