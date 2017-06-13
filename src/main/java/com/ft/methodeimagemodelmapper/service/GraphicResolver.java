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

import static com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper.IMAGE_TYPE;

public class GraphicResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicResolver.class);
    private static final Charset LATIN_1 = Charset.forName("ISO-8859-1");
    private static final String TEXT_CHUNK = "tEXt";
    private static final String GRAPHIC_MEDIATYPE = MethodeImageModelMapper.MEDIATYPE_PREFIX + "png";
    private static final String GRAPHIC_TYPE = "Graphic";
    private static final String SOURCE_GRAPHIC_KEY = "FTImageType";
    private static final String SOURCE_GRAPHIC_KEY_LOWERCASE = SOURCE_GRAPHIC_KEY.toLowerCase();
    private static final String SORUCE_GRAPHIC_VALUE = "graphic";
    private static final String SORUCE_GRAPHIC_VALUE_LOWERCASE = SORUCE_GRAPHIC_VALUE.toLowerCase();

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
                .map(chunk -> new String(chunk.getData(), LATIN_1))
                .map(data -> data.toLowerCase().contains(SOURCE_GRAPHIC_KEY_LOWERCASE) && data.toLowerCase().contains(SORUCE_GRAPHIC_VALUE_LOWERCASE));
        if (isGraphic.isPresent() && isGraphic.get()) {
            return GRAPHIC_TYPE;
        }
        return IMAGE_TYPE;
    }
}
