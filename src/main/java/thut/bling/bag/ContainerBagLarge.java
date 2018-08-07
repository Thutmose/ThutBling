package thut.bling.bag;

import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.bling.ThutBling;
import thut.bling.network.PacketBag;
import thut.lib.CompatWrapper;

@ChestContainer(isLargeChest = true, showButtons = false)
public class ContainerBagLarge extends Container
{
    public static int            yOffset;
    public static int            xOffset;

    public final InventoryLarge  invBag;
    public final InventoryPlayer invPlayer;

    public ContainerBagLarge(InventoryPlayer ivplay)
    {
        super();
        xOffset = 0;
        yOffset = 0;
        invBag = InventoryLarge.getBag(ivplay.player);
        invPlayer = ivplay;
        bindInventories();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            PacketBag packet = new PacketBag(PacketBag.ONOPEN);
            packet.data.setInteger("N", invBag.boxes.length);
            packet.data.setInteger("S", InventoryLarge.PAGECOUNT);
            for (int i = 0; i < invBag.boxes.length; i++)
            {
                packet.data.setString("N" + i, invBag.boxes[i]);
            }
            ThutBling.packetPipeline.sendTo(packet, (EntityPlayerMP) ivplay.player);
        }
    }

    /**
     * 
     */
    @Override
    protected Slot addSlotToContainer(Slot par1Slot)
    {
        par1Slot.slotNumber = this.inventorySlots.size();
        this.inventorySlots.add(par1Slot);
        this.inventoryItemStacks.add(invBag.getStackInSlot(par1Slot.getSlotIndex()));
        return par1Slot;
    }

    protected void bindInventories()
    {
        clearSlots();
        bindPCInventory();
        bindPlayerInventory();
    }

    protected void bindPCInventory()
    {
        int n = 0;
        n = invBag.getPage() * 54;
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(invBag, n + j + i * 9, 8 + j * 18 + xOffset, 18 + i * 18 + yOffset)
                {
                    @Override
                    public boolean isItemValid(ItemStack itemstack)
                    {
                        return !InventoryLarge.INVALID.contains(itemstack.getItem());
                    }
                });
            }
        }
        // int k = 0;
        for (Object o : inventorySlots)
        {
            if (o instanceof Slot)
            {
                ((Slot) o).onSlotChanged();
            }
        }
    }

    protected void bindPlayerInventory()
    {
        int offset = 64 + yOffset;

        for (int i = 0; i < 9; i++)
        {
            addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18 + xOffset, 142 + offset));
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18 + xOffset, 84 + i * 18 + offset));
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return true;
    }

    public void changeName(String name)
    {
        invBag.boxes[invBag.getPage()] = name;
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PacketBag packet = new PacketBag(PacketBag.RENAME);
            packet.data.setString("N", name);
            ThutBling.packetPipeline.sendToServer(packet);
        }
    }

    protected void clearSlots()
    {
        this.inventorySlots.clear();
    }

    @SideOnly(Side.CLIENT)
    public String getPage()
    {
        if (invBag.getPage() < 0)
        {
            Thread.dumpStack();
            invBag.setPage(0);
        }
        return invBag.boxes[invBag.getPage()];
    }

    @SideOnly(Side.CLIENT)
    public String getPageNb()
    {
        return Integer.toString(invBag.getPage() + 1);
    }

    @Override
    public Slot getSlot(int par1)
    {
        return this.inventorySlots.get(par1);
    }

    public void gotoInventoryPage(int page)
    {
        if (page - 1 == invBag.getPage()) return;
        invBag.setPage(page - 1);
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            PacketBag packet = new PacketBag(PacketBag.SETPAGE);
            packet.data.setInteger("P", page);
            ThutBling.packetPipeline.sendToServer(packet);
            invBag.clear();
        }
        bindInventories();
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        invBag.closeInventory(player);
    }

    /** args: slotID, itemStack to put in slot */
    @Override
    public void putStackInSlot(int par1, ItemStack par2ItemStack)
    {
        this.getSlot(par1).putStack(par2ItemStack);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index)
    {
        ItemStack itemstack = CompatWrapper.nullStack;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            int numRows = 6;

            if (index < numRows * 9)
            {
                if (!this.mergeItemStack(itemstack1, numRows * 9, this.inventorySlots.size(),
                        false)) { return CompatWrapper.nullStack; }
            }
            else if (!this.mergeItemStack(itemstack1, 0, numRows * 9, false)) { return CompatWrapper.nullStack; }

            if (!CompatWrapper.isValid(itemstack1))
            {
                slot.putStack(CompatWrapper.nullStack);
            }
            else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    public void updateInventoryPages(int dir, InventoryPlayer invent)
    {
        int page = (invBag.getPage() == 0) && (dir == -1) ? InventoryLarge.PAGECOUNT - 1
                : (invBag.getPage() + dir) % InventoryLarge.PAGECOUNT;
        page += 1;
        gotoInventoryPage(page);
    }

}
