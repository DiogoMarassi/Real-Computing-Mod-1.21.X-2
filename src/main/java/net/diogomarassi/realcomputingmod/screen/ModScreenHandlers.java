package net.diogomarassi.realcomputingmod.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {

    // Usa ExtendedScreenHandlerType para enviar BlockPos para o cliente
    public static final ScreenHandlerType<MagicBlockScreenHandler> MAGIC_BLOCK_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(TutorialMod.MOD_ID, "magic_block"),
                    new ExtendedScreenHandlerType<>(MagicBlockScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        TutorialMod.LOGGER.info("Registering Screen Handlers for " + TutorialMod.MOD_ID);
    }
}