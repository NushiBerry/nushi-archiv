package com.nushi.archiv;

// Importa a interface que marca esta classe como inicializadora principal do mod.
import net.fabricmc.api.ModInitializer;

// Imports do sistema de log.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Declaração da classe principal do mod.
// "implements ModInitializer" significa que esta classe participa da inicialização do mod.
public class Archiv implements ModInitializer {

	// Constante com o ID interno do mod.
	public static final String MOD_ID = "archiv";

	// Logger reutilizável para escrever mensagens no console/log.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Este método é executado quando o mod é inicializado.

	@Override
	public void onInitialize() {

		LOGGER.info("Archiv initialized succesfully!");

	}
}