package electrosphere.engine.loadingthreads;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import electrosphere.client.block.cells.BlockTextureAtlas;
import electrosphere.client.terrain.cells.VoxelTextureAtlas;
import electrosphere.data.block.BlockData;
import electrosphere.data.block.BlockType;
import electrosphere.data.entity.item.Item;
import electrosphere.data.voxel.VoxelData;
import electrosphere.data.voxel.VoxelType;
import electrosphere.engine.Globals;
import electrosphere.engine.assetmanager.AssetDataStrings;
import electrosphere.engine.assetmanager.queue.QueuedTexture;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.meshgen.GeometryModelGen;
import electrosphere.renderer.texture.TextureAtlas;
import electrosphere.util.FileUtils;

/**
 * The intention of this thread is to load basic assets that the engine should generally have available from the start.
 * Examples:
 *  - Texture Atlas for terrain
 *  - Icons for items
 */
public class InitialAssetLoading {

    /**
     * The queued atlas texture
     */
    static QueuedTexture atlasQueuedTexture = null;
    
    /**
     * Loads basic data
     */
    protected static void loadData(){
    
        InitialAssetLoading.loadVoxelTextureAtlas();
        InitialAssetLoading.loadBlockTextureAtlas();
        InitialAssetLoading.loadParticleAtlas();
        LoggerInterface.loggerEngine.INFO("Finished loading texture atlas");

    }

    /**
     * Loads the voxel texture atlas
     */
    private static void loadVoxelTextureAtlas(){
        //terrain texture atlas
        Globals.profiler.beginCpuSample("createVoxelTextureAtlas");
        VoxelData data = Globals.gameConfigCurrent.getVoxelData();
        int iterator = 0;
        BufferedImage image = new BufferedImage(VoxelTextureAtlas.ATLAS_DIM, VoxelTextureAtlas.ATLAS_DIM, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics = image.getGraphics();
        for(VoxelType type : data.getTypes()){
            if(type.getTexture() != null){
                int offX = iterator % VoxelTextureAtlas.ELEMENTS_PER_ROW;
                int offY = iterator / VoxelTextureAtlas.ELEMENTS_PER_ROW;
                try {
                    BufferedImage newType = ImageIO.read(FileUtils.getAssetFile(type.getTexture()));
                    int drawX = VoxelTextureAtlas.ATLAS_ELEMENT_DIM * offX;
                    int drawY = VoxelTextureAtlas.ATLAS_DIM - VoxelTextureAtlas.ATLAS_ELEMENT_DIM - VoxelTextureAtlas.ATLAS_ELEMENT_DIM * offY;
                    graphics.drawImage(newType, drawX, drawY, null);
                } catch (IOException e) {
                    LoggerInterface.loggerRenderer.ERROR("Texture atlas failed to find texture " + type.getTexture(), e);
                }
                //put coords in map
                Globals.voxelTextureAtlas.putTypeCoord(type.getId(),iterator);

                //iterate
                iterator++;
            }
        }
        Globals.profiler.endCpuSample();
        
        //queue to asset manager
        atlasQueuedTexture = QueuedTexture.createFromImage(image);
        Globals.assetManager.queuedAsset(atlasQueuedTexture);


        //wait the texture to be loaded
        while(!atlasQueuedTexture.hasLoaded()){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                LoggerInterface.loggerEngine.ERROR("failed to sleep", e);
            }
        }

        
        //construct texture atlas from buffered image
        Globals.voxelTextureAtlas.setSpecular(atlasQueuedTexture.getTexture());
        Globals.voxelTextureAtlas.setNormal(atlasQueuedTexture.getTexture());
    }

