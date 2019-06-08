package thut.bling.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import thut.bling.bag.ContainerBag;
import thut.bling.bag.ContainerBagLarge;

public class GuiBag extends GuiContainer
{
    public static Object createGui(Container container, EntityPlayer player)
    {
        if (container instanceof ContainerBag) return new GuiBag((ContainerBag) container, player);
        if (container instanceof ContainerBagLarge) return new GuiBagLarge((ContainerBagLarge) container);
        return null;
    }

    final ContainerBag container;
    final EntityPlayer player;

    public GuiBag(ContainerBag inventorySlotsIn, EntityPlayer player)
    {
        super(inventorySlotsIn);
        this.container = inventorySlotsIn;
        this.player = player;
    }

    private final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(container.inventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
        this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 74, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 3 * 18 + 17);
        this.drawTexturedModalRect(i, j + 3 * 18 + 17, 0, 126, this.xSize, 96);
    }

}
