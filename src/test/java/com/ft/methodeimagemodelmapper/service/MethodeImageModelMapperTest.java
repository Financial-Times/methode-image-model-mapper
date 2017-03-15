package com.ft.methodeimagemodelmapper.service;

import com.ft.content.model.Content;
import com.ft.content.model.Distribution;
import com.ft.methodeimagemodelmapper.configuration.BinaryTransformerConfiguration;
import com.ft.methodeimagemodelmapper.exception.MethodeContentNotSupportedException;
import com.ft.methodeimagemodelmapper.exception.TransformationException;
import com.ft.methodeimagemodelmapper.model.EomFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MethodeImageModelMapperTest {

    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";
    private static final String METHODE_IDENTIFIER_AUTHORITY = "http://api.ft.com/system/FTCOM-METHODE";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final Date LAST_MODIFIED_DATE = new Date(300L);
    private static final String FORMAT_UNSUPPORTED = "%s is not an %s.";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private MethodeImageModelMapper methodeImageModelMapper;

    @Before
    public void setUP() {
        methodeImageModelMapper = new MethodeImageModelMapper(
                new BinaryTransformerConfiguration("localhost:8080", "/image/binary/%s"),
                "http://com.ft.imagepublish.int.s3.amazonaws.com/");
    }

    @Test
    public void testTransformImageThrowsIfTypeNotImage() {
        final EomFile eomFile = new EomFile(UUID, "article", null, "attributes", "workflow", "sysattributes", "usageTickets", LAST_MODIFIED_DATE);
        exception.expect(MethodeContentNotSupportedException.class);
        exception.expectMessage(String.format(FORMAT_UNSUPPORTED, UUID, "Image"));

        methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);
    }

    @Test
    public void testTransformsImageCorrectly() throws Exception {
        final EomFile eomFile = createSampleMethodeImage();

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(UUID));
        assertThat(content.getTitle(), equalTo("Fruits of the soul"));
        assertThat(content.getDescription(), equalTo("Picture with fruits"));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPixelWidth(), equalTo(2048));
        assertThat(content.getPixelHeight(), equalTo(1152));
        assertThat(content.getPublishedDate(), equalTo(new Date(1412088300000l)));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
        assertThat(content.getFirstPublishedDate(), equalTo(new Date(1412088300000l)));
        assertThat(content.getCanBeDistributed(), equalTo(Distribution.VERIFY));
    }

    public EomFile createSampleMethodeImage() throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        return new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);
    }

    public String loadFile(final String filename) throws Exception {
        final URI uri = getClass().getClassLoader().getResource(filename).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
    }

    @Test
    public void shouldUseOnlineSourceForCopyrightFirst() throws Exception {
        final EomFile eomFile = createSampleMethodeImage();

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getCopyright().getNotice(), equalTo("© Bloomberg News"));
    }

    @Test
    public void shouldUseManualSourceForCopyrightWhenOnlineSourceIsBlank() throws Exception {
        String attributes = loadFile("sample-attributes.xml");
        attributes = attributes.replace("<online-source>Bloomberg News</online-source>", "<online-source> </online-source>");
        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getCopyright().getNotice(), equalTo("© Some manual source here"));
    }

    @Test
    public void shouldReturnNullCopyrightWhenBothSourcesBlank() throws Exception {
        String attributes = loadFile("sample-attributes.xml");
        attributes = attributes.replace("<online-source>Bloomberg News</online-source>", "<online-source> </online-source>");
        attributes = attributes.replace("<manual-source>Some manual source here</manual-source>", "<manual-source />");

        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getCopyright(), nullValue());
    }

    @Test
    public void testTransformsImageCorrectlyIfWidthHeightDateIncorrect() throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml")
                .replace("2048", "two thousand").replace("1152", "one thousand");
        final String usageTickets = loadFile("sample-usage-tickets.xml").replace("20140930144500", "my birthday");
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(UUID));
        assertThat(content.getTitle(), equalTo("Fruits of the soul"));
        assertThat(content.getDescription(), equalTo("Picture with fruits"));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPixelWidth(), nullValue());
        assertThat(content.getPixelHeight(), nullValue());
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test
    public void testTransformImageNoExceptionIfUnrelatedXml() throws Exception {
        final String attributes = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<!DOCTYPE meta SYSTEM \"/SysConfig/Classify/FTImages/classify.dtd\">" +
                "<meta>empty</meta>";
        final String systemAttributes = "<props>empty as well</props>";
        final String usageTickets = "<html>empty as well</html>";
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(UUID));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test
    public void testTransformImageNoExceptionIfEmptyAttributes() throws Exception {
        final EomFile eomFile = new EomFile(UUID, "Image", null, "", "", "", "", null);

        final Content content = methodeImageModelMapper.mapImageModel(eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(UUID));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test(expected = TransformationException.class)
    public void testTransformAndHandleExceptionsThrowsTransformationException() {
        final EomFile eomFile = new EomFile(UUID, "Image", null, "", "", "", "", null);
        methodeImageModelMapper.transformAndHandleExceptions(eomFile, () -> {
            throw new IOException();
        });
    }

}
