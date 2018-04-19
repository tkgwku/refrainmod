package org.oiran.jar.mod.mending;

import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = RefrainMod.MODID, name = RefrainMod.NAME, version = RefrainMod.VERSION)
public class RefrainMod
{
	public static final String MODID = "refrain";
	public static final String NAME = "Mending Refrain mod";
	public static final String VERSION = "1.0.1";

	private static Logger logger;
	private int durabilityDetectionConfig = 32;
	private int cooltime = 3;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		logger = event.getModLog();
		try {
			Configuration config = new Configuration(event.getSuggestedConfigurationFile());
			durabilityDetectionConfig = config.getInt("maxDetectingDurability", "main", 32, 1, 1560, "");
		} catch (Exception e) {}
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if (event.getSide().isClient()) MinecraftForge.EVENT_BUS.register(this);
	}

	@NetworkCheckHandler
	public boolean netCheckHandler(Map<String, String> mods, Side side)
	{
		return true;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (!event.phase.equals(Phase.END)) return;
		if (cooltime > 0) {
			cooltime--;
			return;
		}
		if (!Minecraft.getMinecraft().gameSettings.keyBindAttack.isKeyDown()) {
			return;
		}
		if (Minecraft.getMinecraft().player == null) {
			logger.info("nullnull");
			return;
		}
		if (Minecraft.getMinecraft().player.isCreative() || Minecraft.getMinecraft().player.isSpectator()) {
			return;
		}
		EntityPlayer player = Minecraft.getMinecraft().player;
		//InventoryPlayer inv = player.inventory;
		ItemStack heldItem = player.getHeldItemMainhand();
		if (heldItem.isEmpty() || heldItem.getItem() == null) return;
		if (heldItem.getItem() instanceof ItemPickaxe) {
			if (((ItemPickaxe)heldItem.getItem()).getToolMaterialName() == "DIAMOND") {
				if (heldItem.getMaxDamage() - heldItem.getItemDamage() < durabilityDetectionConfig) {
					/*int slotId = 40; // off hand slot id is 40, in default
					ItemStack stack = null;
					for (int i = 35; i >= 0; i--) {
						stack = inv.getStackInSlot(i);
						if (i != inv.currentItem
								&& stack.getItem() instanceof ItemPickaxe
								&& stack.getMaxDamage() - stack.getItemDamage() >= 16) {
							slotId = i;
							break;
						}
						if (i >= 9 && stack.isEmpty()) {
							slotId = i;
						}
					}*/
					NetHandlerPlayClient connect = ((EntityPlayerSP) player).connection;
					//connect.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, BlockPos.ORIGIN, EnumFacing.DOWN));
					connect.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
					//inv.markDirty();
					//event.setCanceled(true);
					cooltime = 3;
				}
			}
		}
	}
}
