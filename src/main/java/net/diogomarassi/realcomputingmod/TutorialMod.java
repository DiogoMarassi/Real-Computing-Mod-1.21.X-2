package net.diogomarassi.realcomputingmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.diogomarassi.realcomputingmod.blocks.ModBlocks;
import net.diogomarassi.realcomputingmod.entity.ModBlockEntities;
import net.diogomarassi.realcomputingmod.item.ModItems;
import net.diogomarassi.realcomputingmod.networking.packet.OperationSyncC2SPacket;
import net.diogomarassi.realcomputingmod.screen.ModScreenHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialMod implements ModInitializer {
	public static final String MOD_ID = "realcomputingmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();

		// Registra o payload e receptor do pacote de rede
		PayloadTypeRegistry.playC2S().register(OperationSyncC2SPacket.ID, OperationSyncC2SPacket.CODEC);
		OperationSyncC2SPacket.register();
	}
}