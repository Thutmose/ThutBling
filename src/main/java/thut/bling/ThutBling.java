package thut.bling;

import java.awt.Color;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;
import thut.bling.bag.ContainerBag;
import thut.bling.client.item.TextureHandler;
import thut.bling.recipe.RecipeBling;
import thut.bling.recipe.RecipeLoader;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

@Mod(modid = ThutBling.MODID, name = "Thut's Bling", dependencies = "required-after:thut_wearables;required-after:thutcore", version = ThutBling.VERSION, acceptedMinecraftVersions = Reference.MCVERSIONS)
public class ThutBling
{
    public static final String MODID   = Reference.MODID;
    public static final String VERSION = Reference.VERSION;

    @SidedProxy
    public static CommonProxy  proxy;
    @Instance(value = MODID)
    public static ThutBling    instance;
    public static Item         bling   = new ItemBling().setRegistryName(MODID, "bling");

    public ThutBling()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBling(RegistryEvent.Register<Item> evt)
    {
        evt.getRegistry().register(bling);
        bling.setCreativeTab(CreativeTabs.TOOLS);
        ((ItemBling) bling).initDefaults();
    }

    @Method(modid = "thutcore")
    @EventHandler
    public void Init(FMLInitializationEvent e)
    {
        bling.setCreativeTab(thut.core.common.ThutCore.tabThut);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        RecipeLoader.instance = new RecipeLoader(e);
        proxy.preInit(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    @SubscribeEvent
    public void initRecipes(RegistryEvent.Register<IRecipe> evt)
    {
        IRecipe recipe = new RecipeBling().setRegistryName(new ResourceLocation(MODID, "bling"));
        GameData.register_impl(recipe);
        ResourceLocation group = new ResourceLocation(MODID, "bling");
        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.WAIST),
                new Object[] { "   ", "LLL", "   ", 'L', Items.LEATHER })
                        .setRegistryName(new ResourceLocation(MODID, "belt"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.FINGER),
                new Object[] { " L ", "L L", " L ", 'L', Items.GOLD_NUGGET })
                        .setRegistryName(new ResourceLocation(MODID, "ring"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.WRIST),
                new Object[] { " L ", "L L", " L ", 'L', Items.LEATHER })
                        .setRegistryName(new ResourceLocation(MODID, "brace"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.EAR),
                new Object[] { "SLS", "L L", " L ", 'L', Items.GOLD_NUGGET, 'S', Items.STRING })
                        .setRegistryName(new ResourceLocation(MODID, "earring"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.EAR),
                new Object[] { "SSS", "G G", "   ", 'G', Items.GLASS_BOTTLE, 'S', Items.STICK })
                        .setRegistryName(new ResourceLocation(MODID, "glasses"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.EYE),
                new Object[] { "SLS", "L L", " L ", 'L', Items.GOLD_NUGGET, 'S', Items.STRING })
                        .setRegistryName(new ResourceLocation(MODID, "earring"));
        GameData.register_impl(recipe);

        recipe = new ShapedOreRecipe(group, ItemBling.defaults.get(EnumWearable.BACK),
                new Object[] { "SLS", "LCL", " L ", 'L', Items.LEATHER, 'S', Items.STRING, 'C', Blocks.CHEST })
                        .setRegistryName(new ResourceLocation(MODID, "bag"));
        GameData.register_impl(recipe);

        recipe = new ShapelessOreRecipe(group, ItemBling.defaults.get(EnumWearable.HAT), Items.LEATHER_HELMET)
                .setRegistryName(new ResourceLocation(MODID, "hat"));
        GameData.register_impl(recipe);

        recipe = new ShapelessOreRecipe(group, ItemBling.defaults.get(EnumWearable.NECK),
                ItemBling.defaults.get(EnumWearable.FINGER), Items.STRING)
                        .setRegistryName(new ResourceLocation(MODID, "neck"));
        GameData.register_impl(recipe);

        recipe = new ShapelessOreRecipe(group, ItemBling.defaults.get(EnumWearable.ANKLE),
                ItemBling.defaults.get(EnumWearable.WRIST)).setRegistryName(new ResourceLocation(MODID, "ankle"));
        GameData.register_impl(recipe);

        recipe = new ShapelessOreRecipe(group, ItemBling.defaults.get(EnumWearable.WRIST),
                ItemBling.defaults.get(EnumWearable.ANKLE)).setRegistryName(new ResourceLocation(MODID, "wrist2"));
        GameData.register_impl(recipe);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        RecipeLoader.instance.init();
    }

