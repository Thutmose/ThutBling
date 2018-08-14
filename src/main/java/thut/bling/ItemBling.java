package thut.bling;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import thut.bling.bag.InventoryLarge;
import thut.core.common.CreativeTabThut;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class ItemBling extends Item implements IWearable
{
    public static Map<String, EnumWearable> wearables = Maps.newHashMap();
    public static Map<Item, EnumWearable>   defaults  = Maps.newHashMap();
    public static List<String>              names     = Lists.newArrayList();
    public static List<Item>                bling     = Lists.newArrayList();
    static
    {
        wearables.put("ring", EnumWearable.FINGER);
        wearables.put("neck", EnumWearable.NECK);
        wearables.put("wrist", EnumWearable.WRIST);
        wearables.put("eye", EnumWearable.EYE);
        wearables.put("ankle", EnumWearable.ANKLE);
        wearables.put("ear", EnumWearable.EAR);
        wearables.put("waist", EnumWearable.WAIST);
        wearables.put("hat", EnumWearable.HAT);
        wearables.put("bag", EnumWearable.BACK);
        wearables.put("bag_ender_vanilla", EnumWearable.BACK);
        wearables.put("bag_ender_large", EnumWearable.BACK);
        names.addAll(wearables.keySet());
        Collections.sort(names);
    }

    public static void initDefaults(IForgeRegistry<Item> iForgeRegistry)
    {
        for (String s : names)
        {
            ItemBling bling = new ItemBling(s, wearables.get(s));
            bling.setCreativeTab(CreativeTabs.DECORATIONS);
            bling.setRegistryName(ThutBling.MODID, "bling_" + s);
            bling.setUnlocalizedName("bling_" + s);
            iForgeRegistry.register(bling);
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                ModelLoader.setCustomModelResourceLocation(bling, 0,
                        new ModelResourceLocation(bling.getRegistryName(), "inventory"));
            }
            ItemBling.bling.add(bling);
        }
    }

    public static void initTabs(CreativeTabThut tabThut)
    {
        for (Item i : bling)
            i.setCreativeTab(tabThut);
    }

    public final String        name;
    private final EnumWearable slot;

    public ItemBling(String name, EnumWearable slot)
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.name = name;
        this.slot = slot;
        defaults.put(this, slot);
        if (name.equals("bag_ender_large")) InventoryLarge.INVALID.add(this);
    }

    /** allows items to add custom lines of information to the mouseover
     * description */
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag bool)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
        {
            int damage = stack.getTagCompound().getInteger("dyeColour");
            EnumDyeColor colour = EnumDyeColor.byDyeDamage(damage);
            String s = I18n.format(colour.getUnlocalizedName());
            list.add(s);
        }
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gemTag"))
        {
            ItemStack gem = new ItemStack(stack.getTagCompound().getCompoundTag("gemTag"));
            if (gem != null)
            {
                try
                {
                    list.add(gem.getDisplayName());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if (slot == EnumWearable.BACK)
        {// TODO see why this is broken
            playerIn.openGui(ThutBling.instance, 0, worldIn, 0, 0, 0);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
        }
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        return slot;
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        ThutBling.proxy.renderWearable(slot, wearer, stack, partialTicks);
    }

    @Override
    public boolean dyeable(ItemStack stack)
    {
        return true;
    }

}
