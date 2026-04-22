package com.nushi.archiv.client;

import com.nushi.archiv.Archiv;
import net.fabricmc.api.ClientModInitializer;

public class ArchivClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Archiv.LOGGER.info("Archiv client initialized succesfully!");
	}
}