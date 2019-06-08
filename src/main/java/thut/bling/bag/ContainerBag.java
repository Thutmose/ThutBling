package thut.bling.bag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

import invtweaks.api.container.ChestContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import thut.bling.ItemBling;
import thut.wearables.ThutWearables;

@ChestContainer(isLargeChest = false, showButtons = false)
public class ContainerBag extends ContainerChest
{
    public static boolean loadBag(UUID bag, InventoryBasic inventory)
    {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        ISaveHandler saveHandler = world.getSaveHandler();
        String seperator = System.getProperty("file.separator");
        File file = saveHandler.getMapFileFromName("bling_bags" + seperator + bag);
        File dir = new File(file.getParentFile().getAbsolutePath());
        if (!file.exists())
        {
            dir.mkdirs();
            return false;
        }

        try
        {
            FileInputStream fileinputstream = new FileInputStream(file);
            CompoundNBT CompoundNBT = CompressedStreamTools.readCompressed(fileinputstream);
            fileinputstream.close();
            CompoundNBT tag = CompoundNBT.getCompound("Data");
            ListNBT ListNBT = tag.getTagList("Inventory", 10);
            for (int i = 0; i < ListNBT.size(); ++i)
            {
                CompoundNBT CompoundNBT1 = ListNBT.getCompound(i);
                int j = CompoundNBT1.getByte("Slot") & 255;
                if (j < inventory.getSizeInventory())
                {
                    inventory.setInventorySlotContents(j, new ItemStack(CompoundNBT1));
                }
            }
            return true;
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public static void saveBag(ItemStack bag, InventoryBasic inventory)
    {
        if (!bag.hasTag()) bag.setTag(new CompoundNBT());
        CompoundNBT inventoryTag = bag.getTag();
        if (!inventoryTag.hasKey("bagID"))
        {
            inventoryTag.putString("bagID", UUID.randomUUID().toString());
        }
        // Remove the legacy tag
        inventoryTag.remove("Inventory");

        UUID bagID = UUID.fromString(bag.getTag().getString("bagID"));

        CompoundNBT tag = new CompoundNBT();
        ListNBT ListNBT = new ListNBT();
        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (!itemstack.isEmpty())
            {
                CompoundNBT CompoundNBT1 = new CompoundNBT();
                CompoundNBT1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(CompoundNBT1);
                ListNBT.appendTag(CompoundNBT1);
            }
        }
        tag.setTag("Inventory", ListNBT);
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        ISaveHandler saveHandler = world.getSaveHandler();
        String seperator = System.getProperty("file.separator");
        File file = saveHandler.getMapFileFromName("bling_bags" + seperator + bagID);
        File dir = new File(file.getParentFile().getAbsolutePath());
        if (!file.exists())
        {
            dir.mkdirs();
        }
        if (file != null)
        {
            CompoundNBT CompoundNBT1 = new CompoundNBT();
            CompoundNBT1.setTag("Data", tag);
            try
            {
                FileOutputStream fileoutputstream = new FileOutputStream(file);
                CompressedStreamTools.writeCompressed(CompoundNBT1, fileoutputstream);
                fileoutputstream.close();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static Container makeContainer(ItemStack bag, PlayerEntity player)
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

    private static InventoryBasic init(ItemStack bag, PlayerEntity player)
    {
        InventoryBasic inventory = new InventoryBasic("item.bling_bag.name", false, 27);
        if (bag.hasTag() && bag.getTag().hasKey("bagID") && !player.getEntityWorld().isRemote)
        {
            UUID id = UUID.fromString(bag.getTag().getString("bagID"));
            // Try loading bag.
            if (!loadBag(id, inventory))
            {
                // Otherwise legacy load
                ListNBT ListNBT = bag.getTag().getTagList("Inventory", 10);
                for (int i = 0; i < ListNBT.size(); ++i)
                {
                    CompoundNBT CompoundNBT1 = ListNBT.getCompound(i);
                    int j = CompoundNBT1.getByte("Slot") & 255;
                    if (j < inventory.getSizeInventory())
                    {
                        inventory.setInventorySlotContents(j, new ItemStack(CompoundNBT1));
                    }
                }
            }
        }
        return inventory;
    }

    final ItemStack             bag;
    public final InventoryBasic inventory;

    protected ContainerBag(PlayerEntity player, InventoryBasic bagInventory, final ItemStack bag)
    {
        super(player.inventory, bagInventory, player);
        this.inventory = bagInventory;
        this.bag = bag;
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();
        final UUID bagID = (bag.hasTag() && bag.getTag().hasKey("bagID"))
                ? UUID.fromString(bag.getTag().getString("bagID")) : UUID.randomUUID();
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
                    public boolean canTakeStack(PlayerEntity playerIn)
                    {
                        UUID id = null;
                        if (getStack().hasTag() && getStack().getTag().hasKey("bagID"))
                        {
                            try
                            {
                                id = UUID.fromString(getStack().getTag().getString("bagID"));
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
                public boolean canTakeStack(PlayerEntity playerIn)
                {
                    UUID id = null;
                    if (getStack().hasTag() && getStack().getTag().hasKey("bagID"))
                    {
                        try
                        {
                            id = UUID.fromString(getStack().getTag().getString("bagID"));
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

    private void save(PlayerEntity playerIn)
    {
        if (playerIn.world.isRemote || inventory instanceof InventoryEnderChest) return;
        saveBag(bag, inventory);
        ThutWearables.syncWearables(playerIn);
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn)
    {
        save(playerIn);
        super.onContainerClosed(playerIn);
        inventory.closeInventory(playerIn);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn)
    {
        return true;
    }

}
