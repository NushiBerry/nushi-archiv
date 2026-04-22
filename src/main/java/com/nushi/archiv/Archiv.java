package com.nushi.archiv;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Archiv implements ModInitializer {
	public static final String MOD_ID = "archiv";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		LOGGER.info("Archiv initialized succesfully!");

	}
}