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
	
	
	
	private static void addNewPlayer(String ownerName, int weight, ItemStack... stacks)
	{
		
		if(!playersList.containsKey(ownerName)){
			 PlayerSkinData playerskin = new PlayerSkinData(new GameProfile(null, ownerName), weight);
			 
			playersList.put(ownerName, playerskin);
			 
			 ArrayList<LootItem> drops = new ArrayList<LootItem>();
			 
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

	public static void loadDefaults()
	{
		//cool kids
		//playersList.put("Gen_Deathrow", new PlayerSkinData(new GameProfile(UUID.fromString("4412cc00-65de-43ff-b19a-10e0ec64cc4a"), "Gen_Deathrow"), 10));

		ItemStack cookie = new ItemStack(Items.COOKIE);
		if(cookie.getTagCompound() == null) cookie.setTagCompound(new NBTTagCompound());
		NBTTagCompound lore = new NBTTagCompound();
		NBTTagList lore2 = new NBTTagList();
		lore2.appendTag(new NBTTagString("Hear Have a cookie for your troubles"));
		lore2.appendTag(new NBTTagString("Your Friend,"));
		lore2.appendTag(new NBTTagString("GenDeathrow"));
		lore.setTag("Lore", lore2);
		cookie.getTagCompound().setTag("display", lore);

		addNewPlayer("Gen_Deathrow", 10, cookie);
		
		playersList.put("Funwayguy", new PlayerSkinData(new GameProfile(UUID.fromString("c9ecb54c-6f87-485d-b0e1-0e7f8c777d56"), "Funwayguy"), 10));
		playersList.put("Darkosto", new PlayerSkinData(new GameProfile(UUID.fromString("10755ea6-9721-467a-8b5c-92adf689072c"), "Darkosto"), 10));
		playersList.put("Kashdeya", new PlayerSkinData(new GameProfile(UUID.fromString("e49c3c38-a516-4252-ba19-c2b24ff39987"), "Kashdeya"), 10));
		playersList.put("TheMattaBase", new PlayerSkinData(new GameProfile(UUID.fromString("44ba40ef-fd8a-446f-834b-5aea42119c92"), "TheMattaBase"), 10));
		playersList.put("Jsl7", new PlayerSkinData(new GameProfile(UUID.fromString("94c04938-0f86-4960-a175-e3413a07fe8b"), "Jsl7"), 10));
		playersList.put("Turkey2349", new PlayerSkinData(new GameProfile(UUID.fromString("276130dd-8c9a-4814-8328-2578f034e422"), "Turkey2349"), 10));
		
		//invasion pack testers
		playersList.put("Bacon_Donut", new PlayerSkinData(new GameProfile(UUID.fromString("024a0d05-3e3e-4ec5-b394-6e1a22d23fdc"), "Bacon_Donut"), 10));
		playersList.put("SlothMonster_", new PlayerSkinData(new GameProfile(UUID.fromString("d2f772cb-80a4-47bd-820d-94b24bb3cccb"), "SlothMonster_"), 10));
		playersList.put("GWSheridan", new PlayerSkinData(new GameProfile(UUID.fromString("84680660-1372-4890-9935-88272138173d"), "GWSheridan"), 10));
		playersList.put("DatFailGamur", new PlayerSkinData(new GameProfile(UUID.fromString("29fa9b6c-8eb5-4544-87fb-5be8effbcf70"), "DatFailGamur"), 10));
		playersList.put("darkphan", new PlayerSkinData(new GameProfile(UUID.fromString("cf1f2cfc-1a85-40a6-aaf4-a17355ac6579"), "darkphan"), 10));
		playersList.put("SinfulDeity", new PlayerSkinData(new GameProfile(UUID.fromString("2ca3e953-c572-4c68-99b4-9d950fe7f580"), "SinfulDeity"), 10));
		playersList.put("Gooderness", new PlayerSkinData(new GameProfile(UUID.fromString("de6721e7-23b4-42ec-95c0-e4c976c7fa85"), "Gooderness"), 10));
		playersList.put("Vash505", new PlayerSkinData(new GameProfile(UUID.fromString("89215ee6-ae53-4d53-b524-86da50000a8f"), "Vash505"), 10));
		
		//forge
		playersList.put("LexManos",  new PlayerSkinData(new GameProfile(UUID.fromString("d3cf097a-438f-4523-b770-ec11e13ecc32"), "LexManos"), 10));
		playersList.put("cpw11",  new PlayerSkinData(new GameProfile(UUID.fromString("59af7399-5544-4990-81f1-c8f2263b00e5"), "cpw11"), 10));
		
		//modders
		playersList.put("ganymedes01",  new PlayerSkinData(new GameProfile(UUID.fromString("539c3716-ce9a-4ba5-9721-310e755abe5c"), "ganymedes01"), 10));
		playersList.put("iChun",  new PlayerSkinData(new GameProfile(UUID.fromString("0b7509f0-2458-4160-9ce1-2772b9a45ac2"), "iChun"), 10));
		
		//youtubers
		playersList.put("direwolf20",  new PlayerSkinData(new GameProfile(UUID.fromString("bbb87dbe-690f-4205-bdc5-72ffb8ebc29d"), "direwolf20"), 10));
		
		//ftb
		playersList.put("tfox83",  new PlayerSkinData(new GameProfile(UUID.fromString("0e205074-25d8-4703-b989-8323b1a35faa"), "tfox83"), 10));
		playersList.put("slowpoke101",  new PlayerSkinData(new GameProfile(UUID.fromString("d2839efc-727a-4263-97ce-3c73cdee5013"), "slowpoke101"), 10));
		
		//mojang
		playersList.put("Notch",  new PlayerSkinData(new GameProfile(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "Notch"), 10));
		playersList.put("jeb_",  new PlayerSkinData(new GameProfile(UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"), "jeb_"), 10));
		playersList.put("EvilSeph",  new PlayerSkinData(new GameProfile(UUID.fromString("020242a1-7b94-4179-9eff-511eea1221da"), "EvilSeph"), 10));
		playersList.put("C418",  new PlayerSkinData(new GameProfile(UUID.fromString("0b8b2245-8018-456c-945d-4282121e1b1e"), "C418"), 10));
		//raidersList.put("Dinnerbone",  new RaiderData(new GameProfile(null, "Dinnerbone"), 10));
		playersList.put("carnalizer",  new PlayerSkinData(new GameProfile(UUID.fromString("f96f3d63-fc7f-46a7-9643-86eb0b3d66cb"), "carnalizer"), 10));
		//raidersList.put("Grumm",  new RaiderData(new GameProfile(null, "Grumm"), 10));
	}



}
