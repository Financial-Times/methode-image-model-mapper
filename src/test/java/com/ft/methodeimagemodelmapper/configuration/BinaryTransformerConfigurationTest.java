package com.ft.methodeimagemodelmapper.configuration;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class BinaryTransformerConfigurationTest {

    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";

    private BinaryTransformerConfiguration binaryTransformerConfiguration;

    @Before
    public void setUp() {
        String hostAddress = "localhost:8080";
        String urlAddress = "/image/binary/%s";
        binaryTransformerConfiguration = new BinaryTransformerConfiguration(hostAddress, urlAddress);
    }

    @Test
    public void testBuildInternalDataUrl() {
        assertEquals(binaryTransformerConfiguration.buildInternalDataUrl(UUID), "http://localhost:8080/image/binary/d7625378-d4cd-11e2-bce1-002128161462");
    }
}
