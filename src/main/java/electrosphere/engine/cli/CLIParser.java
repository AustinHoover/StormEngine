package electrosphere.engine.cli;

import java.util.Arrays;
import java.util.List;

import electrosphere.engine.EngineState;
import electrosphere.logger.LoggerInterface;
import electrosphere.logger.Logger.LogLevel;

public class CLIParser {
    
    public static void parseCLIArgs(String args[]){
        List<String> argList = Arrays.asList(args);
        for(int i = 0; i < argList.size(); i++){
            String argCurrent = argList.get(i);
            switch(argCurrent){
                case "--headless": {
                    EngineState.EngineFlags.RUN_CLIENT = false;
                    EngineState.EngineFlags.RUN_SERVER = true;
                    EngineState.EngineFlags.HEADLESS = true;
                } break;
                case "--maxLogs": {
                    LoggerInterface.setInitLogLevel(LogLevel.LOOP_DEBUG);
                } break;
            }
        }
        //check properties
        if(System.getProperty("maxLogs") != null){
            LoggerInterface.setInitLogLevel(LogLevel.LOOP_DEBUG);
        }
    }
    
}
