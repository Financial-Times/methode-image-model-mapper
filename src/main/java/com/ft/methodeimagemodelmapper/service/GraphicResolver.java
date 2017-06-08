package com.ft.methodeimagemodelmapper.service;

import ar.com.hjg.pngj.PngReader;
import com.ft.methodeimagemodelmapper.model.EomFile;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;

public class GraphicResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicResolver.class);
    private static final String GRAPHIC_TYPE = "Graphic";
    private static final String SOURCE_GRAPHIC_KEY = "FTImageType";
    private static final String SORUCE_GRAPHIC_VALUE = "graphic";

    public String resolveType(final EomFile eomFile, final String mediaType) {
        if (MethodeImageModelMapper.DEFAULT_MEDIATYPE.equals(mediaType)) {
            return MethodeImageModelMapper.IMAGE_TYPE;
        }
        LOGGER.info("PNG opening uuid={}", eomFile.getUuid());
        final PngReader pngr = new PngReader(new BufferedInputStream(new ByteInputStream(eomFile.getValue(), eomFile.getValue().length)));
        LOGGER.info("pnginfo={}", pngr.imgInfo);
        final String ftImageType = pngr.getMetadata().getTxtForKey(SOURCE_GRAPHIC_KEY);
        LOGGER.info("ftImageType={}", ftImageType);
        if (SORUCE_GRAPHIC_VALUE.equals(ftImageType)) {
            return GRAPHIC_TYPE;
        }
        return MethodeImageModelMapper.IMAGE_TYPE;
    }
}
