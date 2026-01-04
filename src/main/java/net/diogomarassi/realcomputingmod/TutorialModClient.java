package net.diogomarassi.realcomputingmod;

import net.fabricmc.api.ClientModInitializer;
import net.diogomarassi.realcomputingmod.screen.MagicBlockScreen;
import net.diogomarassi.realcomputingmod.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class TutorialModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // A linha que estava errada lá, vem para cá:
        HandledScreens.register(ModScreenHandlers.MAGIC_BLOCK_SCREEN_HANDLER, MagicBlockScreen::new);
    }
}
