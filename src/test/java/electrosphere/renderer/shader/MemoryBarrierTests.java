package electrosphere.renderer.shader;

import electrosphere.renderer.shader.MemoryBarrier.Barrier;
import electrosphere.test.annotations.IntegrationTest;
import electrosphere.test.template.RenderingTestTemplate;

/**
 * Tests for memory barrier operations
 */
public class MemoryBarrierTests extends RenderingTestTemplate {
    
    @IntegrationTest
    public void testGLMemoryBarrier(){
        MemoryBarrier.glMemoryBarrier(Barrier.GL_COMMAND_BARRIER_BIT);
    }

}
