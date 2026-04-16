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

public class ConversionUpgradeItem extends Item {
    private final int profitMultiplier;

    public ConversionUpgradeItem(Properties properties, int profitMultiplier) {
        super(properties.stacksTo(1));
        this.profitMultiplier = profitMultiplier;
    }

    public int getProfitMultiplier() {
        return profitMultiplier;
    }

    public int getTier() {
        switch (this.profitMultiplier) {
            case 3: return 1;
            case 6: return 2;
            case 9: return 3;
            case 12: return 4;
            case 15: return 5;
            default: return 0;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        int tier = getTier();

        tooltip.add(new TranslationTextComponent("tooltip.energytoemc.upgrade_tier_" + tier)
                .withStyle(TextFormatting.GRAY));

        tooltip.add(new StringTextComponent("Profit: ").withStyle(TextFormatting.GRAY)
                .append(new StringTextComponent(profitMultiplier + "x EMC").withStyle(TextFormatting.YELLOW)));

        tooltip.add(StringTextComponent.EMPTY);
        tooltip.add(new StringTextComponent("Efficiency: Highest").withStyle(TextFormatting.DARK_GRAY));
    }
}