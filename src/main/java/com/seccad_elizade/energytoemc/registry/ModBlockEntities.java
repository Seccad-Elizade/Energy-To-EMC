package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.block.entity.EmcCapacitorBlockEntity;
import com.seccad_elizade.energytoemc.block.entity.EmcConverterBlockEntity;
import com.seccad_elizade.energytoemc.block.entity.EmcPipeBlockEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockEntities {
    // Fixed: Changed MODID to MOD_ID to match your main class
    public static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, EnergyToEmc.MOD_ID);

    public static final RegistryObject<TileEntityType<EmcConverterBlockEntity>> EMC_CONVERTER_BE =
            BLOCK_ENTITIES.register("emc_converter", () ->
                    TileEntityType.Builder.of(EmcConverterBlockEntity::new,
                            ModBlocks.EMC_CONVERTER.get()).build(null));

    public static final RegistryObject<TileEntityType<EmcPipeBlockEntity>> EMC_PIPE_BE =
            BLOCK_ENTITIES.register("emc_pipe", () ->
                    TileEntityType.Builder.of(EmcPipeBlockEntity::new,
                            ModBlocks.EMC_PIPE.get()).build(null));

    public static final RegistryObject<TileEntityType<EmcCapacitorBlockEntity>> EMC_CAPACITOR_BE =
            BLOCK_ENTITIES.register("emc_capacitor", () ->
                    TileEntityType.Builder.of(EmcCapacitorBlockEntity::new,
                            ModBlocks.BASIC_CAPACITOR.get(),
                            ModBlocks.ADVANCED_CAPACITOR.get(),
                            ModBlocks.ELITE_CAPACITOR.get(),
                            ModBlocks.ULTIMATE_CAPACITOR.get(),
                            ModBlocks.CREATIVE_CAPACITOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}