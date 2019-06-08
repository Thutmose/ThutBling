package thut.bling.network;

import java.io.IOException;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.api.distmarker.Dist;
import thut.bling.ThutBling;
import thut.bling.bag.ContainerBagLarge;
import thut.bling.bag.InventoryLarge;

public class PacketBag implements IMessage, IMessageHandler<PacketBag, IMessage>
{
    public static final byte SETPAGE = 0;
    public static final byte RENAME  = 1;
    public static final byte ONOPEN  = 2;
    public static final byte OPEN    = 3;

    public static void OpenBag(EntityPlayer playerIn)
    {
        InventoryLarge inv = InventoryLarge.getBag(playerIn);
        PacketBag packet = new PacketBag(PacketBag.ONOPEN);
        packet.data.setInteger("N", inv.boxes.length);
        packet.data.setInteger("S", InventoryLarge.PAGECOUNT);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet.data.setString("N" + i, inv.boxes[i]);
        }
        ThutBling.packetPipeline.sendTo(packet, (EntityPlayerMP) playerIn);
        for (int i = 0; i < inv.boxes.length; i++)
        {
            packet = new PacketBag(PacketBag.OPEN);
            packet.data = inv.serializeBox(i);
            ThutBling.packetPipeline.sendTo(packet, (EntityPlayerMP) playerIn);
        }
        playerIn.openGui(ThutBling.instance, 0, playerIn.getEntityWorld(), 0, 0, 0);
    }

    byte                  message;
    public NBTTagCompound data = new NBTTagCompound();

    public PacketBag()
    {
    }

    public PacketBag(byte message)
    {
        this.message = message;
    }

    @Override
    public IMessage onMessage(final PacketBag message, final MessageContext ctx)
    {
        ThutBling.proxy.getMainThreadListener().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx, message);
            }
        });
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        message = buf.readByte();
        PacketBuffer buffer = new PacketBuffer(buf);
        try
        {
            data = buffer.readCompoundTag();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(message);
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeCompoundTag(data);
    }

    void processMessage(MessageContext ctx, PacketBag message)
    {
        EntityPlayer player;
        if (ctx.side == Side.CLIENT)
        {
            player = ThutBling.proxy.getClientPlayer();
        }
        else
        {
            player = ctx.getServerHandler().player;
        }
        ContainerBagLarge container = null;
        if (player.openContainer instanceof ContainerBagLarge) container = (ContainerBagLarge) player.openContainer;

        if (message.message == SETPAGE)
        {
            if (container != null)
            {
                container.gotoInventoryPage(message.data.getInteger("P"));
            }
        }
        if (message.message == OPEN && ctx.side == Side.CLIENT)
        {
            InventoryLarge inv = InventoryLarge.getBag(player);
            inv.deserializeBox(message.data);
        }
        if (message.message == RENAME)
        {
            if (container != null)
            {
                String name = message.data.getString("N");
                container.changeName(name);
            }
        }
        if (message.message == ONOPEN)
        {
            InventoryLarge bag = InventoryLarge.getBag(player);
            int num = message.data.getInteger("N");
            InventoryLarge.PAGECOUNT = message.data.getInteger("S");
            bag.boxes = new String[num];
            for (int i = 0; i < bag.boxes.length; i++)
            {
                bag.boxes[i] = message.data.getString("N" + i);
            }
        }
    }
}
