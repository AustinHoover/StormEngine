package electrosphere.entity.state.attach;

import electrosphere.engine.Globals;
import electrosphere.entity.Entity;
import electrosphere.entity.EntityDataStrings;
import electrosphere.entity.EntityTags;
import electrosphere.entity.EntityUtils;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.actor.Actor;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.datacell.utils.ServerEntityTagUtils;
import electrosphere.server.entity.poseactor.PoseActor;

import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Utilities for attaching entities to entities
 */
public class AttachUtils {








    //
    //    FUNCTIONS TO UPDATE ATTACHMENTS FOR CURRENT FRAME
    //
    
    ///
    ///
    ///           SERVER
    ///
    ///

    /**
     * Updates positions of all attached entities in a data cell
     * @param cell The data cell
     */
    public static void serverUpdateAttachedEntityPositions(ServerDataCell cell){
        Globals.profiler.beginAggregateCpuSample("AttachUtils.serverUpdateAttachedEntityPositions");
        AttachUtils.serverUpdateBoneAttachedEntityPositions(cell);
        AttachUtils.serverUpdateNonBoneAttachments(cell);
        Globals.profiler.endCpuSample();
    }

    /**
     * Updates entities attached to bones of actors in a data cell
     * @param cell The data cell
     */
    public static void serverUpdateBoneAttachedEntityPositions(ServerDataCell cell){
        for(Entity currentEntity : cell.getScene().getEntitiesWithTag(EntityTags.BONE_ATTACHED)){
            Entity parent;
            if((parent = (Entity)currentEntity.getData(EntityDataStrings.ATTACH_PARENT))!=null){
                String targetBone;
                if((targetBone = AttachUtils.getTargetBone(currentEntity))!=null){
                    PoseActor parentActor = EntityUtils.getPoseActor(parent);

                    //manual offset
                    Vector3d offset = AttachUtils.getVectorOffset(currentEntity);
                    if(offset == null){
                        offset = new Vector3d();
                    }

                    AttachUtils.calculateEntityTransforms(
                        currentEntity,
                        new Vector3d(offset),
                        new Quaterniond(AttachUtils.getRotationOffset(currentEntity)),
                        new Vector3d(parentActor.getBonePosition(targetBone)),
                        new Quaterniond(parentActor.getBoneRotation(targetBone)),
                        new Vector3d(EntityUtils.getPosition(parent)),
                        new Quaterniond(EntityUtils.getRotation(parent)),
                        new Vector3d(EntityUtils.getScale(parent))
                    );
                }
            } else if(currentEntity.getData(EntityDataStrings.ATTACH_TARGET_BASE)!=null){
                Vector3d positionOffset = getVectorOffset(currentEntity);
                Vector3d parentPosition = EntityUtils.getPosition(parent);
                EntityUtils.setPosition(currentEntity, new Vector3d(parentPosition).add(positionOffset));
            }
        }
    }

    /**
     * Updates entities that aren't attached to a bone directly in a data cell
     * @param cell the data cell
     */
    private static void serverUpdateNonBoneAttachments(ServerDataCell cell){
        Globals.profiler.beginAggregateCpuSample("AttachUtils.serverUpdateNonBoneAttachments");
        Matrix4d parentTransform = new Matrix4d().identity();
        Vector3d position = new Vector3d();
        Quaterniond rotation = new Quaterniond();
        Vector3d scaleRaw = new Vector3d();
        Vector3f scale = new Vector3f();
        Entity parent;
        Matrix4d transform;
        //update entities attached to centerpoint + transform of other entities
        for(Entity currentEntity : cell.getScene().getEntitiesWithTag(EntityTags.TRANSFORM_ATTACHED)){
            if((parent = (Entity)currentEntity.getData(EntityDataStrings.ATTACH_PARENT))!=null){
                if((transform = AttachUtils.getTransformOffset(currentEntity))!=null){
                    //parent objects
                    Vector3d parentPosition = EntityUtils.getPosition(parent);
                    Quaterniond parentRotation = EntityUtils.getRotation(parent);
                    Vector3f parentScale = EntityUtils.getScale(parent);
                    // calculate new transform for current entity
                    parentTransform.identity()
                        .translate(parentPosition)
                        .rotate(parentRotation)
                        .scale(parentScale.x,parentScale.y,parentScale.z)
                        .mul(transform);
                    //transform bone space
                    parentTransform.getTranslation(position);
                    parentTransform.getUnnormalizedRotation(rotation).normalize();
                    parentTransform.getScale(scaleRaw);
                    scale.set((float)scaleRaw.x,(float)scaleRaw.y,(float)scaleRaw.z);
                    //transform worldspace
                    // position.add(new Vector3d(EntityUtils.getPosition(parent)));
                    //set
                    EntityUtils.setPosition(currentEntity, position);
                    EntityUtils.getRotation(currentEntity).set(rotation);
                    EntityUtils.getScale(currentEntity).set(scale);
                }
            }
        }
        Globals.profiler.endCpuSample();
    }


















