package electrosphere.server.datacell.interfaces;

import java.util.Collection;

import org.joml.Vector3d;
import org.joml.Vector3i;

import electrosphere.entity.Entity;
import electrosphere.net.server.player.Player;
import electrosphere.server.datacell.ServerDataCell;
import electrosphere.server.macro.spatial.MacroObject;

/**
 * Interface for manager class for creating and destroying cells with respect to spatial locations
 */
public interface DataCellManager {

    /**
     * Adds a player to the realm that this manager controls. Should do this intelligently based on the player's location
     * @param player The player
     */
    public void addPlayerToRealm(Player player);

    /**
     * Moves a player to a new position
     * @param player The player
     * @param newPosition The new position
     */
    public void movePlayer(Player player, Vector3i newPosition);

    /**
     * Updates player positions based on the location of the player's current entity
     * @return True if a player changed cell, false otherwise
     */
    public boolean updatePlayerPositions();

    /**
     * Get data cell at a given real point in this realm
     * @param point The real point
     * @return Either the data cell if found, or null if not found
     */
    public ServerDataCell getDataCellAtPoint(Vector3d point);


    /**
     * Gets the world position of a given data cell
     * @param cell The data cell
     * @return The world position
     */
    public Vector3i getCellWorldPosition(ServerDataCell cell);

    /**
     * Tries to create a data cell at a given real point
     * @param point The real point
     * @return The data cell if created or if already exists, null if cannot create and does not already exist
     */
    public ServerDataCell tryCreateCellAtPoint(Vector3d point);

    /**
     * Gets a data cell at a given world position
     * @param position The world position
     * @return The data cell if found, null otherwise
     */
    public ServerDataCell getCellAtWorldPosition(Vector3i position);

    /**
     * Checks if this manager contains a given cell
     * @param cell The cell
     * @return True if this manager contains this cell, false otherwise
     */
    public boolean containsCell(ServerDataCell cell);

    /**
     * Calls the simulate function on all loaded cells
     */
    public void simulate();

    /**
     * Unloads playerless chunks. Strategy for doing this is defined per data cell manager.
     */
    public void unloadPlayerlessChunks();

    /**
     * Saves the data cell manager
     * @param saveName The name of the save
     */
    public void save(String saveName);

    /**
     * Guarantees that the returned position is in bounds
     * @param positionToTest the position
     * @return Either the position if it is in bounds, or the closest position that is in bounds
     */
    public Vector3d guaranteePositionIsInBounds(Vector3d positionToTest);

    /**
     * Halts all asynchronous work being done in this data cell manager
     */
    public void halt();

    /**
     * Looks up entities within a bounding sphere
     * @param pos The position of the sphere
     * @param radius The radius of the sphere
     * @return The list of entities within the bounding sphere
     */
    public Collection<Entity> entityLookup(Vector3d pos, double radius);

    /**
     * Gets the point to have the macro object enter a data cell managed by this manager
     * @param point The point to try to spawn near
     * @return The point to enter by
     */
    public Vector3d getMacroEntryPoint(Vector3d point);

    /**
     * Evaluates the realm's view of a macro object (ie, should it be spawned in or not)
     * @param object The object to evaluate
     */
    public void evaluateMacroObject(MacroObject object);
    
}
