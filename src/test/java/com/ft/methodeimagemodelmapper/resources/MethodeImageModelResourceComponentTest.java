package com.ft.methodeimagemodelmapper.resources;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.content.model.Content;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.methodeimagemodelmapper.MethodeImageModelMapperApplication;
import com.ft.methodeimagemodelmapper.configuration.MethodeImageModelMapperConfiguration;
import com.ft.methodeimagemodelmapper.configuration.ProducerConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;

public class MethodeImageModelResourceComponentTest {

    @ClassRule
    public static final DropwizardAppRule<MethodeImageModelMapperConfiguration> RULE =
            new DropwizardAppRule<>(StubMethodeImageModelMapperApplication.class,
                    "methode-image-model-mapper-test.yaml");
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String MAP_IMAGE_MODEL_URI = "http://localhost:16080/map";
    private static final String INGEST_IMAGE_MODEL_URI = "http://localhost:16080/ingest";
    private static final String INVALID_UUID = "Invalid uuid";
    private static MessageProducer producer = mock(MessageProducer.class);
    private final Client client = new Client();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testImageModelComesThrough() throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-response.json");
        final String expectedResultJson = loadFile("sample-image-model-transformer-response.json");
        final Content expectedContent = objectMapper.reader(Content.class).readValue(expectedResultJson);

        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaders().getFirst(CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        final String actualResultJson = response.getEntity(String.class);
        final Content actualContent = objectMapper.reader(Content.class).readValue(actualResultJson);
        assertThat(actualContent.getUuid(), equalTo(expectedContent.getUuid()));
        assertThat(actualContent.getTitle(), equalTo(expectedContent.getTitle()));
        assertThat(actualContent.getAlternativeTitles(), equalTo(expectedContent.getAlternativeTitles()));
        assertThat(actualContent.getIdentifiers(), equalTo(expectedContent.getIdentifiers()));
        assertThat(actualContent.getPublishedDate(), equalTo(expectedContent.getPublishedDate()));
        assertThat(actualContent.getDescription(), equalTo(expectedContent.getDescription()));
        assertThat(actualContent.getMediaType(), equalTo(expectedContent.getMediaType()));
        assertThat(actualContent.getPixelHeight(), equalTo(expectedContent.getPixelHeight()));
        assertThat(actualContent.getPixelWidth(), equalTo(expectedContent.getPixelWidth()));
        assertThat(actualContent.getExternalBinaryUrl(), equalTo(expectedContent.getExternalBinaryUrl()));
        assertThat(actualContent.getPublishReference(), equalTo(expectedContent.getPublishReference()));
        assertThat(actualContent.getFirstPublishedDate(), equalTo(expectedContent.getFirstPublishedDate()));
        assertThat(actualContent.getCanBeDistributed(), equalTo(expectedContent.getCanBeDistributed()));
        assertThat(actualContent.getCanBeSyndicated(), equalTo(expectedContent.getCanBeSyndicated()));
        assertThat(actualContent.getRightsGroup(), equalTo(expectedContent.getRightsGroup()));
        assertThat(actualContent.getMasterSource(), notNullValue());
        assertThat(actualContent.getMasterSource().getAuthority(), equalTo(expectedContent.getMasterSource().getAuthority()));
        assertThat(actualContent.getMasterSource().getIdentifierValue(), equalTo(expectedContent.getMasterSource().getIdentifierValue()));
    }

