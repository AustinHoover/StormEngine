package electrosphere.test.integration;

import electrosphere.test.annotations.IntegrationSetup;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.engine.EngineState;
import electrosphere.engine.profiler.Profiler;
import electrosphere.net.NetUtils;

public class StartupTest {

    @IntegrationSetup
    public void testStartupHeadless(){
        EngineState.EngineFlags.RUN_CLIENT = false;
        EngineState.EngineFlags.RUN_SERVER = true;
        EngineState.EngineFlags.HEADLESS = true;
        Profiler.PROFILE = false;
        NetUtils.setPort(0);
        // Main.startUp();
        // Main.mainLoop(1);
    }

    @IntegrationTest
    public void testEmpty() {
    }

}
