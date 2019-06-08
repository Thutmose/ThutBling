package thut.bling.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thut.bling.ItemBling;
import thut.lib.IDefaultRecipe;
import thut.wearables.CompatWrapper;

public class RecipeBling implements IDefaultRecipe
{
    private ItemStack toRemove = ItemStack.EMPTY;
    private ItemStack output   = ItemStack.EMPTY;

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        ItemStack stack = net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
        if (!CompatWrapper.isValid(stack) && CompatWrapper.isValid(toRemove))
        {
            stack = toRemove;
            toRemove = ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = ItemStack.EMPTY;
        toRemove = ItemStack.EMPTY;
        boolean wearable = false;
        boolean gem = false;
        ItemStack worn = ItemStack.EMPTY;
        ItemStack gemStack = ItemStack.EMPTY;
        int n = 0;
        craft:
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                n++;
                if (stack.getItem() instanceof ItemBling)
                {
                    if (wearable) return false;
                    wearable = true;
                    worn = stack;
                    continue;
                }
                for (ItemStack key : RecipeLoader.instance.knownTextures.keySet())
                {
                    if (RecipeLoader.isSameStack(key, stack))
                    {
                        if (gem) return false;
                        gem = true;
                        gemStack = key;
                        continue craft;
                    }
                }
            }
        }
        if (n > 2 || !wearable) return false;
        if (gem)
        {
            output = worn.copy();
            if (!output.hasTag()) output.setTag(new CompoundNBT());
            if (output.getTag().hasKey("gem"))
            {
                output = ItemStack.EMPTY;
                return false;
            }
            String tex = RecipeLoader.instance.knownTextures.get(gemStack);
            if (tex == null)
            {
                tex = "minecraft:textures/blocks/stone.png";
            }
            output.getTag().putString("gem", tex);
            CompoundNBT tag = new CompoundNBT();
            gemStack.writeToNBT(tag);
            output.getTag().setTag("gemTag", tag);
        }
        else if (!gem && worn.hasTag() && worn.getTag().hasKey("gem"))
        {
            output = worn.copy();
            output.getTag().remove("gem");
            if (output.getTag().hasKey("gemTag"))
            {
                CompoundNBT tag = CompatWrapper.getTag(output, "gemTag", false);
                toRemove = new ItemStack(tag);
            }
            if (!CompatWrapper.isValid(toRemove)) output = ItemStack.EMPTY;
        }
        else
        {
            output = ItemStack.EMPTY;
        }
        return CompatWrapper.isValid(output);
    }

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }
}
