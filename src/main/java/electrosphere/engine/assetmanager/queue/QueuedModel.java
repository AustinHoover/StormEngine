package electrosphere.engine.assetmanager.queue;

import java.util.concurrent.Callable;

import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.model.Model;

/**
 * A model that is queued to be loaded
 */
public class QueuedModel implements QueuedAsset<Model> {

    /**
     * true if loaded
     */
    boolean hasLoaded = false;

    /**
     * The model that will be loaded
     */
    Model model;

    /**
     * The runnable to invoke to actually load the model
     */
    Callable<Model> loadFunc;

    /**
     * The path promised
     */
    String promisedPath;


    /**
     * Creates the queued texture object
     * @param image the image to load to gpu
     */
    public QueuedModel(Callable<Model> loadFunc){
        this.loadFunc = loadFunc;
    }

    @Override
    public void load() {
        if(loadFunc != null){
            try {
                this.model = loadFunc.call();
            } catch (Exception e) {
                LoggerInterface.loggerEngine.ERROR(e);
            }
        }
        hasLoaded = true;
    }

    @Override
    public boolean hasLoaded() {
        return hasLoaded;
    }

    @Override
    public String getPromisedPath(){
        return promisedPath;
    }

    /**
     * Gets the model from this queued item
     * @return The model
     */
    public Model getModel(){
        return model;
    }

    @Override
    public void setPromisedPath(String promisedPath) {
        this.promisedPath = promisedPath;
    }
    
    @Override
    public Model get(){
        return model;
    }

    @Override
    public boolean suppliedPath() {
        return false;
    }

}