    ///
    ///
    ///           CLIENT
    ///
    ///


    /**
     * Client version of attachment update functions
     */
    public static void clientUpdateAttachedEntityPositions(){
        Globals.profiler.beginCpuSample("AttachUtils.clientUpdateAttachedEntityPositions");
        AttachUtils.clientUpdateBoneAttachments();
        AttachUtils.clientUpdateNonBoneAttachments();
        Globals.profiler.endCpuSample();
    }

    /**
     * Updates entities attached to bones
     */
    private static void clientUpdateBoneAttachments(){
        Globals.profiler.beginCpuSample("AttachUtils.clientUpdateBoneAttachments");
        //update entities attached to bones of other entities
        for(Entity currentEntity : Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.BONE_ATTACHED)){
            Entity parent;
            if(currentEntity == null){
                LoggerInterface.loggerEngine.ERROR(new IllegalStateException("Trying to update client bone attachment where null entity is registered!"));
            } else if((parent = (Entity)currentEntity.getData(EntityDataStrings.ATTACH_PARENT))!=null){
                AttachUtils.clientUpdateEntityTransforms(currentEntity,parent);
            } else if(currentEntity.getData(EntityDataStrings.ATTACH_TARGET_BASE)!=null){
                Vector3d positionOffset = getVectorOffset(currentEntity);
                Vector3d parentPosition = EntityUtils.getPosition(parent);
                EntityUtils.setPosition(currentEntity, new Vector3d(parentPosition).add(positionOffset));
            }
        }
        Globals.profiler.endCpuSample();
    }

    /**
     * Updates the spatial data for the attached entity
     * @param child The entity that is attached to a parent
     * @param parent The parent entity that has a child attached to it
     */
    public static void clientUpdateEntityTransforms(Entity child, Entity parent){
        String targetBone;
        if((targetBone = AttachUtils.getTargetBone(child))!=null){
            Actor parentActor = EntityUtils.getActor(parent);

            //manual offset
            Vector3d offset = AttachUtils.getVectorOffset(child);
            if(offset == null){
                offset = new Vector3d();
            }

            AttachUtils.calculateEntityTransforms(
                child,
                new Vector3d(offset),
                new Quaterniond(AttachUtils.getRotationOffset(child)),
                new Vector3d(parentActor.getAnimationData().getBonePosition(targetBone)),
                new Quaterniond(parentActor.getAnimationData().getBoneRotation(targetBone)),
                new Vector3d(EntityUtils.getPosition(parent)),
                new Quaterniond(EntityUtils.getRotation(parent)),
                new Vector3d(EntityUtils.getScale(parent))
            );
        }
    }

    /**
     * Updates entities that aren't attached to a bone directly
     */
    private static void clientUpdateNonBoneAttachments(){
        Globals.profiler.beginCpuSample("AttachUtils.updateNonBoneAttachments");
        Matrix4d parentTransform = new Matrix4d().identity();
        Vector3d position = new Vector3d();
        Quaterniond rotation = new Quaterniond();
        Vector3d scaleRaw = new Vector3d();
        Vector3f scale = new Vector3f();
        Entity parent;
        Matrix4d transform;
        //update entities attached to centerpoint + transform of other entities
        for(Entity currentEntity : Globals.clientState.clientSceneWrapper.getScene().getEntitiesWithTag(EntityTags.TRANSFORM_ATTACHED)){
            if((parent = (Entity)currentEntity.getData(EntityDataStrings.ATTACH_PARENT))!=null){
                if((transform = AttachUtils.getTransformOffset(currentEntity))!=null){
                    //parent objects
                    Vector3d parentPosition = EntityUtils.getPosition(parent);
                    Quaterniond parentRotation = EntityUtils.getRotation(parent);
                    Vector3f parentScale = EntityUtils.getScale(parent);
                    // calculate new transform for current entity
                    parentTransform.identity()
                        .translate(parentPosition)
                        .rotate(parentRotation)
                        .scale(parentScale.x,parentScale.y,parentScale.z)
                        .mul(transform);
                    //transform bone space
                    parentTransform.getTranslation(position);
                    parentTransform.getUnnormalizedRotation(rotation).normalize();
                    parentTransform.getScale(scaleRaw);
                    scale.set((float)scaleRaw.x,(float)scaleRaw.y,(float)scaleRaw.z);
                    //transform worldspace
                    // position.add(new Vector3d(EntityUtils.getPosition(parent)));
                    //set
                    EntityUtils.setPosition(currentEntity, position);
                    EntityUtils.getRotation(currentEntity).set(rotation);
                    EntityUtils.getScale(currentEntity).set(scale);
                }
            }
        }
        Globals.profiler.endCpuSample();
    }










    ///
    ///    MATH
    ///
    /**
     * Updates the spatial data for the attached entity
     * @param child The entity that is attached to a parent
     * @param parent The parent entity that has a child attached to it
     */
    public static void calculateEntityTransforms(
        Entity child,
        
        //optional offsets
        Vector3d offsetVector,
        Quaterniond offsetRotation,

        //current bone transform
        Vector3d bonePosition,
        Quaterniond boneRotation,

        //parent transforms
        Vector3d parentPosition,
        Quaterniond parentRotation,
        Vector3d parentScale
    ){
        //transform bone space
        Vector3d position = AttachUtils.calculateBoneAttachmentWorldPosition(
            offsetVector,
            offsetRotation,
            bonePosition,
            boneRotation,
            parentPosition,
            parentRotation,
            parentScale
        );
        //set
        EntityUtils.setPosition(child, position);


        //calculate and apply rotation
        Quaterniond rotation = AttachUtils.calculateBoneAttachmentRotation(
            offsetVector,
            offsetRotation,
            bonePosition,
            boneRotation,
            parentPosition,
            parentRotation,
            parentScale
        );
        EntityUtils.getRotation(child).set(rotation);
    }

    /**
     * Calculates the position of an entity attached to a bone
     * @param offsetVector The offset position
     * @param offsetRotation The offset rotation
     * @param bonePosition The bone's position
     * @param boneRotation The bone's rotation
     * @param parentPosition The parent's position
     * @param parentRotation The parent's rotation
     * @param parentScale The parent's scale
     * @return The position of the attached/child entity
     */
    public static Vector3d calculateBoneAttachmentLocalPosition(
        //optional offsets
        Vector3d offsetVector,
        Quaterniond offsetRotation,

        //current bone transform
        Vector3d bonePosition,
        Quaterniond boneRotation,

        //parent transforms
        Vector3d parentPosition,
        Quaterniond parentRotation,
        Vector3d parentScale
    ){
        return AttachUtils.calculateBoneAttachmentLocalPosition(new Vector3d(), offsetVector, offsetRotation, bonePosition, boneRotation, parentPosition, parentRotation, parentScale);
    }

    /**
     * Calculates the position of an entity attached to a bone
     * @param offsetVector The offset position
     * @param offsetRotation The offset rotation
     * @param bonePosition The bone's position
     * @param boneRotation The bone's rotation
     * @param parentPosition The parent's position
     * @param parentRotation The parent's rotation
     * @param parentScale The parent's scale
     * @return The position of the attached/child entity
     */
    public static Vector3d calculateBoneAttachmentLocalPosition(
        //The vector to store the result in
        Vector3d res,

        //optional offsets
        Vector3d offsetVector,
        Quaterniond offsetRotation,

        //current bone transform
        Vector3d bonePosition,
        Quaterniond boneRotation,

        //parent transforms
        Vector3d parentPosition,
        Quaterniond parentRotation,
        Vector3d parentScale
    ){
        //transform bone space
        Vector3d position = res.set(offsetVector);
        position = position.rotate(new Quaterniond(boneRotation));
        position = position.add(bonePosition);
        position = position.mul(parentScale);
        position = position.rotate(new Quaterniond(parentRotation));
        //transform worldspace
        // position.add(parentPosition);

        return position;
    }

    /**
     * Calculates the position of an entity attached to a bone
     * @param offsetVector The offset position
     * @param offsetRotation The offset rotation
     * @param bonePosition The bone's position
     * @param boneRotation The bone's rotation
     * @param parentPosition The parent's position
     * @param parentRotation The parent's rotation
     * @param parentScale The parent's scale
     * @return The position of the attached/child entity
     */
    public static Vector3d calculateBoneAttachmentWorldPosition(
        //optional offsets
        Vector3d offsetVector,
        Quaterniond offsetRotation,

        //current bone transform
        Vector3d bonePosition,
        Quaterniond boneRotation,

        //parent transforms
        Vector3d parentPosition,
        Quaterniond parentRotation,
        Vector3d parentScale
    ){
        //transform bone space
        Vector3d position = calculateBoneAttachmentLocalPosition(offsetVector, offsetRotation, bonePosition, boneRotation, parentPosition, parentRotation, parentScale);
        //transform worldspace
        position.add(parentPosition);

        return position;
    }

    /**
     * Calculates the rotation of a child that is attached to a bone on an entity
     * @param offsetVector The offset vector
     * @param offsetRotation The offset rotation
     * @param bonePosition The position of the bone
     * @param boneRotation The rotation of the bone
     * @param parentPosition The position of the parent
     * @param parentRotation The rotation of the parent
     * @param parentScale The scale of the parent
     * @return The rotation of the child
     */
    public static Quaterniond calculateBoneAttachmentRotation(
        //optional offsets
        Vector3d offsetVector,
        Quaterniond offsetRotation,

        //current bone transform
        Vector3d bonePosition,
        Quaterniond boneRotation,

        //parent transforms
        Vector3d parentPosition,
        Quaterniond parentRotation,
        Vector3d parentScale
    ){
        return new Quaterniond()
            .mul(parentRotation)
            .mul(boneRotation)
            .mul(offsetRotation)
            .normalize();
    }














    //
    //    FUNCTIONS TO ATTACH AN ENTITY
    //



    public static void serverAttachEntityToEntityAtBone(Entity parent, Entity toAttach, String boneName, Vector3d offset, Quaterniond rotation){
        ServerEntityTagUtils.attachTagToEntity(toAttach, EntityTags.BONE_ATTACHED);
        toAttach.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        toAttach.putData(EntityDataStrings.ATTACH_PARENT, parent);
        toAttach.putData(EntityDataStrings.ATTACH_TARGET_BONE, boneName);
        AttachUtils.setVectorOffset(toAttach, offset);
        AttachUtils.setRotationOffset(toAttach, rotation);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).add(toAttach);
        } else {
            LinkedList<Entity> childrenEntities = new LinkedList<Entity> ();
            childrenEntities.add(toAttach);
            parent.putData(EntityDataStrings.ATTACH_CHILDREN_LIST, childrenEntities);
        }
    }


    /**
     * Attaches an entity to another entity at a given bone
     * @param parent The parent entity
     * @param toAttach The entity that will be attached
     * @param boneName The name of the bone
     * @param rotation The rotation applied
     */
    public static void clientAttachEntityToEntityAtBone(Entity parent, Entity toAttach, String boneName, Vector3d offset, Quaterniond rotation){
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(toAttach, EntityTags.BONE_ATTACHED);
        toAttach.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        toAttach.putData(EntityDataStrings.ATTACH_PARENT, parent);
        toAttach.putData(EntityDataStrings.ATTACH_TARGET_BONE, boneName);
        AttachUtils.setVectorOffset(toAttach, offset);
        AttachUtils.setRotationOffset(toAttach, rotation);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).add(toAttach);
        } else {
            LinkedList<Entity> childrenEntities = new LinkedList<Entity> ();
            childrenEntities.add(toAttach);
            parent.putData(EntityDataStrings.ATTACH_CHILDREN_LIST, childrenEntities);
        }
    }




    /**
     * Attaches an entity to another based on the parent's absolute position in the game engine
     * @param parent The parent to attach to
     * @param toAttach The entity to attach to the parent
     */
    public static void clientAttachEntityAtCurrentOffset(Entity parent, Entity toAttach){
        Vector3d parentPosition = EntityUtils.getPosition(parent);
        Vector3d childPosition = EntityUtils.getPosition(toAttach);
        Vector3d offset = new Vector3d(childPosition).sub(parentPosition);
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(toAttach, EntityTags.TRANSFORM_ATTACHED);
        toAttach.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        toAttach.putData(EntityDataStrings.ATTACH_PARENT, parent);
        toAttach.putData(EntityDataStrings.ATTACH_TARGET_BASE, true);
        setVectorOffset(parent, offset);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).add(toAttach);
        } else {
            LinkedList<Entity> childrenEntities = new LinkedList<Entity> ();
            childrenEntities.add(toAttach);
            parent.putData(EntityDataStrings.ATTACH_CHILDREN_LIST, childrenEntities);
        }
    }

    /**
     * Attaches an entity such that a transform will be applied to it relative to the parent's position and rotation every frame
     * @param parent The parent entity
     * @param toAttach The child entity
     * @param transform The transform
     */
    public static void clientAttachEntityAtTransform(Entity parent, Entity toAttach, Matrix4d transform){
        Globals.clientState.clientSceneWrapper.getScene().registerEntityToTag(toAttach, EntityTags.TRANSFORM_ATTACHED);
        toAttach.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        toAttach.putData(EntityDataStrings.ATTACH_PARENT, parent);
        toAttach.putData(EntityDataStrings.ATTACH_TARGET_BASE, true);
        toAttach.putData(EntityDataStrings.ATTACH_TRANSFORM, transform);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).add(toAttach);
        } else {
            LinkedList<Entity> childrenEntities = new LinkedList<Entity> ();
            childrenEntities.add(toAttach);
            parent.putData(EntityDataStrings.ATTACH_CHILDREN_LIST, childrenEntities);
        }
    }

    /**
     * Updates the transform for the attachment
     * @param toAttach The entity that is attached
     * @param transform The transform
     */
    public static void updateAttachTransform(Entity toAttach, Matrix4d transform){
        toAttach.putData(EntityDataStrings.ATTACH_TRANSFORM, transform);
    }


    /**
     * Semantically attaches an entity to another entity
     * @param parent The parent entity
     * @param child The child entity
     */
    public static void attachEntityToEntity(Entity parent, Entity child){
        child.putData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED, true);
        child.putData(EntityDataStrings.ATTACH_PARENT, parent);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).add(child);
        } else {
            List<Entity> childrenEntities = new LinkedList<Entity>();
            childrenEntities.add(child);
            parent.putData(EntityDataStrings.ATTACH_CHILDREN_LIST, childrenEntities);
        }
    }








    





















    //
    //    FUNCTIONS TO DETATCH AN ENTITY
    //
    
    /**
     * Detatches an entity on the server
     * @param parent The parent entity
     * @param toAttach The attached entity
     * @return The bone the entity was attached to
     */
    public static String serverDetatchEntityFromEntityAtBone(Entity parent, Entity toAttach){
        String bone = getTargetBone(toAttach);
        ServerEntityTagUtils.removeTagFromEntity(toAttach, EntityTags.BONE_ATTACHED);
        toAttach.removeData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED);
        toAttach.removeData(EntityDataStrings.ATTACH_PARENT);
        toAttach.removeData(EntityDataStrings.ATTACH_TARGET_BONE);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).remove(toAttach);
        }
        return bone;
    }

    /**
     * Detatches an entity on the client
     * @param parent The parent entity
     * @param toAttach The attached entity
     * @return The bone the entity was attached to
     */
    public static String clientDetatchEntityFromEntityAtBone(Entity parent, Entity toAttach){
        String bone = getTargetBone(toAttach);
        Globals.clientState.clientSceneWrapper.getScene().removeEntityFromTag(toAttach, EntityTags.BONE_ATTACHED);
        toAttach.removeData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED);
        toAttach.removeData(EntityDataStrings.ATTACH_PARENT);
        toAttach.removeData(EntityDataStrings.ATTACH_TARGET_BONE);
        if(parent.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST)){
            getChildrenList(parent).remove(toAttach);
        }
        //special case handling for view model
        if(parent == Globals.clientState.playerEntity && getChildrenList(Globals.clientState.firstPersonEntity) != null){
            getChildrenList(Globals.clientState.firstPersonEntity).remove(toAttach);
        }
        return bone;
    }




























    //
    //    GETTERS
    //

    /**
     * Checks whether this entity is attached to another entity or not
     * @param e The entity
     * @return true if attached, false otherwise
     */
    public static boolean isAttached(Entity e){
        if(e == null){
            return false;
        }
        if(!e.containsKey(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED)){
            return false;
        }
        return (boolean)e.getData(EntityDataStrings.ATTACH_ENTITY_IS_ATTACHED);
    }
    
    /**
     * Gets the target bone
     * @param e The entity
     * @return The target bone
     */
    public static String getTargetBone(Entity e){
        return (String)e.getData(EntityDataStrings.ATTACH_TARGET_BONE);
    }
    
    /**
     * Gets the parent entity this child is attached to
     * @param e The child entity
     * @return The parent entity
     */
    public static Entity getParent(Entity e){
        return (Entity)e.getData(EntityDataStrings.ATTACH_PARENT);
    }

    /**
     * Checks if the entity has a parent
     * @param e The entity
     * @return true if has a parent, false otherwise
     */
    public static boolean hasParent(Entity e){
        return e.containsKey(EntityDataStrings.ATTACH_PARENT);
    }

    /**
     * Gets the rotation offset of a given entity
     * @param e The entity
     * @return The rotation offset
     */
    protected static Quaterniond getRotationOffset(Entity e){
        return (Quaterniond)e.getData(EntityDataStrings.ATTACH_ROTATION_OFFSET);
    }

    /**
     * Sets the attached rotation offset
     * @param e the attached entity
     * @param rotation The rotation offset
     */
    public static void setRotationOffset(Entity e, Quaterniond rotation){
        e.putData(EntityDataStrings.ATTACH_ROTATION_OFFSET, rotation);
    }

    /**
     * Gets the vector offset for an attachment
     * @param e The entity
     * @return The offset
     */
    protected static Vector3d getVectorOffset(Entity e){
        return (Vector3d)e.getData(EntityDataStrings.ATTACH_POSITION_OFFSET);
    }

    /**
     * Sets the vector offset for an attachment
     * @param e The entity
     * @param offset The offset
     */
    public static void setVectorOffset(Entity e, Vector3d offset){
        e.putData(EntityDataStrings.ATTACH_POSITION_OFFSET,offset);
    }

    /**
     * Gets the transform for a transform attached entity
     * @param e The entity
     * @return The transform if it exists, false otherwise
     */
    protected static Matrix4d getTransformOffset(Entity e){
        return (Matrix4d)e.getData(EntityDataStrings.ATTACH_TRANSFORM);
    }
    
    /**
     * Checks if the parent entity has attached child entities
     * @param e The parent entity
     * @return true if there are attached child entities, false otherwise
     */
    public static boolean hasChildren(Entity e){
        return e.containsKey(EntityDataStrings.ATTACH_CHILDREN_LIST) && !getChildrenList(e).isEmpty();
    }
    
    /**
     * Gets the list of entities attached to this parent entity
     * <p>
     * NOTE: This can return an empty list of an entity has been attached to this one prior
     * EVEN if it has since been unattached
     * </p>
     * @param parentEntity
     * @return The list of entities that are attached to this parent entity, or null if undefined
     */
    @SuppressWarnings("unchecked")
    public static List<Entity> getChildrenList(Entity parentEntity){
        return (List<Entity>)parentEntity.getData(EntityDataStrings.ATTACH_CHILDREN_LIST);
    }

    /**
     * Gets the equip point's rotation offset in quaterniond form
     * @param values The list of raw float values
     * @return The quaterniond containing those values or an identity quaterniond if no such values exist
     */
    public static Quaterniond getEquipPointRotationOffset(List<Float> values){
        if(values.size() > 0){
            return new Quaterniond(values.get(0),values.get(1),values.get(2),values.get(3));
        } else {
            return new Quaterniond();
        }
    }

    /**
     * Gets the equip point's vector offset in vector form
     * @param values The list of raw float values
     * @return The vector containing those values or an identity vector if no such values exist
     */
    public static Vector3d getEquipPointVectorOffset(List<Float> values){
        if(values.size() > 0){
            return new Vector3d(values.get(0),values.get(1),values.get(2));
        } else {
            return new Vector3d();
        }
    }

}
