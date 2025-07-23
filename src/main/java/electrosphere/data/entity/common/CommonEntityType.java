package electrosphere.data.entity.common;

import java.util.List;

import electrosphere.data.entity.collidable.CollidableTemplate;
import electrosphere.data.entity.collidable.HitboxData;
import electrosphere.data.entity.common.camera.CameraData;
import electrosphere.data.entity.common.interact.InteractionData;
import electrosphere.data.entity.common.item.InventoryDescription;
import electrosphere.data.entity.common.item.SpawnItemDescription;
import electrosphere.data.entity.common.life.HealthSystem;
import electrosphere.data.entity.common.light.PointLightDescription;
import electrosphere.data.entity.creature.LookAtSystem;
import electrosphere.data.entity.creature.ViewModelData;
import electrosphere.data.entity.creature.ai.AITreeData;
import electrosphere.data.entity.creature.attack.AttackMove;
import electrosphere.data.entity.creature.attack.AttackMoveResolver;
import electrosphere.data.entity.creature.block.BlockSystem;
import electrosphere.data.entity.creature.bonegroups.BoneGroup;
import electrosphere.data.entity.creature.equip.EquipPoint;
import electrosphere.data.entity.creature.equip.ToolbarData;
import electrosphere.data.entity.creature.movement.MovementSystem;
import electrosphere.data.entity.creature.rotator.RotatorSystem;
import electrosphere.data.entity.foliage.AmbientAudio;
import electrosphere.data.entity.foliage.GrowthData;
import electrosphere.data.entity.foliage.GrowthModel;
import electrosphere.data.entity.furniture.FurnitureData;
import electrosphere.data.entity.graphics.GraphicsTemplate;
import electrosphere.data.entity.grident.GridAlignedData;
import electrosphere.data.entity.particle.ParticleEmitter;

/**
 * Common data that all entity types use
 */
public class CommonEntityType {
    
    /**
     * The id of the creature
     */
    String id;

    /**
     * The display name of this entity
     */
    String displayName;
    
    /**
     * The list of hitboxes on the creature
     */
    List<HitboxData> hitboxes;
    
    /**
     * Various tokens
     */
    List<String> tokens;

    /**
     * The movement systems available to this creature type
     */
    List<MovementSystem> movementSystems;

    /**
     * Rotator systems available to this creature type
     */
    RotatorSystem rotatorSystem;
    
    /**
     * The list of equip points on this creature
     */
    List<EquipPoint> equipPoints;

    /**
     * The data for the toolbar
     */
    ToolbarData toolbarData;

    /**
     * The collidable used for this creature type
     */
    CollidableTemplate collidable;

    /**
     * The list of attack moves available to this creature
     */
    List<AttackMove> attackMoves;

    /**
     * The health system available to this creature
     */
    HealthSystem healthSystem;

    /**
     * The look at system available for this creature
     */
    LookAtSystem lookAtSystem;

    /**
     * The view model data for this creature
     */
    ViewModelData viewModelData;

    /**
     * The block system for this creature
     */
    BlockSystem blockSystem;

    /**
     * The configuration data for the ai trees associated with this creature
     */
    List<AITreeData> aiTrees;

    /**
     * The attack move resolver for this creature type
     */
    AttackMoveResolver attackMoveResolver;

    /**
     * The list of bone groups for this creature
     */
    List<BoneGroup> boneGroups;

    /**
     * the model of growth characterists
     */
    GrowthModel growthModel;

    /**
     * The ambient audio model
     */
    AmbientAudio ambientAudio;

    /**
     * The graphics template for this object
     */
    GraphicsTemplate graphicsTemplate;

    /**
     * The point light assigned to the entity
     */
    PointLightDescription pointLight;

    /**
     * The particle emitter assigned to the entity
     */
    ParticleEmitter particleEmitter;

    /**
     * Data determining how cameras interact with this type of entity
     */
    CameraData cameraData;

    /**
     * Data about a spawn item type should be generated for this common entity type
     */
    SpawnItemDescription spawnItem;

    /**
     * The behavior when this entity is interacted with via the button interaction
     */
    InteractionData buttonInteraction;

    /**
     * Data for grid alignment
     */
    GridAlignedData gridAlignedData;

    /**
     * Data for furniture behaviors
     */
    FurnitureData furnitureData;

    /**
     * The growth data
     */
    GrowthData growthData;

    /**
     * The data on the inventory of this entity
     */
    InventoryDescription inventoryData;

    /**
     * Gets the id for this creature type
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of this entity type
     * @param id The new id
     */
    public void setId(String id){
        this.id = id;
    }

    /**
     * Gets the display name of the entity type
     * @return The display name
     */
    public String getDisplayName(){
        return displayName;
    }

    /**
     * Sets the display name of the entity type
     * @param displayName The display name
     */
    public void setDisplayName(String displayName){
        this.displayName = displayName;
    }

    /**
     * Gets the list of hitboxes for this creature
     * @return The list of hitbox data
     */
    public List<HitboxData> getHitboxes() {
        return hitboxes;
    }

    /**
     * Gets the tokens for this creature
     * @return The tokens
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * Sets the tokens for this entity type
     * @param tokens The list of tokens
     */
    public void setTokens(List<String> tokens){
        this.tokens = tokens;
    }

