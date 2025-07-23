package electrosphere.server.macro.civilization.culture.religion;

/**
 * Story data strings
 */
public class StoryDataStrings {
    /*
    FABLE,   //a story including animals/things that explains a moral
        MYTH,    //an explanation for some fact
        PARABLE, //a story strictly starring humans that explains a moral
    */
    public static final String STORY_TYPE = "storyType";
    public static final String STORY_TYPE_FABLE = "fable";
    public static final String STORY_TYPE_MYTH = "myth";
    public static final String STORY_TYPE_PARABLE = "parable";
    
    
    public static final String MYTH_GOAL = "mythGoal"; //what is the goal of the myth?
    public static final String MYTH_GOAL_CREATE = "creationism"; //the goal is to tell of the creation of the world
    
    
    public static final String MYTH_CREATE_MECHANISM = "creationismMechanism"; //what is the mechanism that the world is created by in the myth
    public static final String CREATE_MECH_FROM_NOTHING = "fromNothing"; //there was nothing, and then the primary diety created the world
    public static final String CREATE_MECH_START_TIME = "startTime"; //everything that existed at the start was frozen, and then time began
    public static final String CREATE_MECH_DIVER = "diver"; //a primordial being dives into something and retrieves the world
    public static final String CREATE_MECH_EMERGENCE = "emergence"; //primordial entities morph through stages until they're the current world
    public static final String CREATE_MECH_DISMEMBERMENT = "dismemberment"; //some priomordial being is dismembered and the remains are the world
    
}
