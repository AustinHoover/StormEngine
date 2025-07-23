package electrosphere.data.entity.creature;

/**
 * Data about the first person view model for this creature type
 */
public class ViewModelData {
    
    //How far from the origin of the creature to place the viewmodel
    double heightFromOrigin;

    //How far to pull the view model below the camera
    double cameraViewDirOffsetY;

    //How far to pull the view model behind the camera
    double cameraViewDirOffsetZ;

    //the actual model
    String firstPersonModelPath;


    /**
     * How far from the origin of the creature to place the viewmodel
     * @return
     */
    public double getHeightFromOrigin(){
        return heightFromOrigin;
    }

    /**
     * How far to pull the view model below the camera
     * @return
     */
    public double getCameraViewDirOffsetY(){
        return cameraViewDirOffsetY;
    }

    /**
     * How far to pull the view model behind the camera
     * @return
     */
    public double getCameraViewDirOffsetZ(){
        return cameraViewDirOffsetZ;
    }

    /**
     * the actual model
     * @return
     */
    public String getFirstPersonModelPath(){
        return firstPersonModelPath;
    }


}
