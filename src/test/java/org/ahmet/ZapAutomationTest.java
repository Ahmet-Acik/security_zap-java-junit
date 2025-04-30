package org.ahmet;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZapAutomationTest {

    @Test
    public void testZapAutomation() {
        // Validate constants using getter methods
        assertEquals("localhost", ZapAutomation.getZapAddress());
        assertEquals(8080, ZapAutomation.getZapPort());
    }
}