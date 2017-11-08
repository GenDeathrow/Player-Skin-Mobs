package com.gendeathrow.playerskins.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.gendeathrow.playerskins.core.ConfigHandler;
import com.gendeathrow.playerskins.core.PlayerSkinsCore;
import com.gendeathrow.playerskins.data.LootItem;
import com.gendeathrow.playerskins.data.PlayerSkinData;
import com.gendeathrow.playerskins.utils.Tools;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.WeightedRandom;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class PlayerManager 
{
	public static final HashMap<String, PlayerSkinData> playersList = new HashMap<String, PlayerSkinData>();
	
	public static final File playerSkinFile = new File(ConfigHandler.configDir, "skins.json");
	
	public static final File whiteListFolder = new File(PlayerSkinsCore.MODID+"Whitelist");
	
	public static final Random rand = new Random();
	
	private static boolean markDirty = false;
	
	
	public static void markDirty()
	{
		markDirty = true;
	}
	
	@SubscribeEvent
	public static void WorldSave(WorldEvent.Save event)
	{
		if(event.getWorld().isRemote) return;
			Save();
	}
	
	// ServerSide call
	public static PlayerSkinData getRandomPlayerSkin()
	{
		return (PlayerSkinData)WeightedRandom.getRandomItem(rand, getWeightedList());
	}
	
	private static List<PlayerSkinData> getWeightedList()
	{
		return (List<PlayerSkinData>) Lists.newArrayList(playersList.values());
	}
	
	// Common call / clientside mainly
	public static GameProfile getAddPlayerSkinProfile(String ownerName)
	{
		if(playersList.containsKey(ownerName))
		{
			return playersList.get(ownerName).getProfile();
		}
		else
		{
			playersList.put(ownerName,  new PlayerSkinData(new GameProfile(null, ownerName), 10));
			
			return playersList.get(ownerName).getProfile();
		}
	}
	
	
	
	public static PlayerSkinData getPlayerSkinProfile(String ownerName) {
		return playersList.get(ownerName);
	}
	
	
	public static PlayerSkinData getPlayerSkinProfile(GameProfile playerProfile) {
		return playersList.get(playerProfile.getName());
	}
	
	
	public static HashMap<String, PlayerSkinData> getAllRaiders()
	{
		return playersList;
	}
	
	public static void setPlayerSkinProfile(String ownerName, GameProfile newProfile)
	{
		if(playersList.containsKey(ownerName))
		{
			playersList.get(ownerName).setProfile(newProfile);
		}
	}
	
	public static void addNewPlayerSkin(String ownerName, int weight)
	{
		
		if(!playersList.containsKey(ownerName))
		{
			playersList.put(ownerName,  new PlayerSkinData(new GameProfile(null, ownerName), weight));
			markDirty = true;
		}
	}
	
	
	private static void addNewPlayer(String ownerName, int weight)
	{
		addNewPlayer(ownerName, weight, null);
	}
	
	private static void addNewPlayer(String ownerName, int weight, ItemStack... stacks)
	{
		
		if(!playersList.containsKey(ownerName)){
			 PlayerSkinData playerskin = new PlayerSkinData(new GameProfile(null, ownerName), weight);
			 
			playersList.put(ownerName, playerskin);
			 
			 ArrayList<LootItem> drops = new ArrayList<LootItem>();
			 
			 if(stacks != null)
				 for(ItemStack stack : stacks)
					 if(stack != null && !stack.isEmpty())
						 drops.add(new LootItem(stack));
			 
			 
			 if(!drops.isEmpty())
				 SpecialLootManager.addNewSpecial(playerskin, drops);
		}
	}
	

	public static void removePlayerSkin(String ownerName)
	{
		playersList.remove(ownerName);
		markDirty = true;
	}

	public static void Save()
	{
		if(markDirty)
		{
			savePlayerSkinFile();
			markDirty = false;
		}
	}
	
	public static void readPlayerSkinFile()
	{
		 getTwitchSubscribers();
		 
	        if (playerSkinFile.isFile())
	        {
	            try
	            {
	                playersList.clear();
	                SpecialLootManager.clearLootTable();
	                
	                playersList.putAll(parseJson(FileUtils.readFileToString(playerSkinFile)));
                
	                
	                getTwitchSubscribers();
	            }
	            catch (IOException ioexception)
	            {
	            	PlayerSkinsCore.logger.error((String)("Couldn\'t read Player Skins file " + playerSkinFile), (Throwable)ioexception);
	            }
	            catch (JsonParseException jsonparseexception)
	            {
	            	PlayerSkinsCore.logger.error((String)("Couldn\'t parse Player Skin file " + playerSkinFile), (Throwable)jsonparseexception);
	            }
	        }
	        else {
	        	loadDefaults();
	        	savePlayerSkinFile();
	        }
	}

	public static void savePlayerSkinFile()
	{
	    	FileOutputStream fo = null; 
	        try
	        {
	        	fo = FileUtils.openOutputStream(playerSkinFile);
	        			
	            String json = new GsonBuilder().setPrettyPrinting().create().toJson(dumpJson(playersList));
	        	   
	            FileUtils.writeStringToFile(playerSkinFile, json);
	            
	            fo.close(); 
	        }
	        catch (IOException ioexception)
	        {
	        	PlayerSkinsCore.logger.error((String)"Couldn\'t save stats", (Throwable)ioexception);
	        }finally 
	        {
	        	IOUtils.closeQuietly(fo);
	        }
	}

	    
	    
	public static Map<String, PlayerSkinData> parseJson(String p_150881_1_)
	{
	        JsonElement jsonelement = (new JsonParser()).parse(p_150881_1_);

	        if (!jsonelement.isJsonObject())
	        {
	            return Maps.<String, PlayerSkinData>newHashMap();
	        }
	        else
	        {
	            JsonObject jsonobject = jsonelement.getAsJsonObject();
	            Map<String, PlayerSkinData> map = Maps.<String, PlayerSkinData>newHashMap();

	            for (Entry<String, JsonElement> entry : jsonobject.entrySet())
	            {
	                String playerOwner = (String)entry.getKey();

	                if (playerOwner != null)
	                {
	                	JsonObject playerJson = entry.getValue().getAsJsonObject();
	                	
	                	int weight = 10;
	                	
	                	UUID uuid = null;
	                	GameProfile playerProfile;
	                	
	                	if(playerJson.has("weight"))
	                		weight = playerJson.get("weight").getAsInt();
	                	
	                	playerJson.remove("uuid");

                		playerProfile = new GameProfile(null, playerOwner);
	                	
	                	PlayerSkinData skinData = new PlayerSkinData(playerProfile, weight);
	                	
	                	if(!map.containsKey(playerOwner)) {
	                		map.put(playerOwner, skinData);
	                		SpecialLootManager.readJsonItemDrops(skinData, playerJson);
	                	}
	                	else
	                		PlayerSkinsCore.logger.warn("Player Skin already exist in " + playerSkinFile + ":" + (String)entry.getKey());
	                }
	                else
	                {
	                	PlayerSkinsCore.logger.warn("Invalid Player Skin in " + playerSkinFile + ": Don\'t know what " + (String)entry.getKey() + " is");
	                }
	            }

	            return map;
	        }
	}
	    
	    
	public static JsonObject dumpJson(Map<String, PlayerSkinData> p_150880_0_)
	{
	        JsonObject jsonobject = new JsonObject();

	        for (Entry<String, PlayerSkinData> entry : p_150880_0_.entrySet())
	        {
	            if (((PlayerSkinData)entry.getValue()) != null)
	            {
	                JsonObject jsonobject1 = new JsonObject();
	                
//	                if(entry.getValue().getProfile().getId() != null)
//	                	jsonobject1.addProperty("uuid", entry.getValue().getProfile().getId().toString());
	                
	                jsonobject1.addProperty("weight", (Number)Integer.valueOf(entry.getValue().itemWeight));
	               
	                SpecialLootManager.writeJsonItemDrops(((PlayerSkinData)entry.getValue()), jsonobject1);
	                
	                jsonobject.add(((String)entry.getKey()), jsonobject1);
	            }
	            else
	            {
	                jsonobject.addProperty(((String)entry.getKey()), (Number)Integer.valueOf(10));
	            }
	        }

	        
	        return jsonobject;
	}
	
	public static void getTwitchSubscribers()
	{
		getTwitchSubscribers(false);
	}
	
	public static void getTwitchSubscribers(boolean force)
	{
		
		whiteListFolder.mkdirs();

		for(String list : ConfigHandler.whitelists)
		{
		
			// First check to see if you have updated Twitch Subs yet
			File subs = new File(whiteListFolder, list.replaceAll("\\W+", "") +".txt");

			if(!subs.exists() || force) {
				try {
					Tools.DownloadFile(list, subs.getPath());
				} catch (IOException e)	{
					e.printStackTrace();
				}
			}else if(subs.exists()) {
				try {
					parseTwitchSubsWhiteList(subs);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private static void parseTwitchSubsWhiteList(File file) throws IOException
	{
		FileReader input = new FileReader(file);
		
		@SuppressWarnings("resource")
		BufferedReader bufRead = new BufferedReader(input);
		String myLine = null;

		try {
			while ( (myLine = bufRead.readLine()) != null)
			{    
			    String[] array2 = myLine.split("\n");
			    for (int i = 0; i < array2.length; i++)
			    	addNewPlayerSkin(array2[i],10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static ItemStack addLore(ItemStack stack, String... strings) {
		
		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound lore = new NBTTagCompound();
		NBTTagList lore2 = new NBTTagList();
		
		for(String string : strings)
			lore2.appendTag(new NBTTagString(string));

		lore.setTag("Lore", lore2);
		stack.getTagCompound().setTag("display", lore);
		
		return stack;
	}
	
	public static void loadDefaults()
	{
		addNewPlayer("Gen_Deathrow", 10, addLore(new ItemStack(Items.COOKIE), "Here have a cookie for your troubles.",  "Your Friend,", "   GenDeathrow"));
		addNewPlayer("Funwayguy", 10);
		addNewPlayer("Kashdeya", 10);
		addNewPlayer("TheMattaBase", 10);
		addNewPlayer("Jsl7", 10);
		addNewPlayer("Turkey2349", 10);
		
		//invasion pack testers
		addNewPlayer("GWSheridan", 10);
		addNewPlayer("DatFailGamur", 10);
		addNewPlayer("darkphan", 10);
		addNewPlayer("SinfulDeity", 10);
		
		//Twitch Streamers
		addNewPlayer("Bacon_Donut", 10);
		addNewPlayer("SlothMonster_", 10);
		addNewPlayer("Gooderness", 10);
		addNewPlayer("Vash505", 10);
		addNewPlayer("Darkosto", 10, new ItemStack(Items.STICK).setStackDisplayName("Builders Wand"));
		addNewPlayer("Sevadus", 10);
		addNewPlayer("CrustyMustard", 10);
		addNewPlayer("Wyld", 10);
		addNewPlayer("GiantWaffle", 10);
		addNewPlayer("Soaryn", 10);
		addNewPlayer("ZeldoKavira", 10);
		
		//forge
		addNewPlayer("LexManos", 10);
		addNewPlayer("cpw11", 10);
		
		//modders
		addNewPlayer("ganymedes01", 10);
		addNewPlayer("iChun", 10);
		addNewPlayer("KingLemming", 10);
		addNewPlayer("ProfMobius", 10);
		addNewPlayer("Sacheverell", 10);
		addNewPlayer("Pahimar", 10);
		addNewPlayer("Vazkii", 10);
		addNewPlayer("FatherToast", 10);
		addNewPlayer("vadis365", 10);
		addNewPlayer("PurpleMentat", 10);
		
		//youtubers
		addNewPlayer("direwolf20", 10);
		addNewPlayer("ChimneySwift", 10);
		addNewPlayer("Sjin", 10);
		addNewPlayer("Xephos", 10);
		addNewPlayer("CaptainSparklez", 10);
		addNewPlayer("DanTDM", 10);
		addNewPlayer("Etho", 10);
		addNewPlayer("SethBling", 10);
		addNewPlayer("WayofFlowingTime", 10);
		

		
		
		//pack dev
		addNewPlayer("Kehaan", 10);

		
		
		
		//ftb
		addNewPlayer("tfox83", 10);
		addNewPlayer("slowpoke101", 10);
		
		//mojang
		addNewPlayer("Notch", 10);
		addNewPlayer("jeb_", 10);
		addNewPlayer("EvilSeph", 10);
		addNewPlayer("C418", 10);
		addNewPlayer("Dinnerbone", 10);
		addNewPlayer("carnalizer", 10);
		addNewPlayer("Grumm", 10);
		addNewPlayer("Searge_DP", 10);
		addNewPlayer("TheMogMiner", 10);
	}



}
