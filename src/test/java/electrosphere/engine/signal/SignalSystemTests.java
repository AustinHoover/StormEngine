package electrosphere.engine.signal;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import electrosphere.test.annotations.UnitTest;
import electrosphere.engine.signal.Signal.SignalType;
import electrosphere.logger.Logger;
import electrosphere.logger.LoggerInterface;

/**
 * Testing the signal system
 */
public class SignalSystemTests {

    ArgumentCaptor<Signal> signalCaptor;

    @BeforeEach
    public void init(){
        LoggerInterface.loggerEngine = Mockito.mock(Logger.class);
        signalCaptor = ArgumentCaptor.forClass(Signal.class);
    }
    

    @UnitTest
    public void testInit(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();
    }

    @UnitTest
    public void testRegister(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();

        SignalService service = Mockito.mock(SignalService.class);

        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service);
    }

    
    @UnitTest
    public void testPost(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();

        SignalService service = Mockito.mock(SignalService.class);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service);

        signalSystem.post(SignalType.YOGA_APPLY);

        Mockito.verify(service, Mockito.times(1)).post(signalCaptor.capture());
    }

    @UnitTest
    public void testMultiPost(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();

        SignalService service = Mockito.mock(SignalService.class);
        SignalService service2 = Mockito.mock(SignalService.class);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service2);

        signalSystem.post(SignalType.YOGA_APPLY);

        Mockito.verify(service, Mockito.times(1)).post(signalCaptor.capture());
        Mockito.verify(service2, Mockito.times(1)).post(signalCaptor.capture());
    }

    @UnitTest
    public void testPostFiltering(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();

        SignalService service = Mockito.mock(SignalService.class);
        SignalService service2 = Mockito.mock(SignalService.class);
        SignalService service3 = Mockito.mock(SignalService.class);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service2);
        signalSystem.registerServiceToSignal(SignalType.ENGINE_SHUTDOWN, service3);

        signalSystem.post(SignalType.YOGA_APPLY);

        Mockito.verify(service, Mockito.times(1)).post(signalCaptor.capture());
        Mockito.verify(service2, Mockito.times(1)).post(signalCaptor.capture());
        Mockito.verify(service3, Mockito.never()).post(signalCaptor.capture());
    }


    @UnitTest
    public void testDeregister(){
        SignalSystem signalSystem = new SignalSystem();
        signalSystem.init();

        SignalService service = Mockito.mock(SignalService.class);
        signalSystem.registerServiceToSignal(SignalType.YOGA_APPLY, service);
        signalSystem.deregisterServiceToSignal(SignalType.YOGA_APPLY, service);
        signalSystem.post(SignalType.YOGA_APPLY);

        Mockito.verify(service, Mockito.never()).post(signalCaptor.capture());
    }

}
