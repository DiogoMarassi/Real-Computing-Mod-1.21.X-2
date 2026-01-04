package net.diogomarassi.realcomputingmod.networking.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.diogomarassi.realcomputingmod.TutorialMod;
import net.diogomarassi.realcomputingmod.entity.MagicBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OperationSyncC2SPacket(BlockPos pos, int operation) implements CustomPayload {
    public static final CustomPayload.Id<OperationSyncC2SPacket> ID =
        new CustomPayload.Id<>(Identifier.of(TutorialMod.MOD_ID, "operation_sync"));

    public static final PacketCodec<RegistryByteBuf, OperationSyncC2SPacket> CODEC =
        PacketCodec.tuple(
            BlockPos.PACKET_CODEC, OperationSyncC2SPacket::pos,
            PacketCodecs.VAR_INT, OperationSyncC2SPacket::operation,
            OperationSyncC2SPacket::new
        );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            TutorialMod.LOGGER.info("SERVIDOR: Pacote recebido! Pos: {}, Operation: {}", payload.pos(), payload.operation());

            context.server().execute(() -> {
                TutorialMod.LOGGER.info("SERVIDOR: Executando no thread do servidor...");

                if (context.player().getWorld().getBlockEntity(payload.pos()) instanceof MagicBlockEntity magicBlockEntity) {
                    TutorialMod.LOGGER.info("SERVIDOR: MagicBlockEntity encontrada! Operação atual: {}", magicBlockEntity.getOperation());
                    magicBlockEntity.setOperation(payload.operation());
                    TutorialMod.LOGGER.info("SERVIDOR: Operação atualizada para: {}", magicBlockEntity.getOperation());
                } else {
                    TutorialMod.LOGGER.error("SERVIDOR: BlockEntity NÃO encontrada em {}!", payload.pos());
                }
            });
        });
    }
}
