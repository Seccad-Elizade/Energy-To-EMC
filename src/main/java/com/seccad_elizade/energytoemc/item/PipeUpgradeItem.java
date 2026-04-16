package com.seccad_elizade.energytoemc.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class PipeUpgradeItem extends Item {
    private final int tier;
    private final long transferRatePerSecond;

    public PipeUpgradeItem(Properties properties, int tier, long transferRatePerSecond) {
        super(properties.stacksTo(1));
        this.tier = tier;
        this.transferRatePerSecond = transferRatePerSecond;
    }

    public long getTransferPerTick() {
        return Math.max(1, this.transferRatePerSecond / 20);
    }

    public int getTier() {
        return tier;
    }

    public int getParticleMultiplier() {
        return (this.tier + 1) * 2;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        TextFormatting tierColor;
        // Replaced switch expression with standard switch for Java 8 compatibility
        switch (tier) {
            case 0: tierColor = TextFormatting.WHITE; break;
            case 1: tierColor = TextFormatting.GREEN; break;
            case 2: tierColor = TextFormatting.AQUA; break;
            default: tierColor = TextFormatting.LIGHT_PURPLE; break;
        }

        // Tier Line
        tooltip.add(new StringTextComponent("Pipe Tier: ").withStyle(TextFormatting.GRAY)
                .append(new StringTextComponent(String.valueOf(tier + 1)).withStyle(tierColor, TextFormatting.BOLD)));

        tooltip.add(StringTextComponent.EMPTY);

        // Rate Lines
        tooltip.add(new StringTextComponent("Max Transfer Rate:").withStyle(TextFormatting.GOLD));
        tooltip.add(new StringTextComponent(" " + String.format("%,d", transferRatePerSecond) + " EMC/sec")
                .withStyle(TextFormatting.WHITE));

        tooltip.add(new StringTextComponent(" (" + String.format("%,d", getTransferPerTick()) + " EMC/tick)")
                .withStyle(TextFormatting.DARK_GRAY));

        tooltip.add(StringTextComponent.EMPTY);

        // Warning Line
        tooltip.add(new StringTextComponent("⚠ Only works in EXTRACT mode")
                .withStyle(TextFormatting.DARK_AQUA, TextFormatting.ITALIC));
    }
}