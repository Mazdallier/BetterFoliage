package mods.betterfoliage.client;

import java.util.Map;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.integration.CLCIntegration;
import mods.betterfoliage.client.integration.OptifineIntegration;
import mods.betterfoliage.client.integration.ShadersModIntegration;
import mods.betterfoliage.client.integration.TerraFirmaCraftIntegration;
import mods.betterfoliage.client.misc.BetterFoliageMetadataSection;
import mods.betterfoliage.client.misc.WindTracker;
import mods.betterfoliage.client.render.IRenderBlockDecorator;
import mods.betterfoliage.client.render.impl.EntityFXFallingLeaves;
import mods.betterfoliage.client.render.impl.EntityFXRisingSoul;
import mods.betterfoliage.client.render.impl.RenderBlockCactus;
import mods.betterfoliage.client.render.impl.RenderBlockDirtWithAlgae;
import mods.betterfoliage.client.render.impl.RenderBlockDirtWithGrassSide;
import mods.betterfoliage.client.render.impl.RenderBlockDirtWithGrassTop;
import mods.betterfoliage.client.render.impl.RenderBlockGrass;
import mods.betterfoliage.client.render.impl.RenderBlockLeaves;
import mods.betterfoliage.client.render.impl.RenderBlockLilypad;
import mods.betterfoliage.client.render.impl.RenderBlockLogs;
import mods.betterfoliage.client.render.impl.RenderBlockMycelium;
import mods.betterfoliage.client.render.impl.RenderBlockNetherrack;
import mods.betterfoliage.client.render.impl.RenderBlockReed;
import mods.betterfoliage.client.render.impl.RenderBlockSandWithCoral;
import mods.betterfoliage.client.texture.GrassTextures;
import mods.betterfoliage.client.texture.LeafTextures;
import mods.betterfoliage.client.texture.LeafTextureEnumerator;
import mods.betterfoliage.client.texture.SoulParticleTextures;
import mods.betterfoliage.client.texture.generator.LeafGenerator;
import mods.betterfoliage.client.texture.generator.ReedGenerator;
import mods.betterfoliage.client.texture.generator.ShortGrassGenerator;
import mods.betterfoliage.common.config.Config;
import mods.betterfoliage.loader.impl.CodeRefs;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.Maps;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

/** Client proxy class. Contains initialization and ASM-ed call hooks.
 * @author octarine-noise
 */
public class BetterFoliageClient {

	public static ResourceLocation missingTexture = new ResourceLocation("betterfoliage", "textures/blocks/missing_leaf.png");
	
	public static Map<Integer, IRenderBlockDecorator> decorators = Maps.newHashMap();
	public static LeafGenerator leafGenerator = new LeafGenerator();
	public static LeafTextures leafTextures = new LeafTextures();
	public static SoulParticleTextures soulParticles = new SoulParticleTextures();
	public static GrassTextures grassTextures = new GrassTextures();
	public static WindTracker wind = new WindTracker();
	
