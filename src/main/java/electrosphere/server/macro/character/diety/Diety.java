package electrosphere.server.macro.character.diety;

import electrosphere.engine.Globals;
import electrosphere.server.macro.character.data.CharacterData;
import electrosphere.server.macro.character.data.CharacterDataStrings;
import electrosphere.server.macro.symbolism.Symbol;
import electrosphere.server.macro.symbolism.SymbolMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Data defining this character as a diety
 */
public class Diety extends CharacterData {
    
    List<Symbol> symbols = new LinkedList<Symbol>();
    
    //TODO: eventually add function where we can pass intial symbol to seed rest of diety off of
    //this lets us create a "good" diety" and a "bad" diety to guarentee a more balanced pantheon

    /**
     * Constructor
     */
    private Diety(){
        super(CharacterDataStrings.DIETY);
    }
    
    public static Diety generateDiety(long seed){
        Random random = new Random();
        Diety rVal = new Diety();
        
        //TODO: eventually use bucket based rng system where previous choices affect chances on future ones
        //add symbols
        SymbolMap symbolMap = Globals.gameConfigCurrent.getSymbolMap();
        int numSymbolTypes = symbolMap.getSymbolismMap().size();
        int numSymbolsToAdd = 3 + Math.abs(random.nextInt()) % 5;
        for(int i = 0; i < numSymbolsToAdd; i++){
            Symbol potentialSymbol = symbolMap.getSymbolismMap().get(random.nextInt(numSymbolTypes));
            while(rVal.symbols.contains(potentialSymbol)){
                potentialSymbol = symbolMap.getSymbolismMap().get(random.nextInt(numSymbolTypes));
            }
            rVal.symbols.add(potentialSymbol);
        }
        
        return rVal;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }
    
    
    
}
