package electrosphere.data.voxel.sampler;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import electrosphere.server.physics.terrain.generation.noise.NoiseContainer;
import electrosphere.server.physics.terrain.generation.noise.NoiseSampler;
import electrosphere.server.physics.terrain.generation.noise.operators.NoiseOperoatorInvoke;
import electrosphere.util.FileUtils;

/**
 * A sampler definition file
 */
public class SamplerFile {
    
    /**
     * The name of this sampler type
     */
    String name;

    /**
     * The sample to pull from
     */
    NoiseSampler sampler;

    /**
     * Gets the name of the file
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the sampler for this file
     * @return The sampler
     */
    public NoiseSampler getSampler() {
        return sampler;
    }

    /**
     * Gets the sampler definition files
     * @return The list of sampler definition files
     */
    public static List<SamplerFile> readSamplerDefinitionFiles(String initialPath){
        File initialDirectory = FileUtils.getAssetFile(initialPath);
        List<SamplerFile> rVal = new LinkedList<SamplerFile>();

        //read the files in
        SamplerFile.recursivelyReadSamplerDefinitionFiles(initialDirectory, rVal);

        //link invoke targets
        for(SamplerFile file : rVal){
            SamplerFile.linkInvokeTargets(rVal, file.getSampler());
        }

        return rVal;
    }
    
    /**
     * Recursively reads from the root all sampler files
     * @param rootDir The root dir
     * @param appendTarget The list to add sampler files to
     */
    private static void recursivelyReadSamplerDefinitionFiles(File rootDir, List<SamplerFile> appendTarget){
        if(rootDir == null || !rootDir.isDirectory()){
            throw new Error("Invalid path provided! " + rootDir.getAbsolutePath());
        }
        if(rootDir == null || !rootDir.exists() || rootDir.listFiles() == null){
            return;
        }
        for(File child : rootDir.listFiles()){
            if(child.isDirectory()){
                SamplerFile.recursivelyReadSamplerDefinitionFiles(child, appendTarget);
            } else {
                SamplerFile file = FileUtils.loadObjectFromFile(child, SamplerFile.class);
                appendTarget.add(file);
            }
        }
    }

    /**
     * Links all invoke targets to their respective samplers
     * @param files The list of all sampler files
     * @param current The current sampler function to evaluate
     */
    private static void linkInvokeTargets(List<SamplerFile> files, NoiseSampler current){
        if(current instanceof NoiseOperoatorInvoke){
            NoiseOperoatorInvoke invoker = (NoiseOperoatorInvoke)current;
            String target = invoker.getTarget();
            if(target.equals(current.getName())){
                throw new Error("Invoke module pointing at itself!");
            }
            for(SamplerFile file : files){
                if(file.getName().equals(target)){
                    invoker.setInvokeSampler(file.getSampler());
                    break;
                }
            }
            if(invoker.getInvokeSampler() == null){
                throw new Error("Failed to link " + target);
            }
        } else if(current instanceof NoiseContainer){
            for(NoiseSampler child : ((NoiseContainer)current).getChildren()){
                SamplerFile.linkInvokeTargets(files, child);
            }
        }
    }

}
