package com.gendeathrow.playerskins.handlers;

import com.gendeathrow.playerskins.core.ConfigHandler;
import com.gendeathrow.playerskins.entity.EntityPlayerMob;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class EventHandler 
{

	@SubscribeEvent
	public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
		if (event.getEntityLiving() instanceof EntityPlayerMob) {
			World world = event.getWorld();
			DimensionType dimType = world.provider.getDimensionType();
			if (isDimensionBlackListed(dimType.getId())) {
				event.setResult(Result.DENY);
			}
				
		}
	}	
	
	private static boolean isDimensionBlackListed(int dimensionId) {
		for (int id : ConfigHandler.diminsionBlackList)
			if (dimensionId == id)
				return true;
		return false;
	}
	
}
