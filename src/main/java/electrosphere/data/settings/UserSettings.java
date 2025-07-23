package electrosphere.data.settings;

import electrosphere.logger.LoggerInterface;
import electrosphere.util.FileUtils;

/**
 * User-defined settings
 */
public class UserSettings {
    
    
    //
    //Gameplay settings
    //
    boolean gameplayGenerateWorld;
    int gameplayPhysicsCellRadius;
    
    
    //
    //Display settings
    //
    int displayWidth;
    int displayHeight;
    boolean displayFullscreen;
    
    
    //
    //Graphics settings
    //
    //general
    float graphicsFOV;
    //performance
    int graphicsPerformanceLODChunkRadius;
    boolean graphicsPerformanceEnableVSync;
    boolean graphicsPerformanceDrawShadows;
    boolean graphicsPerformanceOIT;

    /**
     * Controls whether the foliage manager runs or not
     */
    boolean graphicsPerformanceEnableFoliageManager;

    //resolution to render at
    //this will be scaled to displayWidth/displayHeight after rendering
    int renderResolutionX;
    int renderResolutionY;
    //debug
    //debug visuals
    boolean graphicsDebugDrawCollisionSpheresClient;
    boolean graphicsDebugDrawCollisionSpheresServer;
    boolean graphicsDebugDrawPhysicsObjectsClient;
    boolean graphicsDebugDrawPhysicsObjectsServer;
    boolean graphicsDebugDrawMovementVectors;
    boolean graphicsDebugDrawNavmesh;
    boolean graphicsDebugDrawGridAlignment;
    boolean graphicsDebugDrawInteractionCollidables;
    boolean graphicsDebugDrawMacroColliders;
    boolean graphicsDebugDrawClientCellColliders;
    boolean graphicsDebugDrawServerCellColliders;
    boolean graphicsDebugDrawServerFacingVectors;
    //debug network
    boolean netRunNetMonitor;


