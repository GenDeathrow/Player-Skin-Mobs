package com.gendeathrow.playerskins.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Level;

import com.gendeathrow.playerskins.handlers.PlayerManager;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ConfigHandler 
{

	public static File configDir = new File("config/"+PlayerSkinsCore.MODID);
	
	public static Configuration config;
	
	private static ConfigCategory remove = new ConfigCategory("remove");
	
	private static String mobsCat = "Spawn Controls";
	private static String spawnerCat = "Dungeon Spawners";
	
	public static ArrayList<String> biomeBlackList;
	public static int[] diminsionBlackList = new int[]{1,-1};
	public static String[] whitelists;
	public static boolean spawnNether;
	public static boolean spawnEnd;
	public static int playerSpawnerWeight;
	public static int playersMaxGroupSpawn;
	public static int playersSpawnWeight;
	public static int cacheTime;
	public static float childSpawn;
	public static boolean spawnersEnabled;
	

	public static void preInit()
	{
		File file = new File(configDir, "settings.cfg");
		
		config = new Configuration(file);
		
		config.load();
		
	}

	public static void load()
	{
		PlayerSkinsCore.logger.log(Level.INFO, "Loading Configs...");
		
		whitelists = config.getStringList("WhiteLists", Configuration.CATEGORY_GENERAL, new String[0], "Meant for Twitch/Other sub whitelist. # One whitelist link per line. \n Example: http://whitelist.twitchapps.com/list.php?id=12345 ");
		cacheTime = config.getInt("Skins Cache Time", Configuration.CATEGORY_GENERAL, 2000, 1000, Integer.MAX_VALUE, "Changes the amount of time between grabbing each skin. \n If your not downloading all skins or getting 'too many request errors' \n Try increasing your cache time. Time is in Miliseconds 1000 = 1 Second"); 
						
		playersSpawnWeight = config.getInt("SpawnWeight", mobsCat, 10, 1, Integer.MAX_VALUE, "Weight of PlayerMobs spawning");
		playersMaxGroupSpawn = config.getInt("MaxSpawnGroup", mobsCat, 2, 1, 20, "Max Spawn group size");
		

		childSpawn = config.getFloat("Baby Spawn Chance", mobsCat, net.minecraftforge.common.ForgeModContainer.zombieBabyChance, 0.01f, 1f, "Chance of Spawning a child player skined mob");
		
		biomeBlackList = new ArrayList<String>(Arrays.asList(config.getStringList("Biome BlackList", mobsCat, new String[]{} , "Add biomes to a blacklist to prevent spawning in that specifc biome. \n Use biomes names Example: \n ForestHills \n Birch Forest")));

		Property dimBlackListProp = config.get(mobsCat, "Dimension BlackList", diminsionBlackList);
		dimBlackListProp.setComment("Blacklist certain dimensions (ID) from spawning player skinned mobs.");

		diminsionBlackList = dimBlackListProp.getIntList();
	
		spawnersEnabled = config.getBoolean("Enable Spawners", spawnerCat, true, "Will add Player Skins to the dungeons spawners List");
		playerSpawnerWeight = config.getInt("Mob Spawner Weight", spawnerCat, 200, 1, Integer.MAX_VALUE, "Changes dungeon spawner weight for PlayerMobs. \n Example is zombies are 200, where skeletons are 100. \n");
		
		
		removeProperties();
		if(config.hasChanged())
			config.save();
	}
	
	public static void PostLoad()
	{
		PlayerManager.readPlayerSkinFile();
	}
	

	private static void removeProperties()
	{
		config.moveProperty(mobsCat, "Enable Nether", remove.getName());
		config.moveProperty(mobsCat, "Enable The End", remove.getName());
		config.removeCategory(remove);
	}
	
	
}
