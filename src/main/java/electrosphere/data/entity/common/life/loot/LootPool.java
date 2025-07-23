package electrosphere.data.entity.common.life.loot;

import java.util.List;

/**
 * A pool of loot that is dropped when an entity dies
 */
public class LootPool {
    
    /**
     * The tickets that can be generated from this loot pool
     */
    List<LootTicket> tickets;

    /**
     * Gets the tickets that can be selected from this pool
     * @return The list of tickets
     */
    public List<LootTicket> getTickets() {
        return tickets;
    }

    /**
     * Sets the list of tickets that can be selected from this pool
     * @param tickets The list of tickets
     */
    public void setTickets(List<LootTicket> tickets) {
        this.tickets = tickets;
    }

    

}
