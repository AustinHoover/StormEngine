package electrosphere.data.macro.temporal;

import electrosphere.engine.Main;

/**
 * Temporal data associated with the macro data (ie calendar date, world age, etc)
 */
public class MacroTemporalData {

    /**
     * Amount of time per day
     */
    public static final long TIME_PER_DAY = (long)Main.targetFrameRate * 60 * 30;

    /**
     * The noon remainder amount
     */
    public static final long TIME_NOON = TIME_PER_DAY / 2;

    /**
     * The midnight remainder amount
     */
    public static final long TIME_MIDNIGHT = 0;
    
    /**
     * Total age of the macro data in years
     */
    private long age;

    /**
     * The time WITHIN THE CURRENT YEAR
     */
    private long time;

    /**
     * Increments the time of the temporal data by some amount
     */
    public void increment(long amount){
        this.time = this.time + amount;
    }

    /**
     * Gets the time WITHIN THE CURRENT YEAR of the data
     * @return The time
     */
    public long getTime(){
        return this.time;
    }

    /**
     * Gets the age of the world in years
     * @return The age of the world in years
     */
    public long getAge(){
        return age;
    }

    /**
     * Sets the time of the temporal data
     * @param time The time
     */
    public void setTime(long time){
        this.time = time;
    }

}
