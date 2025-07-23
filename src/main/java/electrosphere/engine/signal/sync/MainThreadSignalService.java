package electrosphere.engine.signal.sync;

import electrosphere.engine.loadingthreads.MainMenuLoading;
import electrosphere.engine.signal.Signal;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.engine.signal.SignalServiceImpl;
import electrosphere.logger.LoggerInterface;

public class MainThreadSignalService extends SignalServiceImpl {
    
    /**
     * Constructor
     */
    public MainThreadSignalService() {
        super(
            "MainThreadSignalService",
            new SignalType[]{
                SignalType.ENGINE_RETURN_TO_TITLE,
                SignalType.ENGINE_SYNCHRONOUS_CODE,
            }
        );
    }

    @Override
    public boolean handle(Signal signal){
        boolean rVal = false;
        switch(signal.getType()){
            case ENGINE_RETURN_TO_TITLE: {
                MainMenuLoading.returnToMainMenu(null);
                rVal = true;
            } break;
            case ENGINE_SYNCHRONOUS_CODE: {
                ((Runnable)signal.getData()).run();
                rVal = true;
            } break;
            default: {
                LoggerInterface.loggerEngine.WARNING("MainThreadSignalService received signal that it does not have handling for! " + signal);
            } break;
        }
        return rVal;
    }

}
