package electrosphere.renderer.buffer;

import static org.junit.jupiter.api.Assertions.*;

import electrosphere.engine.Globals;
import electrosphere.renderer.OpenGLState;
import electrosphere.renderer.buffer.BufferEnums.BufferAccess;
import electrosphere.renderer.buffer.BufferEnums.BufferUsage;
import electrosphere.test.annotations.UnitTest;
import electrosphere.test.template.RenderingTestTemplate;

/**
 * Tests for the shader storage buffer
 */
public class ShaderStorageBufferTests extends RenderingTestTemplate {
    
    @UnitTest
    public void test_Constructor_NoThrow(){
        assertDoesNotThrow(() -> {
            new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
        });
    }

    @UnitTest
    public void test_Constructor_JavaSideCapacity_10(){
        ShaderStorageBuffer buffer = new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
        assertEquals(10, buffer.getBuffer().limit());
    }

    @UnitTest
    public void test_destroy_NoThrow(){
        assertDoesNotThrow(() -> {
            ShaderStorageBuffer buffer = new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
            buffer.destroy();
        });
    }

    @UnitTest
    public void test_destroyTwice_NoThrow(){
        assertDoesNotThrow(() -> {
            ShaderStorageBuffer buffer = new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
            buffer.destroy();
            buffer.destroy();
        });
    }

    @UnitTest
    public void test_upload_NoThrow(){
        assertDoesNotThrow(() -> {
            ShaderStorageBuffer buffer = new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
            buffer.upload(Globals.renderingEngine.getOpenGLState());
        });
    }

    @UnitTest
    public void test_bind_NoThrow(){
        assertDoesNotThrow(() -> {
            ShaderStorageBuffer buffer = new ShaderStorageBuffer(10, BufferUsage.STATIC, BufferAccess.READ);
            OpenGLState openGLState = Globals.renderingEngine.getOpenGLState();
            openGLState.glBindBufferBase(0, buffer);
        });
    }

}
