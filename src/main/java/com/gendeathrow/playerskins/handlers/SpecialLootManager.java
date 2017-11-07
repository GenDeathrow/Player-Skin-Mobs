package com.gendeathrow.playerskins.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import com.gendeathrow.playerskins.data.LootItem;
import com.gendeathrow.playerskins.data.PlayerSkinData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;

public class SpecialLootManager {
	
	public static final HashMap<PlayerSkinData, ArrayList<LootItem>>LOOTTABLES = new HashMap<PlayerSkinData, ArrayList<LootItem>>();
	
	
	
	public static boolean addNewSpecial(PlayerSkinData playerSkin, ArrayList<LootItem> drops) {
		if(drops.size() > 0 && !LOOTTABLES.containsKey(playerSkin))
			LOOTTABLES.put(playerSkin, drops);
		return true;
	}
	
	

	public static boolean hasLoot(String owner) {
		if(PlayerManager.getPlayerSkinProfile(owner) != null && LOOTTABLES.containsKey(PlayerManager.getPlayerSkinProfile(owner))) {
			return true;
		}
		return false;
	}
	
	public static void DropLoot(Entity entity, String owner) {
		
		if(!hasLoot(owner))
			return;
		
		ArrayList<LootItem> drops = LOOTTABLES.get(PlayerManager.getPlayerSkinProfile(owner));
		
		if(drops != null)
		for(LootItem drop : drops) {
			if(drop.shouldDrop(entity.world.rand)) {
				ItemStack stack = drop.getStack(entity.world.rand);
				if(!stack.isEmpty()) {
					entity.entityDropItem(stack, 0);
				}
			}
		}
	}
	
	public static void clearLootTable() {
		LOOTTABLES.clear();
	}
	
	public static void readJsonItemDrops(PlayerSkinData playerSkin, JsonObject json) {
		if(!json.has("specialDrops"))
			return;
		
		ArrayList<LootItem> drops = new ArrayList<LootItem>();
		
		for(JsonElement item : json.getAsJsonArray("specialDrops")) {
			LootItem loot = new LootItem();
			try	{
				drops.add(loot.readJsonObject(item.getAsJsonObject()));
			}
			catch(NumberFormatException e){
				e.printStackTrace();
			}
		}

		addNewSpecial(playerSkin, drops);
	}
	
	
	public static void writeJsonItemDrops(PlayerSkinData playerSkin, JsonObject json) {
		
		if(LOOTTABLES.containsKey(playerSkin)) {
			
			JsonArray itemArray = new JsonArray();
			
			for(LootItem item : LOOTTABLES.get(playerSkin))	{
				JsonObject itemJson = new JsonObject();
				item.writeJsonObject(itemJson);
				itemArray.add(itemJson);
			}
			
			if(itemArray.size() > 0)
				json.add("specialDrops", itemArray);
			
		}
		
	}

}

