package electrosphere.server.datacell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import electrosphere.test.annotations.FastTest;
import electrosphere.test.annotations.UnitTest;

/**
 * Unit tests for viewport data cell manager
 */
public class ViewportDataCellManagerUnitTests {
    
    @UnitTest
    @FastTest
    public void serverDataCell_isReady_true(){
        ViewportDataCellManager manager = ViewportDataCellManager.create(null);
        ServerDataCell dataCell = manager.getCellAtWorldPosition(null);
        assertEquals(true, dataCell.isReady());
    }

}
