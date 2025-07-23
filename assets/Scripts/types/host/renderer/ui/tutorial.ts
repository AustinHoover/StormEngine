

/**
 * Tutorial ui related functions
 */
export interface TutorialUtils {

    /**
     * Shows a hint popup with contents defined at the hintId
     * Optionally allows passing a close callback
     * @param hintId The hint id in data
     * @param captureControls Instructs the engine to switch to ui controls from in-game controls
     * @param onClose Fired when the hint popup is closed
     */
    showTutorialHint: (hintId: string, captureControls: boolean, onClose?: () => void) => void

}

