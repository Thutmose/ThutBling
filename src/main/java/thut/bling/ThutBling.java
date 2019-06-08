package thut.bling;

import java.awt.Color;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.api.distmarker.Dist;
import thut.bling.bag.ContainerBag;
import thut.bling.bag.InventoryLarge;
import thut.bling.network.PacketBag;
import thut.bling.recipe.RecipeLoader;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

@Mod(modid = Reference.MODID, name = "Thut's Bling", dependencies = "required-after:thut_wearables;required-after:thutcore", version = Reference.VERSION, acceptableRemoteVersions = Reference.MINVERSION, acceptedMinecraftVersions = Reference.MCVERSIONS)
public class ThutBling
{
    public static final String         MODID          = Reference.MODID;
    public static final String         VERSION        = Reference.VERSION;
    public static SimpleNetworkWrapper packetPipeline = new SimpleNetworkWrapper(MODID);

    @SidedProxy
    public static CommonProxy          proxy;
    @Instance(value = MODID)
    public static ThutBling            instance;

    public ThutBling()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBling(RegistryEvent.Register<Item> evt)
    {
        ItemBling.initDefaults(evt.getRegistry());
    }

    @Method(modid = "thutcore")
    @EventHandler
    public void Init(FMLInitializationEvent e)
    {
        ItemBling.initTabs(thut.core.common.ThutCore.tabThut);
    }

    @EventHandler
    public void preInit(FMLCommonSetupEvent e)
    {
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();
        InventoryLarge.PAGECOUNT = config.getInt("large_ender_pages", Configuration.CATEGORY_GENERAL,
                InventoryLarge.PAGECOUNT, 1, 99, "Number of pages in the large ender bag");
        config.save();
        RecipeLoader.instance = new RecipeLoader(e);
        proxy.preInit(e);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        packetPipeline.registerMessage(PacketBag.class, PacketBag.class, 0, Dist.CLIENT);
        packetPipeline.registerMessage(PacketBag.class, PacketBag.class, 1, Dist.DEDICATED_SERVER);

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        RecipeLoader.instance.init();
    }

    public static class CommonProxy implements IGuiHandler
    {
        public void preInit(FMLCommonSetupEvent event)
        {
        }

        public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
        {
        }

        public PlayerEntity getClientPlayer()
        {
            return null;
        }

        public IThreadListener getMainThreadListener()
        {
            return FMLCommonHandler.instance().getMinecraftServerInstance();
        }

