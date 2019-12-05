package com.synopsys.integration.pdf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

public class StringManagerTest {
    @Test
    public void testStringWidth() throws IOException {
        assertTrue(0 < StringManager.getStringWidth(PDType1Font.HELVETICA, 10.0f, "some text"));
    }

}
