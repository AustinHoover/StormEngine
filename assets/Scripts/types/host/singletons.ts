
/**
 * The singletons from the host that are accessible in scripts
 */
export interface SingletonsMap {


    /**
     * Timekeeper that tracks engine time and frame count
     */
    readonly timekeeper?: any,

    /**
     * The object for the current player (if in single player non-headless client)
     */
    readonly currentPlayer?: any,

    /**
     * The scripts logger
     */
    readonly loggerScripts?: any,

    /**
     * The scripts engine
     */
    readonly scriptEngine?: any,

}