    /**
     * Loads the block texture atlas
     */
    private static void loadBlockTextureAtlas(){
        //terrain texture atlas
        Globals.profiler.beginCpuSample("createBlockTextureAtlas");
        BlockData data = Globals.gameConfigCurrent.getBlockData();
        int iterator = 0;
        BufferedImage image = new BufferedImage(BlockTextureAtlas.ATLAS_DIM, BlockTextureAtlas.ATLAS_DIM, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics graphics = image.getGraphics();
        for(BlockType type : data.getTypes()){
            if(type.getTexture() != null){
                int offX = iterator % BlockTextureAtlas.ELEMENTS_PER_ROW;
                int offY = iterator / BlockTextureAtlas.ELEMENTS_PER_ROW;
                try {
                    BufferedImage newType = ImageIO.read(FileUtils.getAssetFile(type.getTexture()));
                    int drawX = BlockTextureAtlas.ATLAS_ELEMENT_DIM * offX;
                    int drawY = BlockTextureAtlas.ATLAS_DIM - BlockTextureAtlas.ATLAS_ELEMENT_DIM - BlockTextureAtlas.ATLAS_ELEMENT_DIM * offY;
                    graphics.drawImage(newType, drawX, drawY, null);
                } catch (IOException e) {
                    LoggerInterface.loggerRenderer.ERROR("Texture atlas failed to find texture " + type.getTexture(), e);
                }
                //put coords in map
                Globals.blockTextureAtlas.putTypeCoord(type.getId(),iterator);

                //once the atlas has been created, need to check if items have already been created
                //if items have been created, must update the hard-coded shader values to reflect the actual atlas values
                if(Globals.gameConfigCurrent.getItemMap().getItem(type.getName()) != null){
                    Item item = Globals.gameConfigCurrent.getItemMap().getItem(type.getName());
                    item.getGraphicsTemplate().getModel().getUniforms().get(GeometryModelGen.MESH_NAME_BLOCK_SINGLE).put("blockAtlasIndex",iterator);
                }

                //iterate
                iterator++;
            }
        }
        Globals.profiler.endCpuSample();
        
        //queue to asset manager
        atlasQueuedTexture = QueuedTexture.createFromImage(AssetDataStrings.TEXTURE_BLOCK_ATLAS, image);
        Globals.assetManager.queuedAsset(atlasQueuedTexture);


        //wait the texture to be loaded
        while(!atlasQueuedTexture.hasLoaded()){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                LoggerInterface.loggerEngine.ERROR("failed to sleep", e);
            }
        }

        
        //construct texture atlas from buffered image
        Globals.blockTextureAtlas.setSpecular(atlasQueuedTexture.getTexture());
        Globals.blockTextureAtlas.setNormal(atlasQueuedTexture.getTexture());

        //update block item models based on atlas values
        for(BlockType blockType : Globals.gameConfigCurrent.getBlockData().getTypes()){
            String typeId = "block:" + blockType.getName();
            Item item = Globals.gameConfigCurrent.getItemMap().getItem(typeId);
            item.getGraphicsTemplate().getModel().getUniforms().get(GeometryModelGen.MESH_NAME_BLOCK_SINGLE).put("blockAtlasIndex",Globals.blockTextureAtlas.getVoxelTypeOffset(blockType.getId()));
        }
    }

    /**
     * Loads the particle texture atlas
     */
    private static void loadParticleAtlas(){
        //terrain texture atlas
        Globals.profiler.beginCpuSample("create particle texture atlas");
        int iterator = 0;
        BufferedImage image = null;
        TextureAtlas textureAtlas = new TextureAtlas();
        File particleTextureDirectory = FileUtils.getAssetFile("Textures/particles");
        File cachedAtlas = FileUtils.getCacheFile("particleAtlas.png");
        if(cachedAtlas.exists()){
            try {
                image = ImageIO.read(Files.newInputStream(cachedAtlas.toPath()));
                for(File childFile : particleTextureDirectory.listFiles()){
                    if(!childFile.isDirectory()){
                        //put coords in map
                        textureAtlas.putPathCoord(childFile.getName(),iterator);
    
                        //iterate
                        iterator++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Failed to load texture atlas from cache!");
            }
        } else {
            cachedAtlas.getParentFile().mkdirs();
            image = new BufferedImage(TextureAtlas.ATLAS_DIM, TextureAtlas.ATLAS_DIM, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics graphics = image.getGraphics();
            for(File childFile : particleTextureDirectory.listFiles()){
                if(!childFile.isDirectory()){
                    String texturePath = "Textures/particles/" + childFile.getName();
                    int offX = iterator % TextureAtlas.ELEMENTS_PER_ROW;
                    int offY = iterator / TextureAtlas.ELEMENTS_PER_ROW;
                    try {
                        BufferedImage newType = ImageIO.read(FileUtils.getAssetFile(texturePath));
                        int drawX = TextureAtlas.ATLAS_ELEMENT_DIM * offX;
                        int drawY = TextureAtlas.ATLAS_DIM - TextureAtlas.ATLAS_ELEMENT_DIM - TextureAtlas.ATLAS_ELEMENT_DIM * offY;
                        graphics.drawImage(newType, drawX, drawY, TextureAtlas.ATLAS_ELEMENT_DIM, TextureAtlas.ATLAS_ELEMENT_DIM, null);
                    } catch (IOException e) {
                        LoggerInterface.loggerRenderer.ERROR("Texture atlas failed to find texture " + texturePath, e);
                    }
                    //put coords in map
                    textureAtlas.putPathCoord(childFile.getName(),iterator);

                    //iterate
                    iterator++;
                }
            }
            try {
                ImageIO.write(image, "png", Files.newOutputStream(cachedAtlas.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Failed to save to cache!");
            }
        }
        Globals.profiler.endCpuSample();
        
        //queue to asset manager
        atlasQueuedTexture = QueuedTexture.createFromImage(image);
        Globals.assetManager.queuedAsset(atlasQueuedTexture);


        //wait the texture to be loaded
        while(!atlasQueuedTexture.hasLoaded()){
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                LoggerInterface.loggerEngine.ERROR("failed to sleep", e);
            }
        }

        //construct texture atlas from buffered image
        Globals.assetManager.registerTextureToPath(atlasQueuedTexture.getTexture(), AssetDataStrings.TEXTURE_PARTICLE);
        textureAtlas.setSpecular(atlasQueuedTexture.getTexture());
        textureAtlas.setNormal(atlasQueuedTexture.getTexture());

        Globals.particleService.setParticleTextureAtlas(textureAtlas);
    }

    /**
     * Gets the queued texture
     */
    protected static QueuedTexture getQueuedTexture(){
        return atlasQueuedTexture;
    }


}
