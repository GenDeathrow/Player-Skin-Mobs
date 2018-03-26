package com.gendeathrow.playerskins.core;

import com.gendeathrow.playerskins.core.init.RegisterEntities;
import com.gendeathrow.playerskins.core.proxies.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = PlayerSkinsCore.MODID, name=PlayerSkinsCore.NAME, version = PlayerSkinsCore.VERSION, dependencies="after:BiomesOPlenty", acceptedMinecraftVersions="[1.12.2]")
public class PlayerSkinsCore
{
    public static final String MODID = "playerskins";
    public static final String NAME = "Player Skins";
    public static final String VERSION = "1.0.4";
    public static final String CHANNELNAME = "genplayerskins";
    
	@Instance(MODID)
	public static PlayerSkinsCore instance;

	public static final String PROXY = "com.gendeathrow.playerskins.core.proxies";
	    
	public static org.apache.logging.log4j.Logger logger;
	    
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	
	public static SimpleNetworkWrapper network;
	
    @EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
		logger = event.getModLog();
		
		ConfigHandler.preInit();
		
    	RegisterEntities.register();
    	
    	proxy.registerHandlers();
     	
       	PlayerSkinsCore.network = NetworkRegistry.INSTANCE.newSimpleChannel(PlayerSkinsCore.CHANNELNAME);
       	
    	proxy.preInit(event);
     }
    
   
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.Init(event);

    	proxy.registerHandlers();
    	
    	ConfigHandler.load();
    	
    	RegisterEntities.RegisterSpawns();
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event)
    {
    	proxy.postInit(event);
    	
    	ConfigHandler.PostLoad();
    	
    	RegisterEntities.RegisterSpawners();
    }
    
    
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
	}
}
