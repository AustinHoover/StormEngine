package electrosphere.data.entity.creature.attack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import electrosphere.logger.LoggerInterface;

/**
 * Resolves attack move ids to lists of chains of attacks
 */
public class AttackMoveResolver {
    
    /**
     * The map of attack move id -> attack move object chain
     */
    Map<String,List<AttackMove>> attackMovesetMap = new HashMap<String,List<AttackMove>>();

    /**
     * Constructor
     * @param movelist The raw list of attack moves
     */
    public AttackMoveResolver(List<AttackMove> movelist){
        //get all moves
        for(AttackMove move : movelist){
            String type = move.getType();
            if(attackMovesetMap.containsKey(type)){
                attackMovesetMap.get(type).add(move);
            } else {
                List<AttackMove> moveList = new LinkedList<AttackMove>();
                moveList.add(move);
                attackMovesetMap.put(type,moveList);
            }
        }
        //reorder
        for(String attackTypeKey : attackMovesetMap.keySet()){
            List<AttackMove> currentKeyList = attackMovesetMap.get(attackTypeKey);
            reorderMoveset(attackTypeKey, currentKeyList);
        }
    }

    /**
     * Gets a chain of attack moves based on the attack type
     * @param attackType The type of attack (IE "Sword2HSlash1")
     * @return The chain of attack moves if it exists, null otherwise
     */
    public List<AttackMove> getMoveset(String attackType){
        return attackMovesetMap.get(attackType);
    }

    /**
     * Reorders the attack moveset list
     * @param attackTypeKey The current attack type key (IE "Sword2HSlash1")
     * @param finalMovelist The moveset list
     */
    void reorderMoveset(String attackTypeKey, List<AttackMove> finalMovelist){
        AttackMove currentMove = null;
        //solve for initial move
        for(AttackMove move : finalMovelist){
            if(move.isInitialMove()){
                currentMove = move;
                break;
            }
        }
        //order list
        if(currentMove != null){
            List<AttackMove> orderedList = new LinkedList<AttackMove>();
            orderedList.add(currentMove);
            String nextId = currentMove.getNextMoveId();
            while(nextId != null && !nextId.equals("")){
                if(finalMovelist.size() == 0){
                    break;
                }
                for(AttackMove move : finalMovelist){
                    if(move.getAttackMoveId().equals(nextId)){
                        currentMove = move;
                        finalMovelist.remove(move);
                        orderedList.add(currentMove);
                        nextId = currentMove.nextMoveId;
                        break;
                    }
                }
            }
            //replace final list contents with ordered list contents
            finalMovelist.clear();
            for(AttackMove move : orderedList){
                finalMovelist.add(move);
            }
        } else {
            String message = "FAILED TO LOAD INITIAL MOVE IN AttackMoveResolver\n" +
            "The attack move type is: " + attackTypeKey + "\n" +
            "This is commonly caused by having your initial move in the attack chain in data not having the field \"initialMove\" set to true!"
            ;
            LoggerInterface.loggerEngine.ERROR(new IllegalArgumentException(message));
        }
    }

}
