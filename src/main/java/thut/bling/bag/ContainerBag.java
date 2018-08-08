package thut.bling.bag;

import java.util.UUID;

import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thut.bling.ItemBling;
import thut.wearables.CompatWrapper;
import thut.wearables.ThutWearables;

@ChestContainer(isLargeChest = false, showButtons = false)
public class ContainerBag extends ContainerChest
{
    public static Container makeContainer(ItemStack bag, EntityPlayer player)
    {
        if (bag.getItem() instanceof ItemBling)
        {
            String name = ((ItemBling) bag.getItem()).name;
            if (name.equals(
                    "bag_ender_vanilla")) { return new ContainerBag(player, player.getInventoryEnderChest(), bag); }
            if (name.equals("bag_ender_large")) { return new ContainerBagLarge(player.inventory); }
        }
        return new ContainerBag(player, init(bag, player), bag);
    }

    private static InventoryBasic init(ItemStack bag, EntityPlayer player)
    {
        InventoryBasic inventory = new InventoryBasic("item.bling_bag.name", false, 27);
        if (bag.hasTagCompound())
        {
            NBTTagList nbttaglist = bag.getTagCompound().getTagList("Inventory", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;
                if (j < inventory.getSizeInventory())
                {
                    inventory.setInventorySlotContents(j, CompatWrapper.fromTag(nbttagcompound1));
                }
            }
        }
        return inventory;
    }

    final ItemStack             bag;
    public final InventoryBasic inventory;

    protected ContainerBag(EntityPlayer player, InventoryBasic bagInventory, final ItemStack bag)
    {
        super(player.inventory, bagInventory, player);
        this.inventory = bagInventory;
        this.bag = bag;
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
        final UUID bagID = (bag.hasTagCompound() && bag.getTagCompound().hasKey("bagID"))
                ? UUID.fromString(bag.getTagCompound().getString("bagID")) : UUID.randomUUID();
        int i = (3 - 4) * 18;

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(bagInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        // Player main inventory.
        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i)
                {
                    @Override
                    public boolean canTakeStack(EntityPlayer playerIn)
                    {
                        UUID id = null;
                        if (getStack().hasTagCompound() && getStack().getTagCompound().hasKey("bagID"))
                        {
                            try
                            {
                                id = UUID.fromString(getStack().getTagCompound().getString("bagID"));
                            }
                            catch (Exception e)
                            {
                            }
                        }
                        return !bagID.equals(id);
                    }
                });
            }
        }

        // Player hotbar.
        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 161 + i)
            {
                @Override
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    UUID id = null;
                    if (getStack().hasTagCompound() && getStack().getTagCompound().hasKey("bagID"))
                    {
                        try
                        {
                            id = UUID.fromString(getStack().getTagCompound().getString("bagID"));
                        }
                        catch (Exception e)
                        {
                        }
                    }
                    return !bagID.equals(id);
                }
            });
        }
    }

    private void save(EntityPlayer playerIn)
    {
        if (playerIn.world.isRemote || inventory instanceof InventoryEnderChest) return;
        if (!bag.hasTagCompound()) bag.setTagCompound(new NBTTagCompound());
        NBTTagCompound inventoryTag = bag.getTagCompound();
        if (!inventoryTag.hasKey("bagID"))
        {
            inventoryTag.setString("bagID", UUID.randomUUID().toString());
        }
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (!itemstack.isEmpty())
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        inventoryTag.setTag("Inventory", nbttaglist);
        ThutWearables.syncWearables(playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        save(playerIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

}
