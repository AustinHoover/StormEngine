package electrosphere.client.ui.menu;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.engine.Globals;
import electrosphere.logger.LoggerInterface;
import electrosphere.renderer.ui.ElementService;
import electrosphere.renderer.ui.elements.Div;
import electrosphere.test.annotations.UnitTest;

/**
 * Tests for the window utils
 */
public class WindowUtilsTests {
    
    @UnitTest
    public void closeWindow_ValidWindow_NotVisible(){
        String someWindowString = "test";
        LoggerInterface.initLoggers();
        Globals.elementService = new ElementService();
        Globals.elementService.init();
        Globals.elementService.registerWindow(someWindowString, Div.createDiv());

        WindowUtils.closeWindow(someWindowString);

        assertEquals(false,WindowUtils.windowIsVisible(someWindowString));
    }

    @UnitTest
    public void closeWindow_ValidWindow_NotRegistered(){
        String someWindowString = "test";
        LoggerInterface.initLoggers();
        Globals.elementService = new ElementService();
        Globals.elementService.init();
        Globals.elementService.registerWindow(someWindowString, Div.createDiv());

        WindowUtils.closeWindow(someWindowString);

        assertNull(Globals.elementService.getWindow(someWindowString));
    }

    @UnitTest
    public void closeWindow_ValidWindowAfterGlobalsReinit_NoThrow(){
        assertDoesNotThrow(()->{
            //setup
            String someWindowString = "test";
            Globals.initGlobals();
            Globals.elementService.registerWindow(someWindowString, Div.createDiv());

            //close
            WindowUtils.closeWindow(someWindowString);

            //reinit globals
            Globals.resetGlobals();
            Globals.initGlobals();
        });
    }

    @UnitTest
    public void closeWindow_ValidWindowAfterGlobalsReinit_NotRegistered(){
        //setup
        String someWindowString = "test";
        Globals.initGlobals();
        Globals.elementService.registerWindow(someWindowString, Div.createDiv());

        //close
        WindowUtils.closeWindow(someWindowString);

        //reinit globals
        Globals.resetGlobals();
        Globals.initGlobals();

        assertNull(Globals.elementService.getWindow(someWindowString));
    }

    @UnitTest
    public void closeWindow_ValidWindowAfterGlobalsReinit_ElementServiceZeroChildren(){
        //setup
        String someWindowString = "test";
        Globals.initGlobals();
        Globals.elementService.registerWindow(someWindowString, Div.createDiv());

        //close
        WindowUtils.closeWindow(someWindowString);

        // reinit globals
        Globals.resetGlobals();
        Globals.initGlobals();

        assertEquals(0,Globals.elementService.getWindowList().size());
    }

    // @UnitTest
    // public void setVisible_ValidWindowToFalseAfterGlobalsReinit_False(){
    //     //setup
    //     String someWindowString = "test";
    //     Globals.initGlobals();
    //     Globals.elementService.registerWindow(someWindowString, Div.createDiv());

    //     //visibility toggle
    //     WindowUtils.recursiveSetVisible(someWindowString,true);
    //     WindowUtils.recursiveSetVisible(someWindowString,false);

    //     //reinit globals
    //     Globals.resetGlobals();
    //     Globals.initGlobals();

    //     //register again
    //     Globals.initGlobals();
    //     Globals.elementService.registerWindow(someWindowString, Div.createDiv());

    //     //visibility toggle
    //     WindowUtils.recursiveSetVisible(someWindowString,true);
    //     WindowUtils.recursiveSetVisible(someWindowString,false);

    //     assertNull(!WindowUtils.windowIsVisible(someWindowString));
    // }

}
