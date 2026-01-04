package net.diogomarassi.realcomputingmod.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.networking.packet.OperationSyncC2SPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MagicBlockScreen extends HandledScreen<MagicBlockScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(TutorialMod.MOD_ID, "textures/gui/magic_block_gui.png");

    // Coordenadas dos 4 botões (grade 2x2)
    private static final int[][] BUTTON_POSITIONS = {
        {25, 19},  // NOT (linha 0, coluna 0)
        {25, 34},  // AND (linha 1, coluna 0)
        {96, 19},  // OR  (linha 0, coluna 1)
        {96, 34}   // XOR (linha 1, coluna 1)
    };
    private static final int BUTTON_WIDTH = 56;
    private static final int BUTTON_HEIGHT = 15;

    // Coordenadas da textura de destaque (na imagem png)
    private static final int HIGHLIGHT_U = 0;
    private static final int HIGHLIGHT_V = 151;

    public MagicBlockScreen(MagicBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 175;
        this.backgroundHeight = 150;

        // Ajusta a posição do título "Inventory" para não sobrepor os slots
        // Coloca acima dos slots do inventário (que começam em Y=68)
        this.playerInventoryTitleY = 56;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // 1. Desenha o fundo da interface (175x150)
        // Especifica o tamanho da textura PNG (256x204) para evitar esticamento
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, 256, 204);

        // 2. Desenha o destaque escuro na opção selecionada
        int currentOp = handler.getOperation(); // 0=NOT, 1=AND, 2=OR, 3=XOR
        if (currentOp >= 0 && currentOp < 4) {
            int btnX = x + BUTTON_POSITIONS[currentOp][0];
            int btnY = y + BUTTON_POSITIONS[currentOp][1];

            // Desenha a textura de destaque escuro (56x15, vindo de 0x151)
            context.drawTexture(TEXTURE, btnX, btnY, HIGHLIGHT_U, HIGHLIGHT_V, BUTTON_WIDTH, BUTTON_HEIGHT, 256, 204);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        // Desenha os textos dos botões
        drawButtonLabels(context);
    }

    private void drawButtonLabels(DrawContext context) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        String[] labels = {"NOT", "AND", "OR", "XOR"};

        for (int i = 0; i < 4; i++) {
            int btnX = x + BUTTON_POSITIONS[i][0];
            int btnY = y + BUTTON_POSITIONS[i][1];

            // Centraliza o texto dentro do retângulo 56x15
            int textWidth = textRenderer.getWidth(labels[i]);
            int textX = btnX + (BUTTON_WIDTH - textWidth) / 2;
            int textY = btnY + (BUTTON_HEIGHT - 8) / 2;

            context.drawText(textRenderer, labels[i], textX, textY, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Clique esquerdo
            int x = (width - backgroundWidth) / 2;
            int y = (height - backgroundHeight) / 2;

            for (int i = 0; i < 4; i++) {
                double btnX = x + BUTTON_POSITIONS[i][0];
                double btnY = y + BUTTON_POSITIONS[i][1];

                // Verifica se o clique foi dentro da área do botão
                if (mouseX >= btnX && mouseX < btnX + BUTTON_WIDTH &&
                        mouseY >= btnY && mouseY < btnY + BUTTON_HEIGHT) {

                    TutorialMod.LOGGER.info("CLIENTE: Clicou no botão {} (operação {})", new String[]{"NOT", "AND", "OR", "XOR"}[i], i);
                    TutorialMod.LOGGER.info("CLIENTE: Operação atual antes: {}", handler.getOperation());
                    TutorialMod.LOGGER.info("CLIENTE: BlockPos: {}", handler.getPos());

                    // Envia pacote para o servidor sincronizar a operação selecionada
                    ClientPlayNetworking.send(new OperationSyncC2SPacket(handler.getPos(), i));
                    TutorialMod.LOGGER.info("CLIENTE: Pacote enviado!");

                    // Toca som de clique
                    client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}