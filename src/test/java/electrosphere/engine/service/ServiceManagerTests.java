package electrosphere.engine.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import electrosphere.test.annotations.UnitTest;
import electrosphere.logger.Logger;
import electrosphere.logger.LoggerInterface;

/**
 * Service manager tests
 */
public class ServiceManagerTests {

    @BeforeEach
    public void init(){
        LoggerInterface.loggerEngine = Mockito.mock(Logger.class);
    }

    @UnitTest
    public void testInit(){
        ServiceManager.create();
    }

    @UnitTest
    public void testRegisterService(){
        ServiceManager serviceManager = ServiceManager.create();
        Service service1Mock = Mockito.mock(Service.class);
        Service service1 = serviceManager.registerService(service1Mock);
        assertEquals(service1Mock,service1);
    }

    @UnitTest
    public void testRegisterTwoServices(){
        ServiceManager serviceManager = ServiceManager.create();
        Service service1Mock = Mockito.mock(Service.class);
        Service service1 = serviceManager.registerService(service1Mock);
        Service service2Mock = Mockito.mock(Service.class);
        Service service2 = serviceManager.registerService(service2Mock);
        assertEquals(service1Mock,service1);
        assertEquals(service2Mock,service2);
    }

    @UnitTest
    public void testRegisterManyServices(){
        ServiceManager serviceManager = ServiceManager.create();
        for(int i = 0; i < 10; i++){
            serviceManager.registerService(Mockito.mock(Service.class));
        }
        Service lastMock = Mockito.mock(Service.class);
        Service lastService = serviceManager.registerService(lastMock);
        assertEquals(lastMock,lastService);
    }

    @UnitTest
    public void testInstantiate(){
        ServiceManager serviceManager = ServiceManager.create();

        //register service
        Service service1Mock = Mockito.spy(Service.class);
        Service service1 = serviceManager.registerService(service1Mock);
        assertEquals(service1Mock,service1);

        //instantiate all services
        serviceManager.instantiate();

        //verify init was called
        Mockito.verify(service1Mock, Mockito.times(1)).init();
    }

    @UnitTest
    public void testDestroy(){
        ServiceManager serviceManager = ServiceManager.create();

        //register service
        Service service1Mock = Mockito.spy(Service.class);
        Service service1 = serviceManager.registerService(service1Mock);
        assertEquals(service1Mock,service1);

        //instantiate all services
        serviceManager.instantiate();

        //verify init was called
        Mockito.verify(service1Mock, Mockito.times(1)).init();

        //destroy
        serviceManager.destroy();

        //verify destroy was called
        Mockito.verify(service1Mock, Mockito.times(1)).destroy();
    }
    
}