        @Override
        public Object getServerGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
        {
            PlayerWearables cap = ThutWearables.getWearables(player);
            ItemStack bag = ItemStack.EMPTY;
            if (bag.isEmpty()) bag = player.getHeldItemMainhand();
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) bag = player.getHeldItemOffhand();
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) bag = cap.getWearable(EnumWearable.BACK);
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) return null;
            return ContainerBag.makeContainer(bag, player);
        }

        @Override
        public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
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
        public Object getClientGuiElement(int ID, PlayerEntity player, World world, int x, int y, int z)
        {
            PlayerWearables cap = ThutWearables.getWearables(player);
            ItemStack bag = ItemStack.EMPTY;
            if (bag.isEmpty()) bag = player.getHeldItemMainhand();
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) bag = player.getHeldItemOffhand();
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) bag = cap.getWearable(EnumWearable.BACK);
            if (bag.isEmpty() || !(bag.getItem() instanceof ItemBling)) return null;
            return thut.bling.gui.GuiBag.createGui(ContainerBag.makeContainer(bag, player), player);
        }

        @Override
        public void preInit(FMLCommonSetupEvent event)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @Override
        public PlayerEntity getClientPlayer()
        {
            return Minecraft.getInstance().player;
        }

        @Override
        public IThreadListener getMainThreadListener()
        {
            if (super.getMainThreadListener() == null) { return Minecraft.getInstance(); }
            return super.getMainThreadListener();
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
            if (stack.hasTag() && stack.getTag().hasKey("model"))
            {
                String model = stack.getTag().getString("model");
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
            if (stack.hasTag() && stack.getTag().hasKey("tex"))
            {
                String tex = stack.getTag().getString("tex");
                textures = customTextures.get(tex);
                if (textures == null)
                {
                    textures = new ResourceLocation[2];
                    textures[0] = new ResourceLocation(tex);
                    if (stack.getTag().hasKey("tex2"))
                    {
                        textures[1] = new ResourceLocation(stack.getTag().getString("tex2"));
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
        public void renderWearable(EnumWearable slot, LivingEntity wearer, ItemStack stack, float partialTicks)
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

        private void renderAnkle(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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

        private void renderBack(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            if (!(model instanceof IModelCustom)) return;
            IModelCustom renderable = (IModelCustom) model;

            EnumDyeColor ret;
            Color colour;
            int[] col;

            ResourceLocation[] tex = textures.clone();
            Minecraft minecraft = Minecraft.getInstance();
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
            if (stack.hasTag() && stack.getTag().hasKey("dyeColour"))
            {
                int damage = stack.getTag().getInteger("dyeColour");
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

        private void renderEar(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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

        private void renderEye(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            // TODO eye by model instead of texture.
            GlStateManager.pushMatrix();
            Minecraft.getInstance().renderEngine.bindTexture(new ResourceLocation(MODID, "textures/items/eye.png"));
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

        private void renderFinger(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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

        private void renderHat(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
                int brightness)
        {
            if (!(model instanceof IModelCustom)) return;
            IModelCustom renderable = (IModelCustom) model;

            EnumDyeColor ret;
            Color colour;
            int[] col;

            ResourceLocation[] tex = textures.clone();
            Minecraft minecraft = Minecraft.getInstance();
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
            if (stack.hasTag() && stack.getTag().hasKey("dyeColour"))
            {
                int damage = stack.getTag().getInteger("dyeColour");
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

        private void renderNeck(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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
            if (stack.hasTag() && stack.getTag().hasKey("gem"))
            {
                tex[0] = new ResourceLocation(stack.getTag().getString("gem"));
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
            if (stack.hasTag() && stack.getTag().hasKey("dyeColour"))
            {
                int damage = stack.getTag().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            colour = new Color(ret.getColorValue() + 0xFF000000);
            col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
            IExtendedModelPart part = model.getParts().get(colorpart);
            if (part != null)
            {
                part.setRGBAB(col);
                Minecraft.getInstance().renderEngine.bindTexture(tex[1]);
                GlStateManager.scale(1, 1, .1);
                renderable.renderPart(colorpart);
            }
            GL11.glColor3f(1, 1, 1);
            part = model.getParts().get(itempart);
            if (part != null && tex[0] != null)
            {
                Minecraft.getInstance().renderEngine.bindTexture(tex[0]);
                GlStateManager.scale(1, 1, 10);
                GlStateManager.translate(0, 0.01, -0.075);
                renderable.renderPart(itempart);
            }
            GL11.glPopMatrix();
        }

        private void renderWaist(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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

        private void renderWrist(LivingEntity wearer, ItemStack stack, IModel model, ResourceLocation[] textures,
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
            if (stack.hasTag() && stack.getTag().hasKey("dyeColour"))
            {
                int damage = stack.getTag().getInteger("dyeColour");
                ret = EnumDyeColor.byDyeDamage(damage);
            }
            Color colour = new Color(ret.getColorValue() + 0xFF000000);
            int[] col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
            IExtendedModelPart part = model.getParts().get(colorpart);

            if (stack.hasTag() && stack.getTag().hasKey("gem"))
            {
                tex[0] = new ResourceLocation(stack.getTag().getString("gem"));
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
                Minecraft.getInstance().renderEngine.bindTexture(tex[1]);
                renderable.renderPart(colorpart);
            }
            GL11.glColor3f(1, 1, 1);
            part = model.getParts().get(itempart);
            if (part != null && tex[0] != null)
            {
                Minecraft.getInstance().renderEngine.bindTexture(tex[0]);
                renderable.renderPart(itempart);
            }
            GL11.glPopMatrix();
        }
    }
}
