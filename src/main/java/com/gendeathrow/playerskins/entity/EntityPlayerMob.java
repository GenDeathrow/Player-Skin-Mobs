package com.gendeathrow.playerskins.entity;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.gendeathrow.playerskins.client.PlayerSkinManager;
import com.gendeathrow.playerskins.core.ConfigHandler;
import com.gendeathrow.playerskins.data.PlayerSkinData;
import com.gendeathrow.playerskins.handlers.PlayerManager;
import com.gendeathrow.playerskins.handlers.SpecialLootManager;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityPlayerMob extends EntityMob{

	protected GameProfile playerProfile;
	
    public boolean profileset = false;

    private static final UUID BABY_SPEED_BOOST_ID = UUID.fromString("B9766B59-9577-4402-BA4F-2EE2A276D836");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, "Baby speed boost", 0.5D, 1);
  
    
    private static final DataParameter<String> SKIN_VARIANT = EntityDataManager.<String>createKey(EntityPlayerMob.class, DataSerializers.STRING);
    private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityZombie.class, DataSerializers.BOOLEAN);
    private final EntityAIBreakDoor breakDoor = new EntityAIBreakDoor(this);
  
    private boolean isBreakDoorsTaskSet;
    /** The width of the entity */
    private float playerWidth = -1.0F;
    /** The height of the the entity. */
    private float playerHeight;
	    
	
	public EntityPlayerMob(World worldIn){
		super(worldIn);
		this.setSize(0.6F, 1.95F);
	}
	
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, false));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.applyEntityAI();
    }

    protected void applyEntityAI()
    {
        this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[] {EntityPigZombie.class}));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityVillager>(this, EntityVillager.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityIronGolem>(this, EntityIronGolem.class, true));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0D);
    }

	    
	@Override
	protected void entityInit()
	{
		super.entityInit();
        this.getDataManager().register(IS_CHILD, Boolean.valueOf(false));
		this.getDataManager().register(SKIN_VARIANT, "Steve");
	}

    public boolean isBreakDoorsTaskSet()
    {
        return this.isBreakDoorsTaskSet;
    }

    /**
     * Sets or removes EntityAIBreakDoor task
     */
    public void setBreakDoorsAItask(boolean enabled)
    {
        if (this.isBreakDoorsTaskSet != enabled)
        {
            this.isBreakDoorsTaskSet = enabled;
            ((PathNavigateGround)this.getNavigator()).setBreakDoors(enabled);

            if (enabled)
            {
                this.tasks.addTask(1, this.breakDoor);
            }
            else
            {
                this.tasks.removeTask(this.breakDoor);
            }
        }
    }
    
    public boolean isChild()
    {
        return ((Boolean)this.getDataManager().get(IS_CHILD)).booleanValue();
    }
    
    /**
     * Get the experience points the entity currently has.
     */
    @Override
    protected int getExperiencePoints(EntityPlayer player)
    {
        if (this.isChild())
        {
            this.experienceValue = (int)((float)this.experienceValue * 2.5F);
        }

        return super.getExperiencePoints(player);
    }

    /**
     * Set whether this zombie is a child.
     */
    public void setChild(boolean childZombie)
    {
        this.getDataManager().set(IS_CHILD, Boolean.valueOf(childZombie));

        if (this.world != null && !this.world.isRemote)
        {
            IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            iattributeinstance.removeModifier(BABY_SPEED_BOOST);

            if (childZombie)
            {
                iattributeinstance.applyModifier(BABY_SPEED_BOOST);
            }
        }

        this.setChildSize(childZombie);
    }

    public void notifyDataManagerChange(DataParameter<?> key)
    {
        if (IS_CHILD.equals(key))
        {
            this.setChildSize(this.isChild());
        }

        super.notifyDataManagerChange(key);
    }

    public GameProfile getPlayerProfile(){
		return PlayerManager.getAddPlayerSkinProfile(this.getOwner());
    }
   
    // Handle Skins
    public ResourceLocation getLocationSkin(){
		return PlayerSkinManager.DownloadPlayersSkin(this);
    }
    
    /** 
     * Set raiders skin owner
     * @param name
     */
    public void setOwner(String name){
        this.dataManager.set(SKIN_VARIANT, name);
    }
    
    /**
     * Get raiders skin owner
     * @return
     */
	public String getOwner(){
    	return this.dataManager.get(SKIN_VARIANT);
    }
	
	 @Nullable
	    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
	    {
	        livingdata = super.onInitialSpawn(difficulty, livingdata);
	        float f = difficulty.getClampedAdditionalDifficulty();
	        this.setCanPickUpLoot(this.rand.nextFloat() < 0.55F * f);
	        
	        PlayerSkinData skin = EntityPlayerMob.getRandomPlayerSkin();
	        		
	        this.setOwner(skin.getOwnerName());
	        this.setCustomNameTag(skin.getOwnerName());

	        if (livingdata == null)
	        {
	            livingdata = new EntityPlayerMob.GroupData(this.world.rand.nextFloat() < ConfigHandler.childSpawn);
	        }

	        if (livingdata instanceof EntityPlayerMob.GroupData)
	        {
	        	EntityPlayerMob.GroupData entityplayermob$groupdata = (EntityPlayerMob.GroupData)livingdata;

	            if (entityplayermob$groupdata.isChild)
	            {
	                this.setChild(true);

	                if ((double)this.world.rand.nextFloat() < 0.05D)
	                {
	                    List<EntityChicken> list = this.world.<EntityChicken>getEntitiesWithinAABB(EntityChicken.class, this.getEntityBoundingBox().grow(5.0D, 3.0D, 5.0D), EntitySelectors.IS_STANDALONE);

	                    if (!list.isEmpty())
	                    {
	                        EntityChicken entitychicken = list.get(0);
	                        entitychicken.setChickenJockey(true);
	                        this.startRiding(entitychicken);
	                    }
	                }
	                else if ((double)this.world.rand.nextFloat() < 0.05D)
	                {
	                    EntityChicken entitychicken1 = new EntityChicken(this.world);
	                    entitychicken1.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F);
	                    entitychicken1.onInitialSpawn(difficulty, (IEntityLivingData)null);
	                    entitychicken1.setChickenJockey(true);
	                    this.world.spawnEntity(entitychicken1);
	                    this.startRiding(entitychicken1);
	                }
	            }
	        }

	        this.setBreakDoorsAItask(this.rand.nextFloat() < f * 0.1F);
	        this.setEquipmentBasedOnDifficulty(difficulty);
	        this.setEnchantmentBasedOnDifficulty(difficulty);

	        if (this.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
	        {
	            Calendar calendar = this.world.getCurrentDate();

	            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.rand.nextFloat() < 0.25F)
	            {
	                this.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(this.rand.nextFloat() < 0.1F ? Blocks.LIT_PUMPKIN : Blocks.PUMPKIN));
	                this.inventoryArmorDropChances[EntityEquipmentSlot.HEAD.getIndex()] = 0.0F;
	            }
	        }

	        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).applyModifier(new AttributeModifier("Random spawn bonus", this.rand.nextDouble() * 0.05000000074505806D, 0));
	        double d0 = this.rand.nextDouble() * 1.5D * (double)f;

	        if (d0 > 1.0D)
	        {
	            this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).applyModifier(new AttributeModifier("Random zombie-spawn bonus", d0, 2));
	        }

	        if (this.rand.nextFloat() < f * 0.05F)
	        {
	            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("Leader zombie bonus", this.rand.nextDouble() * 3.0D + 1.0D, 2));
	            this.setBreakDoorsAItask(true);
	        }

	        return livingdata;
	    }

	
	
    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("Owner"))
            this.setOwner(compound.getString("Owner"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound){
    	
        super.writeEntityToNBT(compound);
        
        compound.setString("Owner", this.getOwner());
        

        if (compound.getBoolean("IsBaby"))
        {
            this.setChild(true);
        }

        this.setBreakDoorsAItask(compound.getBoolean("CanBreakDoors"));
  
        
        if (this.isChild())
        {
            compound.setBoolean("IsBaby", true);
        }

        compound.setBoolean("CanBreakDoors", this.isBreakDoorsTaskSet());

    }
  
    
    public float getEyeHeight()
    {
        float f = 1.74F;

        if (this.isChild())
        {
            f = (float)((double)f - 0.81D);
        }

        return f;
    }

    /**
     * sets the size of the entity to be half of its current size if true.
     */
    public void setChildSize(boolean isChild)
    {
        this.multiplySize(isChild ? 0.5F : 1.0F);
    }

    /**
     * Sets the width and height of the entity.
     */
    protected final void setSize(float width, float height)
    {
        boolean flag = this.playerWidth > 0.0F && this.playerHeight > 0.0F;
        this.playerWidth = width;
        this.playerHeight = height;

        if (!flag)
        {
            this.multiplySize(1.0F);
        }
    }

    /**
     * Multiplies the height and width by the provided float.
     */
    protected final void multiplySize(float size)
    {
        super.setSize(this.playerWidth * size, this.playerHeight * size);
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getYOffset()
    {
        return this.isChild() ? 0.0D : -0.45D;
    }

    
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty)
    {
        super.setEquipmentBasedOnDifficulty(difficulty);

        if (this.rand.nextFloat() < (this.world.getDifficulty() == EnumDifficulty.HARD ? 0.05F : 0.01F))
        {
            int i = this.rand.nextInt(3);

            if (i == 0)
            {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            else
            {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }
    
    public boolean attackEntityAsMob(Entity entityIn)
    {
        boolean flag = super.attackEntityAsMob(entityIn);

        if (flag) {
            float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();

            if (this.getHeldItemMainhand().isEmpty() && this.isBurning() && this.rand.nextFloat() < f * 0.3F) {
                entityIn.setFire(2 * (int)f);
            }
        }
        return flag;
    }
    
    protected boolean canEquipItem(ItemStack stack) {
        return stack.getItem() == Items.EGG && this.isChild() && this.isRiding() ? false : super.canEquipItem(stack);
    }
    
    @Override
	public void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source)
	{
		if(source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayer)
		{
			double dropit = this.rand.nextDouble();
			
			if( dropit < (.025)) //lootingModifier*0.025 + 
			{
				ItemStack stack = new ItemStack(Items.SKULL, 1, 3);
				
				if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setString("SkullOwner", this.getOwner());

				EntityItem skull = new EntityItem(world, this.posX, this.posY, this.posZ, stack);
				
				this.world.spawnEntity(skull);
			}
		}
		
		if(SpecialLootManager.hasLoot(this.getOwner())) {
			SpecialLootManager.DropLoot(this, this.getOwner());
			
		}
			

		super.dropLoot(wasRecentlyHit, lootingModifier, source);
	}
	
    
//    //TODO
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
		return SoundEvents.ENTITY_PLAYER_HURT;
	}

    @Override
	protected SoundEvent getDeathSound(){
		return SoundEvents.ENTITY_PLAYER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn){
		this.playSound(SoundEvents.ENTITY_ZOMBIE_STEP, 0.15F, 1.0F);
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute(){
		return EnumCreatureAttribute.UNDEFINED;
	}
	
	/**
	 *  This gets a random GameProfile from registered raider player skins. 
	 */
	protected static PlayerSkinData getRandomPlayerSkin(){
		PlayerSkinData profile;
			 profile = PlayerManager.getRandomPlayerSkin();
		return profile;
	}

	 class GroupData implements IEntityLivingData
	    {
	        public boolean isChild;

	        private GroupData(boolean p_i47328_2_)
	        {
	            this.isChild = p_i47328_2_;
	        }
	    }
}