    @Test
    public void testImageModelComesThroughIfJsonNotComplete() throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-response-partial.json");
        final String expectedResultJson = loadFile("sample-image-model-transformer-response-partial.json");
        final Content expectedContent = objectMapper.reader(Content.class).readValue(expectedResultJson);

        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaders().getFirst(CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        final String actualResultJson = response.getEntity(String.class);
        final Content actualContent = objectMapper.reader(Content.class).readValue(actualResultJson);
        assertThat(actualContent.getUuid(), equalTo(expectedContent.getUuid()));
        assertThat(actualContent.getTitle(), equalTo(expectedContent.getTitle()));
        assertThat(actualContent.getAlternativeTitles(), equalTo(expectedContent.getAlternativeTitles()));
        assertThat(actualContent.getIdentifiers(), equalTo(expectedContent.getIdentifiers()));
        assertThat(actualContent.getPublishedDate(), equalTo(expectedContent.getPublishedDate()));
        assertThat(actualContent.getDescription(), equalTo(expectedContent.getDescription()));
        assertThat(actualContent.getMediaType(), equalTo(expectedContent.getMediaType()));
        assertThat(actualContent.getPixelHeight(), equalTo(expectedContent.getPixelHeight()));
        assertThat(actualContent.getPixelWidth(), equalTo(expectedContent.getPixelWidth()));
        assertThat(actualContent.getPublishReference(), equalTo(expectedContent.getPublishReference()));
        assertThat(actualContent.getFirstPublishedDate(), equalTo(expectedContent.getFirstPublishedDate()));
        assertThat(actualContent.getCanBeDistributed(), equalTo(expectedContent.getCanBeDistributed()));
        assertThat(actualContent.getCanBeSyndicated(), equalTo(expectedContent.getCanBeSyndicated()));
        assertThat(actualContent.getRightsGroup(), equalTo(expectedContent.getRightsGroup()));
        assertThat(actualContent.getMasterSource(), equalTo(expectedContent.getMasterSource()));
    }

    @Test
    public void testGetImageModel400IfUuidInvalid() throws Exception {
        testEndpointReturns422WhenUuidIsInvalid(MAP_IMAGE_MODEL_URI);
    }

    @Test
    public void testMapModelShouldReturn422WhenContentIsNotSupported() throws Exception {
        final String sourceApiJson = loadFile("native-not-image-model.json");
        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(422));
        final String expectedMessage = "{\"message\":\"" + "Content cannot be mapped." + "\"}";
        assertThat(response.getEntity(String.class), equalTo(expectedMessage));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testIngestImageModelShouldReturn204WhenValidContent() throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-response.json");

        final ClientResponse response = client.resource(INGEST_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        ArgumentCaptor<List> sent = ArgumentCaptor.forClass(List.class);
        assertThat(response.getStatus(), equalTo(204));
        verify(producer).send(sent.capture());
    }

    @Test
    public void testIngestImageModelShouldReturn400WhenUuidIsInvalid() throws Exception {
        testEndpointReturns422WhenUuidIsInvalid(INGEST_IMAGE_MODEL_URI);
    }

    private void testEndpointReturns422WhenUuidIsInvalid(String endpoint) throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-invalid-uuid.json");
        final ClientResponse response = client.resource(endpoint)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(422));
        final String expectedMessage = "{\"message\":\"" + INVALID_UUID + "\"}";
        assertThat(response.getEntity(String.class), equalTo(expectedMessage));
    }

    @Test
    public void testIngestModelShouldNotSendMessageWhenContentTypeIsNotSupported() throws Exception {
        final String sourceApiJson = loadFile("native-not-image-model.json");
        final ClientResponse response = client.resource(INGEST_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(422));
        verifyZeroInteractions(producer);
    }

    @Test
    public void testIngestModelShouldNotSendMessageWhenImageBodyIsEmpty() throws Exception {
        final String sourceApiJson = loadFile("native-empty-payload-image-model.json");
        final ClientResponse response = client.resource(INGEST_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(422));
        verifyZeroInteractions(producer);
    }

    private String loadFile(final String fileName) throws Exception {
        URL url = getClass().getClassLoader().getResource(fileName);
        if (url == null) {
            return StringUtils.EMPTY;
        }
        final URI uri = url.toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
    }

    public static class StubMethodeImageModelMapperApplication extends MethodeImageModelMapperApplication {
        @Override
        protected MessageProducer configureMessageProducer(Environment environment, ProducerConfiguration config) {
            return producer;
        }
    }

}
