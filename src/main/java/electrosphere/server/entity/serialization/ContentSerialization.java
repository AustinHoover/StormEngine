package electrosphere.server.entity.serialization;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import electrosphere.entity.Entity;
import electrosphere.entity.EntityUtils;
import electrosphere.entity.state.attach.AttachUtils;
import electrosphere.entity.state.server.ServerCharacterData;
import electrosphere.entity.types.EntityTypes;
import electrosphere.entity.types.EntityTypes.EntityType;
import electrosphere.entity.types.common.CommonEntityUtils;
import electrosphere.entity.types.creature.ObjectTemplate;
import electrosphere.entity.types.creature.CreatureUtils;
import electrosphere.entity.types.foliage.FoliageUtils;
import electrosphere.entity.types.item.ItemUtils;
import electrosphere.server.datacell.Realm;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.util.Utilities;

/**
 * Contains all content for a given cell
 */
public class ContentSerialization {


    /**
     * The entities, serialized
     */
    List<EntitySerialization> serializedEntities = new LinkedList<EntitySerialization>();

    /**
     * Constructs a content serialization of a given list of entities
     * @param entities The entities
     * @return The content serialization
     */
    public static ContentSerialization constructContentSerialization(Collection<Entity> entities){
        ContentSerialization rVal = new ContentSerialization();
        for(Entity entity : entities){
            if(!CreatureUtils.hasControllerPlayerId(entity) && !ServerCharacterData.hasServerCharacterDataTree(entity)){
                EntityType type = CommonEntityUtils.getEntityType(entity);
                if(type == EntityType.ENGINE){
                    //do not serialize engine entities
                    continue;
                }
                //don't serialize attached entities
                if(AttachUtils.isAttached(entity)){
                    continue;
                }
                if(type != null){
                    EntitySerialization serializedEntity = ContentSerialization.constructEntitySerialization(entity);
                    rVal.serializedEntities.add(serializedEntity);
                }
            }
        }
        return rVal;
    }

    /**
     * Constructs a serialization of an entity
     * @param entity The entity
     * @return The serialization of the entity
     */
    public static EntitySerialization constructEntitySerialization(Entity entity){
        if(AttachUtils.isAttached(entity)){
            throw new Error("Trying to serialize attached entity!");
        }
        EntitySerialization serializedEntity = new EntitySerialization();
        serializedEntity.setPosition(EntityUtils.getPosition(entity));
        serializedEntity.setRotation(EntityUtils.getRotation(entity));
        if(CommonEntityUtils.getObjectTemplate(entity) != null){
            serializedEntity.setTemplate(Utilities.stringify(CommonEntityUtils.getObjectTemplate(entity)));
        }
        EntityType type = CommonEntityUtils.getEntityType(entity);
        if(type != null){
            switch(type){
                case CREATURE: {
                    serializedEntity.setType(EntityType.CREATURE.getValue());
                    serializedEntity.setSubtype(CommonEntityUtils.getEntitySubtype(entity));
                } break;
                case ITEM: {
                    serializedEntity.setType(EntityType.ITEM.getValue());
                    serializedEntity.setSubtype(CommonEntityUtils.getEntitySubtype(entity));
                } break;
                case FOLIAGE: {
                    serializedEntity.setType(EntityType.FOLIAGE.getValue());
                    serializedEntity.setSubtype(CommonEntityUtils.getEntitySubtype(entity));
                } break;
                case COMMON: {
                    serializedEntity.setType(EntityType.COMMON.getValue());
                    serializedEntity.setSubtype(CommonEntityUtils.getEntitySubtype(entity));
                } break;
                case ENGINE: {
                    throw new Error("Unsupported entity type!");
                }
            }
        } else {
            throw new Error("Trying to save entity that does not have a type!");
        }
        return serializedEntity;
    }

    /**
     * Actually creates the entities from a content serialization
     * @param contentRaw The content serialization
     */
    public void hydrateRawContent(Realm realm, ServerDataCell serverDataCell){
        List<EntitySerialization> serializedEntities = this.getSerializedEntities();
        for(EntitySerialization serializedEntity : serializedEntities){
            ContentSerialization.serverHydrateEntitySerialization(realm, serializedEntity);
        }
    }

