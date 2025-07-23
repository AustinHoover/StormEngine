package electrosphere.data.tutorial;

import java.util.List;

/**
 * A file that defines tutorial hints
 */
public class HintDefinition {
    
    //the list of all tutorial hints
    List<TutorialHint> hints;

    /**
     * Gets the list of all available tutorial hints
     * @return the list
     */
    public List<TutorialHint> getHints(){
        return hints;
    }

    /**
     * Gets a hint by its id
     * @param id the id of the hint
     * @return The hint if it exists, null otherwise
     */
    public TutorialHint getHintById(String id){
        for(TutorialHint hint : hints){
            if(hint.id.equals(id)){
                return hint;
            }
        }
        return null;
    }

}
