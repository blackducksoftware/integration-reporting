package com.synopsys.integration.pdf;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class StringManagerTest {
    @Test
    public void testStringWidth() throws IOException {
        assertTrue(0 < StringManager.getStringWidth("some text"));
    }

}
