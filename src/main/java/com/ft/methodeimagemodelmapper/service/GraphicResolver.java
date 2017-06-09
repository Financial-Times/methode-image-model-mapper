package com.ft.methodeimagemodelmapper.service;

import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngjException;
import com.ft.methodeimagemodelmapper.model.EomFile;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;

import static com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper.IMAGE_TYPE;

public class GraphicResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicResolver.class);
    private static final String GRAPHIC_MEDIATYPE = MethodeImageModelMapper.MEDIATYPE_PREFIX + "png";
    private static final String GRAPHIC_TYPE = "Graphic";
    private static final String SOURCE_GRAPHIC_KEY = "FTImageType";
    private static final String SORUCE_GRAPHIC_VALUE = "graphic";

    public String resolveType(final EomFile eomFile, final String mediaType, final String transactionId) {
        if (!GRAPHIC_MEDIATYPE.equals(mediaType)) {
            return IMAGE_TYPE;
        }
        final PngReader pngReader;
        try {
            pngReader = new PngReader(new BufferedInputStream(new ByteInputStream(eomFile.getValue(), eomFile.getValue().length)));
        } catch (PngjException ex) {
            LOGGER.warn("Image has mediaType={} but wasn't recognized as true PNG file. uuid={} transactionId={}", mediaType, eomFile.getUuid(), transactionId);
            return IMAGE_TYPE;
        }
        if (SORUCE_GRAPHIC_VALUE.equals(pngReader.getMetadata().getTxtForKey(SOURCE_GRAPHIC_KEY))) {
            return GRAPHIC_TYPE;
        }
        return IMAGE_TYPE;
    }
}
