package com.ft.methodeimagemodelmapper.service;

import com.ft.methodeimagemodelmapper.model.EomFile;
import com.googlecode.pngtastic.core.PngException;
import com.googlecode.pngtastic.core.PngImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class GraphicResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicResolver.class);
    private static final Charset LATIN_1 = Charset.forName("ISO-8859-1");
    private static final String TEXT_CHUNK = "tEXt";
    private static final String GRAPHIC_MEDIATYPE = "image/png";
    private static final String IMAGE_TYPE = "Image";
    private static final String GRAPHIC_TYPE = "Graphic";
    private static final String SOURCE_GRAPHIC_KEY = "ftimagetype";
    private static final String SORUCE_GRAPHIC_VALUE = "graphic";

    public String resolveType(final EomFile eomFile, final String mediaType, final String transactionId) {
        if (!GRAPHIC_MEDIATYPE.equals(mediaType)) {
            return IMAGE_TYPE;
        }
        final PngImage pngImage;
        try {
            pngImage = new PngImage(new BufferedInputStream(new ByteArrayInputStream(eomFile.getValue())));
        } catch (PngException ex) {
            LOGGER.warn("Image has mediaType={} but wasn't recognized as true PNG file. uuid={} transactionId={}", mediaType, eomFile.getUuid(), transactionId);
            return IMAGE_TYPE;
        }
        final Optional<Boolean> isGraphic = pngImage.getChunks()
                .stream()
                .filter(chunk -> TEXT_CHUNK.equals(chunk.getTypeString()))
                .findFirst()
                .map(chunk -> new String(chunk.getData(), LATIN_1).toLowerCase())
                .map(data -> data.contains(SOURCE_GRAPHIC_KEY) && data.contains(SORUCE_GRAPHIC_VALUE));
        if (isGraphic.isPresent() && isGraphic.get()) {
            return GRAPHIC_TYPE;
        }
        return IMAGE_TYPE;
    }
}
