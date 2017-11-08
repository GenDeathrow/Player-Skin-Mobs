package com.gendeathrow.playerskins.core.init;

import java.util.ArrayList;
import java.util.List;

import com.gendeathrow.playerskins.client.renderer.EntityPlayerSkinRenderer;
import com.gendeathrow.playerskins.core.ConfigHandler;
import com.gendeathrow.playerskins.core.PlayerSkinsCore;
import com.gendeathrow.playerskins.entity.EntityPlayerMob;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.common.DungeonHooks;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RegisterEntities {
	
	static int nextID = 1;
	
	public static void register() {
	  	EntityRegistry.registerModEntity(new ResourceLocation(PlayerSkinsCore.MODID, "playermob"), EntityPlayerMob.class, "playermob", 1, PlayerSkinsCore.instance, 80, 3, true, -3971048, 15677239);
	}
	
	@SideOnly(Side.CLIENT)
	public static void RegisterRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityPlayerMob.class, EntityPlayerSkinRenderer::new);
	}
	
	public static void RegisterSpawners() {
	  	
      	DungeonHooks.addDungeonMob(new ResourceLocation(PlayerSkinsCore.MODID ,"playermob"), ConfigHandler.playerSpawnerWeight); 
	}
	
	public static void RegisterSpawns() {
	   	List<Biome> biomes = new ArrayList<Biome>();
    	int blacklisted = 0;
    	
    	
	   	for (Biome biomeEntry : ForgeRegistries.BIOMES.getValues()) 
	   	{
	   		if(ConfigHandler.biomeBlackList.contains(biomeEntry.getRegistryName().toString())) {
	   			blacklisted++;
	   			continue;
	   		}
	   		
			for (Object obj : biomeEntry.getSpawnableList(EnumCreatureType.MONSTER))
				if (obj instanceof SpawnListEntry) {
					SpawnListEntry entry = (SpawnListEntry) obj;
					if (entry.entityClass == EntityZombie.class) {
						biomes.add(biomeEntry);
					}
				}
	   	}
     	
	   	PlayerSkinsCore.logger.info("Added "+ biomes.size() +" biomes to Players Skin spawn list.");
	   	if(blacklisted > 0 ) PlayerSkinsCore.logger.info(blacklisted +" biomes were blacklisted");
	   	
	   	EntityRegistry.addSpawn(EntityPlayerMob.class, ConfigHandler.playersSpawnWeight, 1, ConfigHandler.playersMaxGroupSpawn,EnumCreatureType.MONSTER, biomes.toArray(new Biome[biomes.size()]));
	}

}
