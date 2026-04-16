package com.seccad_elizade.energytoemc.network;

import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPipeMode {
    private final BlockPos pos;

    public PacketPipeMode(BlockPos pos) {
        this.pos = pos;
    }

    // Encoder: Writing data to the buffer
    public static void encode(PacketPipeMode msg, PacketBuffer buffer) {
        buffer.writeBlockPos(msg.pos);
    }

    // Decoder: Reading data from the buffer
    public static PacketPipeMode decode(PacketBuffer buffer) {
        return new PacketPipeMode(buffer.readBlockPos());
    }

    // Handler: The logic that runs when the packet is received
    public static void handle(PacketPipeMode msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // In 1.16.5, the context gives us the sender (the player)
            World world = ctx.get().getSender().level;
            if (world.isLoaded(msg.pos)) {
                if (world.getBlockEntity(msg.pos) instanceof EmcPipeBlockEntity) {
                    ((EmcPipeBlockEntity) world.getBlockEntity(msg.pos)).toggleMode();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}