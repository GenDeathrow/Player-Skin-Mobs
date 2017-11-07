package com.gendeathrow.playerskins.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.gendeathrow.playerskins.core.ConfigHandler;
import com.gendeathrow.playerskins.core.PlayerSkinsCore;
import com.gendeathrow.playerskins.data.PlayerSkinData;
import com.gendeathrow.playerskins.entity.EntityPlayerMob;
import com.gendeathrow.playerskins.handlers.PlayerManager;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class PlayerSkinManager 
{
	public static PlayerProfileCache profileCache;
	public static MinecraftSessionService sessionService;
	
	public final HashMap<String, ResourceLocation> cachedSkins = new HashMap<String, ResourceLocation>();
	
	public static final PlayerSkinManager INSTANCE = new PlayerSkinManager();
	
	private static Thread thread2;

	private static List<PlayerSkinData> raidersdata = new ArrayList<PlayerSkinData>();	
	
	/**
	 *  Run thur the Raiders list and Cache the Skin's localy
	 *  
	 */
	public static void cacheSkins()
	{
		Iterator<PlayerSkinData> raidersList = PlayerManager.getAllRaiders().values().iterator();
		
		while(raidersList.hasNext())
			updateProfile(raidersList.next());
	}
	
	
	/**
	 * 
	 * @param raiderInfo
	 */
	public static void updateProfile(PlayerSkinData raiderInfo) {

		if(raiderInfo == null) return;
		
		if(!raidersdata.contains(raiderInfo)) raidersdata.add(raiderInfo);

		if (thread2 == null || thread2.getState() == Thread.State.TERMINATED) {
			thread2 = new Thread(new Runnable() 
			{

				@Override
				public void run() {
					
					while (!raidersdata.isEmpty()) 
					{
						PlayerSkinData raider = raidersdata.get(0);

						boolean uuidWasEmpty = false;
						
//						if(raider.getProfile().getId() == null)
//							uuidWasEmpty = true;
						
						raider.setProfile(TileEntitySkull.updateGameprofile(raider.getProfile()));
						
//						// If UUID was empty than make raiders to update
//						if(uuidWasEmpty && raider.getProfile().getId() != null)
//							PlayerManager.markDirty();

						raider.setProfileUpdated();
						
						try 
						{
							//System.out.println("Getting Profile "+ raider.getOwnerName() +" - "+ raider.getProfile().getId().toString());
							Thread.sleep(ConfigHandler.cacheTime);
							
						} catch (InterruptedException e) 
						{
							e.printStackTrace();
						}

						
						raidersdata.remove(0);
					}
				}
			});

			thread2.start();
		}
	}

	private static List<String> badraiders = new ArrayList<String>();
	public static void addToBadList(String owner) 
	{
		if(!badraiders.contains(owner)) {
			badraiders.add(owner);
			PlayerSkinsCore.logger.error("Could not Get this players profile."+ owner +" Added to the Naughty List");
		}
	}
	
	
	/**
	 * Download players skin from mojang
	 * @return
	 */
	@SuppressWarnings(value = { "unchecked" })
    public static ResourceLocation DownloadPlayersSkin(EntityPlayerMob raider) {
		ResourceLocation resourcelocation =  DefaultPlayerSkin.getDefaultSkinLegacy();

		PlayerSkinData playerSkinProfile = PlayerManager.getPlayerSkinProfile(raider.getPlayerProfile());
		
		if (raider.getPlayerProfile() != null && playerSkinProfile != null)
		{
			if(playerSkinProfile.hasUpdatedProfile()) {
				Minecraft minecraft = Minecraft.getMinecraft();
				Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(raider.getPlayerProfile());

				if (map.containsKey(Type.SKIN)) {
					resourcelocation = minecraft.getSkinManager().loadSkin((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
				}
			}
			else {
				updateProfile(playerSkinProfile);
			}
		}
 		return resourcelocation;
    	
    }
	
	
    static boolean hasCheckedSkins = false;
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

    	if(!hasCheckedSkins){
    		hasCheckedSkins = true;
    		//cacheSkins();
    	}
    }
	
}