    public static class CommonProxy implements IGuiHandler
    {
        public void preInit(FMLPreInitializationEvent event)
        {
        }

        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            PlayerWearables cap = ThutWearables.getWearables(player);
            ItemStack bag = null;
            if (bag == null) bag = player.getHeldItemMainhand();
            if (bag == null || !(bag.getItem() instanceof ItemBling)) bag = player.getHeldItemOffhand();
            if (bag == null || !(bag.getItem() instanceof ItemBling)) bag = cap.getWearable(EnumWearable.BACK);
            if (bag == null || !(bag.getItem() instanceof ItemBling)) return null;
            return new ContainerBag(player, ContainerBag.init(bag), bag);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }
    }

    public static class ServerProxy extends CommonProxy
    {
    }

    public static class ClientProxy extends CommonProxy
    {
        Map<EnumWearable, IModel>             defaultModels   = Maps.newHashMap();
        Map<EnumWearable, ResourceLocation[]> defaultTextures = Maps.newHashMap();

        Map<String, IModel>                   customModels    = Maps.newHashMap();
        Map<String, ResourceLocation[]>       customTextures  = Maps.newHashMap();

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            PlayerWearables cap = ThutWearables.getWearables(player);
            ItemStack bag = null;
            if (bag == null) bag = player.getHeldItemMainhand();
            if (bag == null || !(bag.getItem() instanceof ItemBling)) bag = player.getHeldItemOffhand();
            if (bag == null || !(bag.getItem() instanceof ItemBling)) bag = cap.getWearable(EnumWearable.BACK);
            if (bag == null || !(bag.getItem() instanceof ItemBling)) return null;
            final InventoryBasic inv = ContainerBag.init(bag);
            return new GuiContainer(new ContainerBag(player, inv, bag))
            {
                private final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(
                        "textures/gui/container/generic_54.png");

                /** Draws the screen and all the components in it. */
                @Override
                public void drawScreen(int mouseX, int mouseY, float partialTicks)
                {
                    this.drawDefaultBackground();
                    super.drawScreen(mouseX, mouseY, partialTicks);
                    this.renderHoveredToolTip(mouseX, mouseY);
                }

                /** Draw the foreground layer for the GuiContainer (everything
                 * in front of the items) */
                @Override
                protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
                {
                    this.fontRenderer.drawString(inv.getDisplayName().getUnformattedText(), 8, 6, 4210752);
                    this.fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, 74,
                            4210752);
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
            };
        }

        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            TextureHandler.registerItemModels();
            MinecraftForge.EVENT_BUS.register(this);
        }

        private void initDefaultModels()
        {
            if (!defaultModels.isEmpty()) return;
            for (EnumWearable slot : EnumWearable.values())
            {
                IModel model = defaultModels.get(slot);
                ResourceLocation[] tex = defaultTextures.get(slot);
                if (model == null)
                {
                    ModelHolder holder = null;
                    if (slot == EnumWearable.WAIST || slot == EnumWearable.WRIST || slot == EnumWearable.ANKLE
                            || slot == EnumWearable.FINGER || slot == EnumWearable.EAR || slot == EnumWearable.NECK)
                    {
                        tex = new ResourceLocation[2];
                        tex[0] = new ResourceLocation("minecraft", "textures/items/diamond.png");
                        tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/belt.png");
                        holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/belt.x3d"), tex[1],
                                null, "belt");
                    }
                    if (slot == EnumWearable.HAT)
                    {
                        tex = new ResourceLocation[2];
                        tex[0] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat.png");
                        tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/hat2.png");
                        holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/hat.x3d"), tex[0],
                                null, "belt");
                    }
                    if (slot == EnumWearable.BACK)
                    {
                        tex = new ResourceLocation[2];
                        tex[0] = new ResourceLocation(ThutBling.MODID, "textures/worn/bag1.png");
                        tex[1] = new ResourceLocation(ThutBling.MODID, "textures/worn/bag2.png");
                        holder = new ModelHolder(new ResourceLocation(ThutBling.MODID, "models/worn/bag.x3d"), tex[0],
                                null, "belt");
                    }
                    if (holder != null) model = ModelFactory.create(holder);
                    if (model != null && tex != null)
                    {
                        defaultModels.put(slot, model);
                        defaultTextures.put(slot, tex);
                    }
                }
            }
        }

        private IModel getModels(EnumWearable slot, ItemStack stack)
        {
            IModel imodel;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("model"))
            {
                String model = stack.getTagCompound().getString("model");
                imodel = customModels.get(model);
                if (imodel == null)
                {
                    ResourceLocation loc = new ResourceLocation(model);
                    imodel = ModelFactory.create(new ModelHolder(loc, null, null, model));
                    if (model != null)
                    {
                        customModels.put(model, imodel);
                        return imodel;
                    }
                }
                else return imodel;
            }
            imodel = defaultModels.get(slot);
            return imodel;
        }

        private ResourceLocation[] getTextures(EnumWearable slot, ItemStack stack)
        {
            ResourceLocation[] textures;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("tex"))
            {
                String tex = stack.getTagCompound().getString("tex");
                textures = customTextures.get(tex);
                if (textures == null)
                {
                    textures = new ResourceLocation[2];
                    textures[0] = new ResourceLocation(tex);
                    if (stack.getTagCompound().hasKey("tex2"))
                    {
                        textures[1] = new ResourceLocation(stack.getTagCompound().getString("tex2"));
                    }
                    else textures[1] = textures[0];
                    customTextures.put(tex, textures);
                    return textures;
                }
                else return textures;
            }
            textures = defaultTextures.get(slot);
            return textures;
        }

        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            initDefaultModels();
            IModel model = getModels(slot, stack);
            ResourceLocation[] textures = getTextures(slot, stack);
            if (model == null && slot != EnumWearable.EYE) return;
            int brightness = wearer.getBrightnessForRender();
            switch (slot)
            {
            case ANKLE:
                renderAnkle(wearer, stack, model, textures, brightness);
                break;
            case BACK:
                renderBack(wearer, stack, model, textures, brightness);
                break;
            case EAR:
                renderEar(wearer, stack, model, textures, brightness);
                break;
            case EYE:
                renderEye(wearer, stack, model, textures, brightness);
                break;
            case FINGER:
                renderFinger(wearer, stack, model, textures, brightness);
                break;
            case HAT:
                renderHat(wearer, stack, model, textures, brightness);
                break;
            case NECK:
                renderNeck(wearer, stack, model, textures, brightness);
                break;
            case WAIST:
                renderWaist(wearer, stack, model, textures, brightness);
                break;
            case WRIST:
                renderWrist(wearer, stack, model, textures, brightness);
                break;
            default:
                break;
            }
        }

        private void renderAnkle(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            float s, sy, sx, sz, dx, dy, dz;
            dx = 0.f;
            dy = .06f;
            dz = 0.f;
            s = 0.475f;
            sx = 1.05f * s / 2;
            sy = s * 1.8f / 2;
            sz = s / 2;
            Vector3f dr = new Vector3f(dx, dy, dz);
            Vector3f ds = new Vector3f(sx, sy, sz);
            renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
        }

        private void renderBack(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            if (!(model instanceof IModelCustom)) return;
            IModelCustom renderable = (IModelCustom) model;

            EnumDyeColor ret;
            Color colour;
            int[] col;

            ResourceLocation[] tex = textures.clone();
            Minecraft minecraft = Minecraft.getMinecraft();
            float s;
            GlStateManager.pushMatrix();
            s = 0.65f;
            GL11.glScaled(s, -s, -s);
            minecraft.renderEngine.bindTexture(tex[0]);
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.translate(0, -.18, -0.85);
            col = new int[] { 255, 255, 255, 255, brightness };
            for (IExtendedModelPart part1 : model.getParts().values())
            {
                part1.setRGBAB(col);
            }
            renderable.renderAll();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GL11.glScaled(s, -s, -s);
            minecraft.renderEngine.bindTexture(tex[1]);
            ret = EnumDyeColor.RED;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            colour = new Color(ret.getColorValue() + 0xFF000000);
            col[0] = colour.getRed();
            col[1] = colour.getGreen();
            col[2] = colour.getBlue();
            for (IExtendedModelPart part1 : model.getParts().values())
            {
                part1.setRGBAB(col);
            }
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.translate(0, -.18, -0.85);
            renderable.renderAll();
            GL11.glColor3f(1, 1, 1);
            GlStateManager.popMatrix();
        }

        private void renderEar(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            float s, dx, dy, dz;
            dx = 0.0f;
            dy = .175f;
            dz = 0.0f;
            s = 0.475f / 4f;
            Vector3f dr = new Vector3f(dx, dy, dz);
            Vector3f ds = new Vector3f(s, s, s);
            renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
        }

        private void renderEye(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            // TODO eye by model instead of texture.
            GlStateManager.pushMatrix();
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(MODID, "textures/items/eye.png"));
            GL11.glTranslated(-0.26, -0.175, -0.251);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexbuffer = tessellator.getBuffer();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            double height = 0.5;
            double width = 0.5;
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos(0.0D, height, 0.0D).tex(0.0D, 1).color(255, 255, 255, 255).endVertex();
            vertexbuffer.pos(width, height, 0.0D).tex(1, 1).color(255, 255, 255, 255).endVertex();
            vertexbuffer.pos(width, 0.0D, 0.0D).tex(1, 0).color(255, 255, 255, 255).endVertex();
            vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0).color(255, 255, 255, 255).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }

        private void renderFinger(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            float s, dx, dy, dz;
            dx = 0.0f;
            dy = .175f;
            dz = 0.0f;
            s = 0.475f / 4f;
            Vector3f dr = new Vector3f(dx, dy, dz);
            Vector3f ds = new Vector3f(s, s, s);
            renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
        }

        private void renderHat(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            if (!(model instanceof IModelCustom)) return;
            IModelCustom renderable = (IModelCustom) model;

            EnumDyeColor ret;
            Color colour;
            int[] col;

            ResourceLocation[] tex = textures.clone();
            Minecraft minecraft = Minecraft.getMinecraft();
            float s;
            GlStateManager.pushMatrix();
            s = 0.285f;
            GL11.glScaled(s, -s, -s);
            minecraft.renderEngine.bindTexture(tex[0]);
            col = new int[] { 255, 255, 255, 255, brightness };
            for (IExtendedModelPart part1 : model.getParts().values())
            {
                part1.setRGBAB(col);
            }
            renderable.renderAll();
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GL11.glScaled(s * 0.995f, -s * 0.995f, -s * 0.995f);
            minecraft.renderEngine.bindTexture(tex[1]);
            ret = EnumDyeColor.RED;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            colour = new Color(ret.getColorValue() + 0xFF000000);
            col[0] = colour.getRed();
            col[1] = colour.getGreen();
            col[2] = colour.getBlue();
            for (IExtendedModelPart part1 : model.getParts().values())
            {
                part1.setRGBAB(col);
            }
            renderable.renderAll();
            GL11.glColor3f(1, 1, 1);
            GlStateManager.popMatrix();
        }

        private void renderNeck(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            if (!(model instanceof IModelCustom)) return;
            ResourceLocation[] tex = textures.clone();
            IModelCustom renderable = (IModelCustom) model;
            EnumDyeColor ret;
            Color colour;
            int[] col;
            float s, dx, dy, dz;
            dx = 0;
            dy = -.0f;
            dz = -0.03f;
            s = 0.525f;
            if (wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS) == null)
            {
                s = 0.465f;
            }
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
            {
                tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
            }
            else
            {
                tex[0] = null;
            }
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslatef(dx, dy, dz);
            GL11.glScalef(s, s, s);
            String colorpart = "main";
            String itempart = "gem";
            ret = EnumDyeColor.YELLOW;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            colour = new Color(ret.getColorValue() + 0xFF000000);
            col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
            IExtendedModelPart part = model.getParts().get(colorpart);
            if (part != null)
            {
                part.setRGBAB(col);
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[1]);
                GlStateManager.scale(1, 1, .1);
                renderable.renderPart(colorpart);
            }
            GL11.glColor3f(1, 1, 1);
            part = model.getParts().get(itempart);
            if (part != null && tex[0] != null)
            {
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[0]);
                GlStateManager.scale(1, 1, 10);
                GlStateManager.translate(0, 0.01, -0.075);
                renderable.renderPart(itempart);
            }
            GL11.glPopMatrix();
        }

        private void renderWaist(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            float s, dx, dy, dz;
            dx = 0;
            dy = -.0f;
            dz = -0.6f;
            s = 0.525f;
            if (wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS) == null)
            {
                s = 0.465f;
            }
            Vector3f dr = new Vector3f(dx, dy, dz);
            Vector3f ds = new Vector3f(s, s, s);
            renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
        }

        private void renderWrist(EntityLivingBase wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            float s, sy, sx, sz, dx, dy, dz;
            dx = 0.f;
            dy = .06f;
            dz = 0.f;
            s = 0.475f;
            sx = 1.05f * s / 2;
            sy = s * 1.8f / 2;
            sz = s / 2;
            Vector3f dr = new Vector3f(dx, dy, dz);
            Vector3f ds = new Vector3f(sx, sy, sz);
            renderStandardModelWithGem(stack, "main", "gem", model, textures, brightness, dr, ds);
        }

        private void renderStandardModelWithGem(ItemStack stack, String colorpart, String itempart, IModel model,
                ResourceLocation[] tex, int brightness, Vector3f dr, Vector3f ds)
        {
            if (!(model instanceof IModelCustom)) return;
            tex = tex.clone();
            IModelCustom renderable = (IModelCustom) model;
            EnumDyeColor ret = EnumDyeColor.YELLOW;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("dyeColour"))
            {
                int damage = stack.getTagCompound().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getColorValue() + 0xFF000000);
            int[] col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
            IExtendedModelPart part = model.getParts().get(colorpart);

            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("gem"))
            {
                tex[0] = new ResourceLocation(stack.getTagCompound().getString("gem"));
            }
            else
            {
                tex[0] = null;
            }
            GL11.glPushMatrix();
            GL11.glRotated(90, 1, 0, 0);
            GL11.glRotated(180, 0, 0, 1);
            GL11.glTranslatef(dr.x, dr.y, dr.z);
            GL11.glScalef(ds.x, ds.y, ds.z);
            if (part != null)
            {
                part.setRGBAB(col);
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[1]);
                renderable.renderPart(colorpart);
            }
            GL11.glColor3f(1, 1, 1);
            part = model.getParts().get(itempart);
            if (part != null && tex[0] != null)
            {
                Minecraft.getMinecraft().renderEngine.bindTexture(tex[0]);
                renderable.renderPart(itempart);
            }
            GL11.glPopMatrix();
        }
    }
}