	public static void postInit() {
		FMLCommonHandler.instance().bus().register(new KeyHandler());
		FMLCommonHandler.instance().bus().register(new Config());
		
		BetterFoliage.log.info("Registering renderers");
		registerRenderer(new RenderBlockCactus());
		registerRenderer(new RenderBlockNetherrack());
		registerRenderer(new RenderBlockLilypad());
		registerRenderer(new RenderBlockMycelium());
		registerRenderer(new RenderBlockLeaves());
		registerRenderer(new RenderBlockGrass());
		registerRenderer(new RenderBlockReed());
		registerRenderer(new RenderBlockDirtWithAlgae());
		registerRenderer(new RenderBlockSandWithCoral());
		registerRenderer(new RenderBlockDirtWithGrassSide());
		registerRenderer(new RenderBlockDirtWithGrassTop());
		registerRenderer(new RenderBlockLogs());

		MinecraftForge.EVENT_BUS.register(wind);
		FMLCommonHandler.instance().bus().register(wind);
		
		MinecraftForge.EVENT_BUS.register(Config.leaves);
		MinecraftForge.EVENT_BUS.register(Config.crops);
		MinecraftForge.EVENT_BUS.register(Config.dirt);
		MinecraftForge.EVENT_BUS.register(Config.grass);
		MinecraftForge.EVENT_BUS.register(Config.logs);
		
		BetterFoliage.log.info("Registering texture generators");
		MinecraftForge.EVENT_BUS.register(soulParticles);
		MinecraftForge.EVENT_BUS.register(leafGenerator);
		MinecraftForge.EVENT_BUS.register(leafTextures);
		MinecraftForge.EVENT_BUS.register(grassTextures);
		MinecraftForge.EVENT_BUS.register(new LeafTextureEnumerator());
		
		MinecraftForge.EVENT_BUS.register(new ReedGenerator("bf_reed_bottom", missingTexture, true));
		MinecraftForge.EVENT_BUS.register(new ReedGenerator("bf_reed_top", missingTexture, false));
		MinecraftForge.EVENT_BUS.register(new ShortGrassGenerator("bf_shortgrass", missingTexture, false));
		MinecraftForge.EVENT_BUS.register(new ShortGrassGenerator("bf_shortgrass_snow", missingTexture, true));
		
		IMetadataSerializer serializer = CodeRefs.fMetadataSerializer.<IMetadataSerializer>getInstanceField(Minecraft.getMinecraft().getResourceManager());
		serializer.registerMetadataSectionType(new BetterFoliageMetadataSection.BetterFoliageMetadataSerializer(), BetterFoliageMetadataSection.class);
		
		ShadersModIntegration.init();
		TerraFirmaCraftIntegration.init();
		OptifineIntegration.init();
		CLCIntegration.init();
	}

	public static boolean isLeafTexture(TextureAtlasSprite icon) {
		String resourceLocation = icon.getIconName();
		if (resourceLocation.startsWith("forestry:leaves/")) return true;
		return false;
	}
	
	public static int getRenderTypeOverride(IBlockAccess blockAccess, int x, int y, int z, Block block, int original) {
		// universal sign for DON'T RENDER ME!
		if (original == -1) return original;
		
		for (Map.Entry<Integer, IRenderBlockDecorator> entry : decorators.entrySet())
			if (entry.getValue().isBlockAccepted(blockAccess, x, y, z, block, original))
				return entry.getKey();
		
		return original;
	}
	
	public static boolean shouldRenderBlockSideOverride(boolean original, IBlockAccess blockAccess, int x, int y, int z, int side) {
	    return original || (Config.logsEnabled && Config.logs.matchesID(blockAccess.getBlock(x, y, z)));
	}
	
	public static float getAmbientOcclusionLightValueOverride(float original, Block block) {
	    if (Config.logsEnabled && Config.logs.matchesID(block)) return 1.0f;
	    return original;
	}
	
    public static boolean getUseNeighborBrightnessOverride(boolean original, Block block) {
        return original || (Config.logsEnabled && Config.logs.matchesID(block));
    }
	   
	public static void onRandomDisplayTick(Block block, World world, int x, int y, int z) {
	    if (Config.soulFXEnabled) {
	        if (world.getBlock(x, y, z) == Blocks.soul_sand && Math.random() < Config.soulFXChance) {
	            Minecraft.getMinecraft().effectRenderer.addEffect(new EntityFXRisingSoul(world, x, y, z));
	            return;
	        }
	    }
	    if (Config.leafFXEnabled) {
	        if (Config.leaves.matchesID(block) && world.isAirBlock(x, y - 1, z) && Math.random() < Config.leafFXChance) {
	            new EntityFXFallingLeaves(world, x, y, z).addToRenderer(Minecraft.getMinecraft().effectRenderer);
	            return;
	        }
	    }
	}
	
	public static void registerRenderer(IRenderBlockDecorator decorator) {
		int renderId = RenderingRegistry.getNextAvailableRenderId();
		decorators.put(renderId, decorator);
		RenderingRegistry.registerBlockHandler(renderId, decorator);
		MinecraftForge.EVENT_BUS.register(decorator);
		decorator.init();
	}

}
