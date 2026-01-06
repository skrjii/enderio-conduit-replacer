package com.soliddowant.enderioconduitreplacer.gui;

import javax.annotation.Nonnull;

import com.soliddowant.enderioconduitreplacer.Tags;
import com.soliddowant.enderioconduitreplacer.util.ConduitSettingsUtil;

import crazypants.enderio.base.gui.GuiContainerBaseEIO;
import crazypants.enderio.base.integration.jei.IHaveGhostTargets;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiConduitReplacer extends GuiContainerBaseEIO<ItemStack>
        implements IHaveGhostTargets<GuiConduitReplacer> {

    private final ContainerConduitReplacer container;

    public GuiConduitReplacer(ContainerConduitReplacer container) {
        super(container.getPlayer().getHeldItem(container.getHand()), container, "conduit_replacer");
        this.container = container;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();

        // Add ghost slots to the handler
        getGhostSlotHandler().add(new ConduitGhostSlot(
                0, 44, 35,
                container::getSourceConduit,
                container::setSourceConduit,
                container::getHand));

        getGhostSlotHandler().add(new ConduitGhostSlot(
                1, 116, 35,
                container::getReplacementConduit,
                container::setReplacementConduit,
                container::getHand));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(getGuiTexture());
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
    }

    @Override
    protected void drawForegroundImpl(int mouseX, int mouseY) {
        // Draw title
        String title = I18n.format(Tags.MODID + ".gui.title");
        getFontRenderer().drawString(title, (xSize - getFontRenderer().getStringWidth(title)) / 2, 6, 0x404040);

        // Draw slot labels
        getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.source"), 35, 25, 0x404040);
        getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.replacement"), 99, 25, 0x404040);

        // Draw category info
        ItemStack source = container.getSourceConduit();
        ItemStack replacement = container.getReplacementConduit();

        int statusY = 55;

        if (!source.isEmpty() && !replacement.isEmpty()) {
            boolean sameCategory = ConduitSettingsUtil.areSameCategory(source, replacement);
            if (sameCategory) {
                String categoryName = ConduitSettingsUtil.getCategoryName(source);
                getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.category", categoryName), 8, statusY,
                        0x006600);
                getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.ready"), 8, statusY + 10, 0x006600);
            } else {
                getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.category_mismatch"), 8, statusY, 0xCC0000);
                String srcCat = ConduitSettingsUtil.getCategoryName(source);
                String repCat = ConduitSettingsUtil.getCategoryName(replacement);
                getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.category_mismatch_detail", srcCat, repCat), 8, statusY + 10, 0xCC0000);
            }
        } else if (!source.isEmpty()) {
            String categoryName = ConduitSettingsUtil.getCategoryName(source);
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.category", categoryName), 8, statusY, 0x404040);
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.set_replacement"), 8, statusY + 10, 0x808080);
        } else if (!replacement.isEmpty()) {
            String categoryName = ConduitSettingsUtil.getCategoryName(replacement);
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.category", categoryName), 8, statusY, 0x404040);
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.set_source"), 8, statusY + 10, 0x808080);
        } else {
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.set_conduits"), 8, statusY, 0x808080);
            getFontRenderer().drawString(I18n.format(Tags.MODID + ".gui.drag_hint"), 8, statusY + 10, 0x808080);
        }

        // Draw "Inventory" label
        getFontRenderer().drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 0x404040);

        super.drawForegroundImpl(mouseX, mouseY);
    }

    @Override
    @Nonnull
    public ResourceLocation getGuiTexture() {
        return new ResourceLocation(Tags.MODID, "textures/gui/conduit_replacer.png");
    }
}
