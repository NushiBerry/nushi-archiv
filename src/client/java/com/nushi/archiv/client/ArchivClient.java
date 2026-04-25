package com.nushi.archiv.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nushi.archiv.Archiv;
import com.nushi.archiv.client.screen.ArchivBrowseScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

// Classe responsável pela inicialização do lado do cliente (é do Fabric API)
public class ArchivClient implements ClientModInitializer {

	//Categoria das teclas nas confis do Mine
	private static final KeyMapping.Category ARCHIV_CATEGORY = KeyMapping.Category.register(
			ResourceLocation.fromNamespaceAndPath(Archiv.MOD_ID, "main")
	);

	// Registro de teclas do Archiv
	private static final KeyMapping OPEN_ARCHIV_KEY = KeyBindingHelper.registerKeyBinding(
			new KeyMapping(
					"key.archiv.open_browser",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_B,
					ARCHIV_CATEGORY
			)
	);

	@Override
	public void onInitializeClient() {
		Archiv.LOGGER.info("Archiv client initialized successfully.");

		// A cada tick do cliente (do mine de quem tiver usando o MOD), verifica se a tecla foi apertada
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_ARCHIV_KEY.consumeClick()) {
				client.setScreen(new ArchivBrowseScreen(Component.literal("Archiv - Asset Browser"), client.screen));
			}
		});

	}
}