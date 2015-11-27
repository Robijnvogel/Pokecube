package pokecube.compat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import pokecube.compat.ai.AIElectricalInterferance;
import pokecube.compat.ai.AITendPlants;
import pokecube.compat.ai.AIThermalInteferance;
import pokecube.compat.blocks.rf.BlockSiphon;
import pokecube.compat.blocks.rf.TileEntitySiphon;
import pokecube.compat.galacticraft.GCCompat;
import pokecube.compat.mfr.MFRCompat;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.events.PostPostInit;
import pokecube.core.events.SpawnEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

@Mod(modid = "pokecube_compat", name = "Pokecube Compat", version = "1.0")
public class Compat 
{
	ThaumcraftCompat tccompat;
	ThaumiumPokecube thaumiumpokecube;
	GCCompat gccompat;
	Config conf;
	public static String CUSTOMSPAWNSFILE;
	
	public Compat()
	{
		gccompat = new GCCompat();
		MinecraftForge.EVENT_BUS.register(gccompat);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		doMetastuff();
		MinecraftForge.EVENT_BUS.register(this);
		setSpawnsFile(evt);
		
		Block b = new BlockSiphon().setCreativeTab(PokecubeMod.creativeTabPokecubeBlocks).setUnlocalizedName("pokesiphon");
		PokecubeItems.register(b, "pokesiphon");
		GameRegistry.registerTileEntity(TileEntitySiphon.class, "pokesiphon");
		if(evt.getSide()==Side.CLIENT)
		{
			PokecubeItems.registerItemTexture(Item.getItemFromBlock(b), 0, new ModelResourceLocation("pokecube_compat:pokesiphon", "inventory"));
		}
		
		Database.addSpawnData(CUSTOMSPAWNSFILE);
		conf = new Config(evt);

//		ReComplexCompat.register();
		if(Loader.isModLoaded("Thaumcraft")){
			thaumiumpokecube = new ThaumiumPokecube();
			thaumiumpokecube.addThaumiumPokecube();
		}
	}

	@EventHandler
	public void load(FMLInitializationEvent evt) 
	{
		if(Loader.isModLoaded("Thaumcraft")){
			tccompat = new ThaumcraftCompat();
			MinecraftForge.EVENT_BUS.register(tccompat);
		}
		new IGWSupportNotifier();

		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		if(Loader.isModLoaded("IGWMod"))
		{
			try
			{
				Class<?> registry = Class.forName("igwmod.api.WikiRegistry");
				Class<?> tabClass = Class.forName("igwmod.gui.tabs.IWikiTab");
				if (registry != null)
				{
					Method registerTab = registry.getMethod("registerWikiTab", tabClass);
					registerTab.invoke(registry, new PokecubeWikiTab());
				}
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
			}
		}
	}
	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		conf.postInit();

		GameRegistry.addRecipe(new ShapedOreRecipe(PokecubeItems.getBlock("pokesiphon"), new Object[] { 
			"RrR",
			"rCr",
			"RrR", 'R', Blocks.redstone_block, 'C', PokecubeItems.getBlock("cloner"), 'r', Items.redstone }));
	}
    
    @Optional.Method(modid = "AS_Ruins")
    @EventHandler
    public void AS_RuinsCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("AS_Ruins Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.RuinsCompat());
    }
    
    @Optional.Method(modid = "DynamicLights")
    @EventHandler
    public void AS_DLCompat(FMLPostInitializationEvent evt)
    {
        System.out.println("DynamicLights Compat");
        MinecraftForge.EVENT_BUS.register(new pokecube.compat.atomicstryker.DynamicLightsCompat());
    }
	
	@SubscribeEvent
	public void postPostInit(PostPostInit evt) 
	{
		gccompat.register();
//		MFRCompat.register();
		boolean wikiWrite = false;
		
		if(wikiWrite)
		{
			WikiWriter.writeWiki();
		}
	}
	
	@SubscribeEvent
	public void pokemobSpawnCheck(SpawnEvent.Spawn evt)
	{
		int id = evt.world.provider.getDimensionId();
		for(int i: Config.dimensionBlackList)
		{
			if(i==id)
			{
				evt.setCanceled(true);
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void entityConstruct(EntityJoinWorldEvent evt)
	{
		if(evt.entity instanceof IPokemob && evt.entity instanceof EntityLiving)
		{
			EntityLiving living = (EntityLiving) evt.entity;
			
			living.tasks.addTask(1, new AIElectricalInterferance((IPokemob) living));
			living.tasks.addTask(1, new AIThermalInteferance((IPokemob) living));
			living.tasks.addTask(1, new AITendPlants(living));
		}
	}

	private void doMetastuff()
	{
		ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
		
		meta.parent = PokecubeMod.ID;
	}
	
    public static void setSpawnsFile(FMLPreInitializationEvent evt)
    {
    	File file = evt.getSuggestedConfigurationFile();
    	String seperator = System.getProperty("file.separator");
    	
    	String folder = file.getAbsolutePath();
    	String name = file.getName();
    	folder = folder.replace(name, "pokecube"+seperator+ "compat"+seperator+"spawns.csv");
    	
    	CUSTOMSPAWNSFILE = folder;
    	writeDefaultConfig();
		return;
    }
    private static PrintWriter out;
    private static FileWriter fwriter;
    
    static String header = "Name,Special Cases,Biomes - any acceptable,Biomes - all needed,Excluded biomes,Replace";
    static String example1 = "Rattata,day night ,mound 0.6:10:5,,,false";
    static String example2 = "Spearow,day night ,plains 0.3;hills 0.3,,,true";
	private static void writeDefaultConfig()
	{
		try {
			File temp = new File(CUSTOMSPAWNSFILE.replace("spawns.csv", "")); 
			if(!temp.exists())
			{
				temp.mkdirs();
			}
			File temp1 = new File(CUSTOMSPAWNSFILE);
			if(temp1.exists())
			{
				return;
			}
			
			fwriter = new FileWriter(CUSTOMSPAWNSFILE);
	     	out = new PrintWriter(fwriter);
	        out.println(header);
	        out.println(example1);
	        out.println(example2);
			
			out.close();
			fwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
}
