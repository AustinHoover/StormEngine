import { Engine } from "/Scripts/types/engine";


/**
 * A hook that runs a function when a signal is fired in the engine
 */
export interface Hook {

    /**
     * The signal that triggers this hook in particular
     */
    readonly signal: string,

    /**
     * The function to call when the signal is fired
     */
    readonly callback: (engine: Engine, ...value: any) => void,

}