    /**
     * Hydrates an entity serialization into a realm
     * @param realm The realm
     * @param serializedEntity The entity serialization
     */
    public static Entity serverHydrateEntitySerialization(Realm realm, EntitySerialization serializedEntity){
        Entity rVal = null;
        if(serializedEntity.getSubtype() == null){
            throw new Error("Subtype undefined!");
        }
        ObjectTemplate template = null;
        if(serializedEntity.getTemplate() != null && serializedEntity.getTemplate().length() > 0){
            try{
                template = Utilities.deserialize(serializedEntity.getTemplate(), ObjectTemplate.class);
            } catch(Exception e){
                String message = serializedEntity.getTemplate() + "\n";
                throw new Error(message, e);
            }
        }
        switch(EntityTypes.fromInt(serializedEntity.getType())){
            case CREATURE: {
                rVal = CreatureUtils.serverSpawnBasicCreature(realm, serializedEntity.getPosition(), serializedEntity.getSubtype(), template);
                CreatureUtils.serverApplyTemplate(realm, rVal, template);
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case ITEM: {
                rVal = ItemUtils.serverSpawnBasicItem(realm, serializedEntity.getPosition(), serializedEntity.getSubtype());
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case COMMON: {
                if(template == null){
                    rVal = CommonEntityUtils.serverSpawnBasicObject(realm, serializedEntity.getPosition(), serializedEntity.getSubtype());
                } else {
                    rVal = CommonEntityUtils.serverSpawnTemplateObject(realm, serializedEntity.getPosition(), serializedEntity.getSubtype(), template);
                }
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case FOLIAGE: {
                rVal = FoliageUtils.serverSpawnTreeFoliage(realm, serializedEntity.getPosition(), serializedEntity.getSubtype());
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case ENGINE: {
                throw new Error("Unsupported entity type!");
            }
        }
        return rVal;
    }

    /**
     * Hydrates an entity serialization into a realm
     * @param serializedEntity The entity serialization
     */
    public static Entity clientHydrateEntitySerialization(EntitySerialization serializedEntity){
        Entity rVal = null;
        ObjectTemplate template = null;
        if(serializedEntity.getTemplate() != null && serializedEntity.getTemplate().length() > 0){
            template = Utilities.deserialize(serializedEntity.getTemplate(), ObjectTemplate.class);
        }
        switch(EntityTypes.fromInt(serializedEntity.getType())){
            case CREATURE: {
                rVal = CreatureUtils.clientSpawnBasicCreature(serializedEntity.getSubtype(), template);
                CreatureUtils.clientApplyTemplate(rVal, template);
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case ITEM: {
                rVal = ItemUtils.clientSpawnBasicItem(serializedEntity.getSubtype());
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case COMMON: {
                rVal = CommonEntityUtils.clientSpawnBasicObject(serializedEntity.getSubtype());
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case FOLIAGE: {
                rVal = FoliageUtils.clientSpawnBasicFoliage(serializedEntity.getSubtype());
                EntityUtils.getRotation(rVal).set(serializedEntity.getRotation());
            } break;
            case ENGINE: {
                throw new Error("Unsupported entity type!");
            }
        }
        return rVal;
    }

    /**
     * Creates a serialization of a given entity type without depending on a pre-existing entity
     * @param type The type of entity
     * @param id The id of the entity type
     * @return The serialization
     */
    public static EntitySerialization createNewSerialization(EntityType type, String id){
        EntitySerialization rVal = new EntitySerialization();
        rVal.setType(type.getValue());
        rVal.setSubtype(id);
        rVal.setPosition(new Vector3d());
        rVal.setRotation(new Quaterniond());
        return rVal;
    }

    /**
     * Gets all serialized entities
     * @return the list of serialized entities
     */
    public List<EntitySerialization> getSerializedEntities(){
        return serializedEntities;
    }
    
}
