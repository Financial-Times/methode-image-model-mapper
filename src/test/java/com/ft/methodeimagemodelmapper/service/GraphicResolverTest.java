package com.ft.methodeimagemodelmapper.service;

import com.ft.methodeimagemodelmapper.model.EomFile;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper.DEFAULT_MEDIATYPE;
import static com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper.MEDIATYPE_PREFIX;
import static org.junit.Assert.assertEquals;

public class GraphicResolverTest {

    private static byte[] SAMPLE_GRAPHIC;
    private static byte[] SAMPLE_PNG_IMAGE;
    private static byte[] SAMPLE_JPEG_IMAGE;

    private final GraphicResolver graphicResolver = new GraphicResolver();

    @BeforeClass
    public static void setUp() throws IOException {
        SAMPLE_GRAPHIC = Files.readAllBytes(Paths.get("src/test/resources/sample-image.png"));
        SAMPLE_PNG_IMAGE = Files.readAllBytes(Paths.get("src/test/resources/sample-image.png"));
        SAMPLE_JPEG_IMAGE = Files.readAllBytes(Paths.get("src/test/resources/sample-image.jpg"));
    }

    @Test
    public void testGraphicIsGraphic() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(SAMPLE_GRAPHIC).build();
        final String actualType = graphicResolver.resolveType(eomFile, MEDIATYPE_PREFIX + "png", "tid_test");
        assertEquals("Graphic", actualType);
    }

    @Test
    public void testPngImageIsImage() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(SAMPLE_PNG_IMAGE).build();
        final String actualType = graphicResolver.resolveType(eomFile, MEDIATYPE_PREFIX + "png", "tid_test");
        assertEquals("Image", actualType);
    }

    @Test
    public void testJpgImageIsImage() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(SAMPLE_JPEG_IMAGE).build();
        final String actualType = graphicResolver.resolveType(eomFile, DEFAULT_MEDIATYPE, "tid_test");
        assertEquals("Image", actualType);
    }

    @Test
    public void testGraphicWithJpegMediatypeIsImage() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(SAMPLE_GRAPHIC).build();
        final String actualType = graphicResolver.resolveType(eomFile, DEFAULT_MEDIATYPE, "tid_test");
        assertEquals("Image", actualType);
    }

    @Test
    public void testOtherMediaTypedImageIsImage() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(SAMPLE_JPEG_IMAGE).build();
        final String actualType = graphicResolver.resolveType(eomFile, MEDIATYPE_PREFIX + "bmp", "tid_test");
        assertEquals("Image", actualType);
    }

    @Test
    public void testUnrecognizedIsWarnedUponAndReturnedImage() throws Exception {
        final EomFile eomFile = new EomFile.Builder().withValue(new byte[]{12, 13, 14, 15, 16}).build();
        final String actualType = graphicResolver.resolveType(eomFile, MEDIATYPE_PREFIX + "png", "tid_test");
        assertEquals("Image", actualType);
    }
}
