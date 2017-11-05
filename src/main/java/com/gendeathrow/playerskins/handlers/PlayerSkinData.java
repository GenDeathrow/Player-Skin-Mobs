package com.gendeathrow.playerskins.handlers;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.WeightedRandom;

public class PlayerSkinData extends WeightedRandom.Item
{
	private GameProfile gameProfile;
	private boolean hasUpdated = false;
	//private int weight;
	
	public PlayerSkinData(GameProfile profileIn, int weightIn)
	{
		super(weightIn);
		this.gameProfile = profileIn;
	}
	
	public boolean hasUpdatedProfile() {
		return hasUpdated;
	}
	
	public void setProfileUpdated() {
		hasUpdated = true;
	}
	
	public String getOwnerName()
	{
		
		return this.gameProfile.getName(); 
	}


	public GameProfile getProfile()
	{
		return this.gameProfile;
	}

	public void setProfile(GameProfile profile)
	{
		gameProfile = profile;
	}
}