    float graphicsViewRange;
    
    
    
    
    
    
    
    
    public boolean gameplayGenerateWorld() {
        return gameplayGenerateWorld;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public float getGraphicsFOV() {
        return graphicsFOV;
    }

    public boolean graphicsPerformanceEnableVSync() {
        return graphicsPerformanceEnableVSync;
    }

    public boolean graphicsPerformanceDrawShadows() {
        return graphicsPerformanceDrawShadows;
    }

    public boolean getGraphicsDebugDrawCollisionSpheresClient() {
        return graphicsDebugDrawCollisionSpheresClient;
    }

    public boolean getGraphicsDebugDrawCollisionSpheresServer() {
        return graphicsDebugDrawCollisionSpheresServer;
    }

    public boolean graphicsDebugDrawPhysicsObjectsClient() {
        return graphicsDebugDrawPhysicsObjectsClient;
    }

    public boolean graphicsDebugDrawPhysicsObjectsServer() {
        return graphicsDebugDrawPhysicsObjectsServer;
    }

    public boolean graphicsDebugDrawMovementVectors() {
        return graphicsDebugDrawMovementVectors;
    }

    public int getGameplayPhysicsCellRadius() {
        return gameplayPhysicsCellRadius;
    }

    public int getGraphicsPerformanceLODChunkRadius() {
        return graphicsPerformanceLODChunkRadius;
    }

    public float getGraphicsViewDistance(){
        return graphicsViewRange;
    }

    public boolean graphicsDebugDrawNavmesh() {
        return graphicsDebugDrawNavmesh;
    }

    public boolean displayFullscreen(){
        return displayFullscreen;
    }

    public int getRenderResolutionX(){
        return renderResolutionX;
    }

    public int getRenderResolutionY(){
        return renderResolutionY;
    }

    public boolean getGraphicsPerformanceOIT(){
        return graphicsPerformanceOIT;
    }

    public boolean getNetRunNetMonitor(){
        return netRunNetMonitor;
    }
    

    public void setGraphicsDebugDrawCollisionSpheresClient(boolean draw){
        this.graphicsDebugDrawCollisionSpheresClient = draw;
    }

    public void setGraphicsDebugDrawCollisionSpheresServer(boolean draw){
        this.graphicsDebugDrawCollisionSpheresServer = draw;
    }

    public void setGraphicsDebugDrawPhysicsObjectsClient(boolean draw){
        this.graphicsDebugDrawPhysicsObjectsClient = draw;
    }

    public void setGraphicsDebugDrawPhysicsObjectsServer(boolean draw){
        this.graphicsDebugDrawPhysicsObjectsServer = draw;
    }

    public void setGraphicsDebugDrawMovementVectors(boolean draw){
        this.graphicsDebugDrawMovementVectors = draw;
    }

    public void setGraphicsDebugDrawNavmesh(boolean draw){
        this.graphicsDebugDrawNavmesh = draw;
    }

    public void setGraphicsViewRange(float range){
        graphicsViewRange = range;
    }

    public void setDisplayFullscreen(boolean fullscreen){
        this.displayFullscreen = fullscreen;
    }
    
    
    /**
     * Checks if the foliage manager is enabled or not
     * @return true if enabled, false otherwise
     */
    public boolean getGraphicsPerformanceEnableFoliageManager() {
        return graphicsPerformanceEnableFoliageManager;
    }

    /**
     * Sets if the foliage manager is enabled or not
     * @param graphicsPerformanceEnableFoliageManager true if enabled, false otherwise
     */
    public void setGraphicsPerformanceEnableFoliageManager(boolean graphicsPerformanceEnableFoliageManager) {
        this.graphicsPerformanceEnableFoliageManager = graphicsPerformanceEnableFoliageManager;
    }
    
    /**
     * Checks if should render the grid alignment data
     * @return true if should render grid alignment data, false otherwise
     */
    public boolean getGraphicsDebugDrawGridAlignment() {
        return graphicsDebugDrawGridAlignment;
    }

    /**
     * Sets whether to draw the grid alignment data or not
     * @param graphicsDebugDrawGridAlignment true to render the data, false otherwise
     */
    public void setGraphicsDebugDrawGridAlignment(boolean graphicsDebugDrawGridAlignment) {
        this.graphicsDebugDrawGridAlignment = graphicsDebugDrawGridAlignment;
    }

    /**
     * Checs if should render the interaction engine collidables
     * @return true to render them, false otherwise
     */
    public boolean getGraphicsDebugDrawInteractionCollidables() {
        return graphicsDebugDrawInteractionCollidables;
    }

    /**
     * Sets whether should render the interaction engine collidables
     * @param graphicsDebugDrawInteractionCollidables true to render, false otherwise
     */
    public void setGraphicsDebugDrawInteractionCollidables(boolean graphicsDebugDrawInteractionCollidables) {
        this.graphicsDebugDrawInteractionCollidables = graphicsDebugDrawInteractionCollidables;
    }

    public boolean getGraphicsDebugDrawMacroColliders() {
        return graphicsDebugDrawMacroColliders;
    }

    public void setGraphicsDebugDrawMacroColliders(boolean graphicsDebugDrawMacroColliders) {
        this.graphicsDebugDrawMacroColliders = graphicsDebugDrawMacroColliders;
    }

    public boolean getGraphicsDebugDrawClientCellColliders() {
        return graphicsDebugDrawClientCellColliders;
    }

    public void setGraphicsDebugDrawClientCellColliders(boolean graphicsDebugDrawCellColliders) {
        this.graphicsDebugDrawClientCellColliders = graphicsDebugDrawCellColliders;
    }

    public boolean getGraphicsDebugDrawServerCellColliders() {
        return graphicsDebugDrawServerCellColliders;
    }

    public void setGraphicsDebugDrawServerCellColliders(boolean graphicsDebugDrawServerCellColliders) {
        this.graphicsDebugDrawServerCellColliders = graphicsDebugDrawServerCellColliders;
    }

    public boolean getGraphicsDebugDrawServerFacingVectors() {
        return graphicsDebugDrawServerFacingVectors;
    }

    public void setGraphicsDebugDrawServerFacingVectors(boolean graphicsDebugDrawServerFacingVectors) {
        this.graphicsDebugDrawServerFacingVectors = graphicsDebugDrawServerFacingVectors;
    }

    

    














    /**
     * Generates a default settings file
     * @return The settings file
     */
    static UserSettings getDefault(){
        UserSettings rVal = new UserSettings();
        
        //display settings
        rVal.displayHeight = 1080;
        rVal.displayWidth = 1920;
        rVal.displayFullscreen = true;
        
        //gameplay settings
        rVal.gameplayGenerateWorld = true;
        rVal.gameplayPhysicsCellRadius = 2;
        
        //graphics settings
        rVal.graphicsDebugDrawCollisionSpheresClient = false;
        rVal.graphicsDebugDrawCollisionSpheresServer = false;
        rVal.graphicsDebugDrawMovementVectors = false;
        rVal.graphicsDebugDrawPhysicsObjectsClient = false;
        rVal.graphicsDebugDrawPhysicsObjectsServer = false;
        rVal.graphicsDebugDrawNavmesh = false;
        rVal.graphicsPerformanceLODChunkRadius = 5;
        rVal.graphicsFOV = 90.0f;
        rVal.graphicsPerformanceDrawShadows = true;
        rVal.graphicsPerformanceEnableVSync = true;
        rVal.graphicsPerformanceOIT = true;
        rVal.graphicsPerformanceEnableFoliageManager = true;
        rVal.renderResolutionX = 1920;
        rVal.renderResolutionY = 1080;

        //debug settings
        rVal.netRunNetMonitor = false;
        
        return rVal;
    }
    
    /**
     * Loads the user's settings
     */
    public static UserSettings loadUserSettings(){
        LoggerInterface.loggerStartup.INFO("Load user settings");
        UserSettings rVal = FileUtils.loadObjectFromAssetPath("/Config/settings.json", UserSettings.class);
        if(rVal == null){
            rVal = UserSettings.getDefault();
        }
        return rVal;
    }
    
    
    
}
