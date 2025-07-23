package electrosphere.server.macro.symbolism;

import java.util.List;

/**
 * A symbol
 */
public class Symbol {
    String name;
    List<SymbolismRelation> relations;

    public String getName() {
        return name;
    }

    public List<SymbolismRelation> getRelations() {
        return relations;
    }
    
    
    
}
