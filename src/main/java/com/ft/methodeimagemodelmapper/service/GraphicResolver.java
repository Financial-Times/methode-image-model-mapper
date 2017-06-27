package com.ft.methodeimagemodelmapper.service;

import com.ft.methodeimagemodelmapper.model.EomFile;
import com.googlecode.pngtastic.core.PngException;
import com.googlecode.pngtastic.core.PngImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Optional;

public class GraphicResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphicResolver.class);
    private static final Charset LATIN_1 = Charset.forName("ISO-8859-1");
    private static final String TEXT_CHUNK = "tEXt";
    private static final String GRAPHIC_MEDIA_TYPE = "image/png";
    private static final String IMAGE_TYPE = "Image";
    private static final String GRAPHIC_TYPE = "Graphic";
    private static final String SOURCE_GRAPHIC_KEY = "ftimagetype";
    private static final String SOURCE_GRAPHIC_VALUE = "graphic";

    public String resolveType(final EomFile eomFile, final String mediaType, final String transactionId) {
        if (!GRAPHIC_MEDIA_TYPE.equals(mediaType)) {
            return IMAGE_TYPE;
        }
        if (isGraphicByMethodeMetadata(eomFile, transactionId) ||
                isGraphicByPngBinaryMetadata(eomFile, mediaType, transactionId)) {
            return GRAPHIC_TYPE;
        }
        return IMAGE_TYPE;
    }

    private boolean isGraphicByMethodeMetadata(final EomFile eomFile, final String transactionId) {
        if (eomFile.getAttributes() == null) {
            return false;
        }
        try {
            final DocumentBuilder documentBuilder = getDocumentBuilder();
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final Document attributesDocument = documentBuilder.parse(new InputSource(new StringReader(eomFile.getAttributes())));
            final String ftImageType = xpath.evaluate("/meta/picture/FTImageType", attributesDocument);
            return SOURCE_GRAPHIC_VALUE.equals(ftImageType);
        } catch (SAXException | IOException | XPathExpressionException | ParserConfigurationException ex) {
            LOGGER.warn("Failed retrieving attributes XML of image uuid={} transactionId={} {}", eomFile.getUuid(), transactionId, ex);
        }
        return false;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return documentBuilderFactory.newDocumentBuilder();
    }

    private boolean isGraphicByPngBinaryMetadata(final EomFile eomFile, final String mediaType, final String transactionId) {
        final PngImage pngImage;
        try {
            pngImage = new PngImage(new BufferedInputStream(new ByteArrayInputStream(eomFile.getValue())));
        } catch (PngException ex) {
            LOGGER.warn("Image has mediaType={} but wasn't recognized as true PNG file. uuid={} transactionId={}", mediaType, eomFile.getUuid(), transactionId);
            return false;
        }
        final Optional<Boolean> isGraphic = pngImage.getChunks()
                .stream()
                .filter(chunk -> TEXT_CHUNK.equals(chunk.getTypeString()))
                .findFirst()
                .map(chunk -> new String(chunk.getData(), LATIN_1).toLowerCase())
                .map(data -> data.contains(SOURCE_GRAPHIC_KEY) && data.contains(SOURCE_GRAPHIC_VALUE));
        return isGraphic.isPresent() && isGraphic.get();
    }
}
