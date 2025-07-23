package electrosphere.server.saves;

/**
 * Top level save object that stores information about the save
 */
public class Save {
    /*
    FOR FUTURE USE
    idea is if we want to dump a bunch of like managers into a json file
    we can put them all in here then serialize this instead
    or smthn
    */

    //the version of the game
    String versionString;

    //the time the save was created
    String timeCreated;

    //The name of the save
    String name;

    /**
     * Constructor
     */
    public Save(String name){
        this.name = name;
        versionString = "0.0.1";
        timeCreated = System.currentTimeMillis() + "";
    }

    /**
     * Gets the name of the save
     * @return The name
     */
    public String getName(){
        return name;
    }

}
