package com.blackducksoftware.integration.pdf;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class StringManagerTest {
    @Test
    public void testStringWidth() throws IOException {
        Assert.assertTrue(0 < StringManager.getStringWidth("some text"));
    }

}
