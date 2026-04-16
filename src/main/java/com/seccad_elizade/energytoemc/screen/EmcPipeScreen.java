package com.seccad_elizade.energytoemc.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seccad_elizade.energytoemc.EnergyToEmc;
import com.seccad_elizade.energytoemc.network.ModPacketHandler;
import com.seccad_elizade.energytoemc.network.PacketPipeMode;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EmcPipeScreen extends ContainerScreen<EmcPipeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(EnergyToEmc.MOD_ID, "textures/gui/emc_pipe_gui.png");

    public EmcPipeScreen(EmcPipeMenu menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // Hide default labels to prevent mapping errors and for a cleaner UI
        this.titleLabelX = -1000;
        this.inventoryLabelX = -1000;

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.addButton(new Button(x + 38, y + 50, 100, 20, new StringTextComponent("Switch Mode"), (button) -> {
            ModPacketHandler.INSTANCE.sendToServer(new PacketPipeMode(this.menu.getBlockEntity().getBlockPos()));
        }));
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        // If you still want to draw the "Inventory" text without using the problematic variable:
        // ITextComponent invName = new TranslationTextComponent("container.inventory");
        // this.font.draw(matrixStack, invName, 8, (float)this.imageHeight - 94, 4210752);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.minecraft != null) {
            this.minecraft.getTextureManager().bind(TEXTURE);
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(matrixStack, x, y, 0, 0, imageWidth, imageHeight);

        boolean isExtract = menu.isExtractMode();
        String modeText = isExtract ? "MODE: EXTRACT" : "MODE: INSERT";
        int color = isExtract ? 0xFF5555 : 0x55FF55;

        int textX = (imageWidth / 2) - (font.width(modeText) / 2);
        this.font.draw(matrixStack, modeText, x + textX, y + 10, color);

        if (!isExtract) {
            int slotX = x + 80;
            int slotY = y + 25;
            fill(matrixStack, slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xAA000000);
            drawCenteredString(matrixStack, this.font, "X", slotX + 8, slotY + 4, 0xFF0000);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, delta);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }
}