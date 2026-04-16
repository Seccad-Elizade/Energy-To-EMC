package com.seccad_elizade.energytoemc.event;

import com.seccad_elizade.energytoemc.EnergyToEmc;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = EnergyToEmc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityRegistration {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        EnergyToEmc.LOGGER.info("EnergyToEMC: Initializing Compatibility Setup...");
    }
}