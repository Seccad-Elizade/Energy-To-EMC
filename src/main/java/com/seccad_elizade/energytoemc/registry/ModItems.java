package com.seccad_elizade.energytoemc.registry;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.item.ConversionUpgradeItem;
import com.seccad_elizade.energytoemc.item.PipeUpgradeItem;
import com.seccad_elizade.energytoemc.item.WrenchItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EnergyToEmc.MOD_ID);

    // --- Resources ---
    public static final RegistryObject<Item> COPPER_INGOT = ITEMS.register("copper_ingot",
            () -> new Item(new Item.Properties().tab(EnergyToEmc.TAB)));

    // --- Block Items ---
    public static final RegistryObject<Item> EMC_CONVERTER_ITEM = ITEMS.register("emc_converter",
            () -> new BlockItem(ModBlocks.EMC_CONVERTER.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> EMC_PIPE_ITEM = ITEMS.register("emc_pipe",
            () -> new BlockItem(ModBlocks.EMC_PIPE.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> BASIC_CAPACITOR = ITEMS.register("basic_emc_capacitor",
            () -> new BlockItem(ModBlocks.BASIC_CAPACITOR.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> HARDENED_CAPACITOR = ITEMS.register("hardened_emc_capacitor",
            () -> new BlockItem(ModBlocks.ADVANCED_CAPACITOR.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> REINFORCED_CAPACITOR = ITEMS.register("reinforced_emc_capacitor",
            () -> new BlockItem(ModBlocks.ELITE_CAPACITOR.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> ATOMIC_CAPACITOR = ITEMS.register("atomic_emc_capacitor",
            () -> new BlockItem(ModBlocks.ULTIMATE_CAPACITOR.get(), new Item.Properties().tab(EnergyToEmc.TAB)));

    public static final RegistryObject<Item> SINGULARITY_CAPACITOR = ITEMS.register("singularity_emc_capacitor",
            () -> new BlockItem(ModBlocks.CREATIVE_CAPACITOR.get(), new Item.Properties().tab(EnergyToEmc.TAB).rarity(Rarity.EPIC)));

    // --- Functional Items ---
    public static final RegistryObject<WrenchItem> WRENCH = ITEMS.register("wrench",
            () -> new WrenchItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).tab(EnergyToEmc.TAB)));

    // --- Pipe Upgrades ---
    public static final RegistryObject<PipeUpgradeItem> PIPE_UPGRADE_1 = ITEMS.register("pipe_upgrade_1",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 0, 360));
    public static final RegistryObject<PipeUpgradeItem> PIPE_UPGRADE_2 = ITEMS.register("pipe_upgrade_2",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 1, 1200));
    public static final RegistryObject<PipeUpgradeItem> PIPE_UPGRADE_3 = ITEMS.register("pipe_upgrade_3",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 2, 4000));
    public static final RegistryObject<PipeUpgradeItem> PIPE_UPGRADE_4 = ITEMS.register("pipe_upgrade_4",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 3, 20000));
    public static final RegistryObject<PipeUpgradeItem> PIPE_UPGRADE_5 = ITEMS.register("pipe_upgrade_5",
            () -> new PipeUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 4, 100000));

    // --- Conversion Upgrades ---
    public static final RegistryObject<ConversionUpgradeItem> UPGRADE_TIER_1 = ITEMS.register("upgrade_tier_1",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 3));
    public static final RegistryObject<ConversionUpgradeItem> UPGRADE_TIER_2 = ITEMS.register("upgrade_tier_2",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 6));
    public static final RegistryObject<ConversionUpgradeItem> UPGRADE_TIER_3 = ITEMS.register("upgrade_tier_3",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 9));
    public static final RegistryObject<ConversionUpgradeItem> UPGRADE_TIER_4 = ITEMS.register("upgrade_tier_4",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 12));
    public static final RegistryObject<ConversionUpgradeItem> UPGRADE_TIER_5 = ITEMS.register("upgrade_tier_5",
            () -> new ConversionUpgradeItem(new Item.Properties().stacksTo(1).tab(EnergyToEmc.TAB), 15));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}