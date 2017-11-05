package com.gendeathrow.playerskins.client.renderer;



import com.gendeathrow.playerskins.client.model.renderer.PlayerSkinModel;
import com.gendeathrow.playerskins.entity.EntityPlayerMob;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityPlayerSkinRenderer extends RenderBiped<EntityPlayerMob> 
{
	private final ModelBiped defaultModel;
	    
	private boolean flag = false;
	    
	public EntityPlayerSkinRenderer(RenderManager renderManager)
	{
		super(renderManager, new PlayerSkinModel(0F), 0.5F);
		
		this.defaultModel = this.getMainModel();
		this.addLayer(new LayerBipedArmor(this));
		this.addLayer(new LayerHeldItem(this));
		this.addLayer(new LayerArrow(this));

	}
	    
	    /**
	     * Renders the desired {@code T} type Entity.
	     */
	    
	public PlayerSkinModel getMainModel()
	{
		return (PlayerSkinModel)super.getMainModel();
	}
	
	@Override
	protected ResourceLocation getEntityTexture(EntityPlayerMob entity) 
	{
		return entity.getLocationSkin();
	}
	
	@Override
	public void doRender(EntityPlayerMob entity, double x, double y, double z, float entityYaw, float partialTicks)
	{
            GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	    		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	        GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
	}

	    
	/**
	 * Allows the render to do state modifications necessary before the model is rendered.
	 */
	@Override
	protected void preRenderCallback(EntityPlayerMob entitylivingbaseIn, float partialTickTime)
	{
		super.preRenderCallback(entitylivingbaseIn, partialTickTime);
	}
		
}