    /**
     * Gets the list of data of movement types available to this creature
     * @return The list of movement type data
     */
    public List<MovementSystem> getMovementSystems() {
        return movementSystems;
    }

    /**
     * Gets the list of attack moves available to this creature type
     * @return The list of attack moves
     */
    public List<AttackMove> getAttackMoves() {
        return attackMoves;
    }

    /**
     * Gets the health system data for this creature type
     * @return The health system data
     */
    public HealthSystem getHealthSystem() {
        return healthSystem;
    }

    /**
     * Gets the collidable data for this creature type
     * @return The collidable data
     */
    public CollidableTemplate getCollidable() {
        return collidable;
    }

    /**
     * Gets the look at system configuration for this creature type
     * @return The look at system data
     */
    public LookAtSystem getLookAtSystem() {
        return lookAtSystem;
    }
    
    /**
     * Gets the rotator data for this creature type
     * @return The rotator data
     */
    public RotatorSystem getRotatorSystem() {
        return rotatorSystem;
    }

    /**
     * Gets the list of equip points for this creature type
     * @return The list of equip points
     */
    public List<EquipPoint> getEquipPoints(){
        return equipPoints;
    }

    /**
     * Gets the toolbar data
     * @return The toolbar data
     */
    public ToolbarData getToolbarData(){
        return toolbarData;
    }

    /**
     * Sets the attack move resolver for this creature type
     * @param resolver The resolver
     */
    public void setAttackMoveResolver(AttackMoveResolver resolver){
        attackMoveResolver = resolver;
    }

    /**
     * Gets the attack move resolver for this creature type
     * @return The attack move resolver
     */
    public AttackMoveResolver getAttackMoveResolver(){
        return attackMoveResolver;
    }

    /**
     * Gets the first-person view model data for this creature type
     * @return The first-person view model data
     */
    public ViewModelData getViewModelData(){
        return viewModelData;
    }

    /**
     * Gets the block system data for this creature type
     * @return The block system data
     */
    public BlockSystem getBlockSystem(){
        return blockSystem;
     }

    /**
     * Gets the AI tree data associated with this creature type
     * @return The list of ai tree data
     */
    public List<AITreeData> getAITrees(){
        return aiTrees;
    }

    /**
     * Gets the list of bone groups for this creature
     * @return The list of bone groups
     */
    public List<BoneGroup> getBoneGroups(){
        return boneGroups;
    }

    /**
     * Gets the graphics template for this object
     * @return the graphics template
     */
    public GraphicsTemplate getGraphicsTemplate(){
        return graphicsTemplate;
    }

    /**
     * Sets the graphics template for this object
     * @param graphicsTemplate The graphics template
     */
    public void setGraphicsTemplate(GraphicsTemplate graphicsTemplate){
        this.graphicsTemplate = graphicsTemplate;
    }

    /**
     * Gets the growth model
     * @return The growth model
     */
    public GrowthModel getGrowthModel(){
        return growthModel;
    }

    /**
     * Gets the ambient audio model
     * @return The ambient audio model
     */
    public AmbientAudio getAmbientAudio(){
        return ambientAudio;
    }

    /**
     * Gets the point light data
     * @return The point light data
     */
    public PointLightDescription getPointLight(){
        return pointLight;
    }

    /**
     * Gets the particle emitter data
     * @return The particle emitter data
     */
    public ParticleEmitter getParticleEmitter(){
        return particleEmitter;
    }

    /**
     * Gets the camera data for this entity
     * @return The camera data
     */
    public CameraData getCameraData() {
        return cameraData;
    }

    /**
     * Gets the ai tree data for this entity
     * @return The ai tree data
     */
    public List<AITreeData> getAiTrees() {
        return aiTrees;
    }

    /**
     * Gets data about a spawn item type should be generated for this common entity type
     * @return Data about a spawn item type should be generated for this common entity type
     */
    public SpawnItemDescription getSpawnItem(){
        return this.spawnItem;
    }

    /**
     * Gets the behavior when this entity is interacted with via the button interaction
     * @return The behavior when this entity is interacted with via the button interaction
     */
    public InteractionData getButtonInteraction() {
        return buttonInteraction;
    }

    /**
     * Sets the behavior when this entity is interacted with via the button interaction
     * @return The behavior when this entity is interacted with via the button interaction
     */
    public void setButtonInteraction(InteractionData interaction) {
        this.buttonInteraction = interaction;
    }

    /**
     * Gets the grid aligned data
     * @return The grid aligned data
     */
    public GridAlignedData getGridAlignedData() {
        return gridAlignedData;
    }

    /**
     * Sets the grid-aligned data
     * @param data The grid-aligned data
     */
    public void setGridAlignedData(GridAlignedData data){
        this.gridAlignedData = data;
    }

    /**
     * Gets the data for furniture behaviors
     * @return The data for furniture behaviors
     */
    public FurnitureData getFurnitureData() {
        return furnitureData;
    }
    
    /**
     * Gets the growth data for the entity
     * @return The growth data
     */
    public GrowthData getGrowthData(){
        return growthData;
    }

    /**
     * Gets the inventory data for this object
     * @return The inventory data
     */
    public InventoryDescription getInventoryData(){
        return inventoryData;
    }


}
