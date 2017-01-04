package com.ft.methodeimagemodelmapper.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.methodeimagemodelmapper.MethodeImageModelMapperApplication;
import com.ft.methodeimagemodelmapper.configuration.MethodeImageModelMapperConfiguration;
import com.ft.methodeimagemodelmapper.configuration.ProducerConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class MethodeImageModelResourceCT {

    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String MAP_IMAGE_MODEL_URI = "http://localhost:16080/map";
    private static final String INGEST_IMAGE_MODEL_URI = "http://localhost:16080/ingest";
    private static final String INVALID_UUID = "Invalid uuid";
    private static final String CONTENT_TYPE_NOT_SUPPORTED = "Unsupported type - not an image.";

    private static MessageProducer producer = mock(MessageProducer.class);

    public static class StubMethodeImageModelMapperApplication extends MethodeImageModelMapperApplication {
        @Override
        protected MessageProducer configureMessageProducer(Environment environment, ProducerConfiguration config) {
            return producer;
        }
    }

    @ClassRule
    public static final DropwizardAppRule<MethodeImageModelMapperConfiguration> RULE =
            new DropwizardAppRule<>(StubMethodeImageModelMapperApplication.class,
                    "methode-image-model-mapper-test.yaml");

    private final Client client = new Client();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testImageModelComesThrough() throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-response.json");
        final String expectedResultJson = loadFile("sample-image-model-transformer-response.json");

        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaders().getFirst(CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        final String actualResultJson = response.getEntity(String.class);
        final JsonNode actualTree = objectMapper.readTree(actualResultJson);
        assertThat(actualTree, equalTo(objectMapper.readTree(expectedResultJson)));
    }

    @Test
    public void testImageModelComesThroughIfJsonNotComplete() throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-response-partial.json");
        final String expectedResultJson = loadFile("sample-image-model-transformer-response-partial.json");

        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.getHeaders().getFirst(CONTENT_TYPE), equalTo("application/json; charset=utf-8"));
        final String actualResultJson = response.getEntity(String.class);
        final JsonNode actualTree = objectMapper.readTree(actualResultJson);
        assertThat(actualTree, equalTo(objectMapper.readTree(expectedResultJson)));
    }

    @Test
    public void testGetImageModel400IfUuidInvalid() throws Exception {
        testEndpointReturns400WhenUuidIsInvalid(MAP_IMAGE_MODEL_URI);
    }

    @Test
    public void testMapModelShouldReturn400WhenContentIsNotSupported() throws Exception {
        final String sourceApiJson = loadFile("native-not-image-model.json");
        final ClientResponse response = client.resource(MAP_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(400));
        final String expectedMessage = "{\"message\":\"" + CONTENT_TYPE_NOT_SUPPORTED + "\"}";
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
        //// TODO: 1/3/2017 check this
        assertThat(response.getStatus(), equalTo(204));
        verify(producer).send(sent.capture());
    }

    @Test
    public void testIngestImageModelShouldReturn400WhenUuidIsInvalid() throws Exception {
        testEndpointReturns400WhenUuidIsInvalid(INGEST_IMAGE_MODEL_URI);
    }

    private void testEndpointReturns400WhenUuidIsInvalid(String endpoint) throws Exception {
        final String sourceApiJson = loadFile("sample-source-api-invalid-uuid.json");
        final ClientResponse response = client.resource(endpoint)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(400));
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

        assertThat(response.getStatus(), equalTo(204));
        verifyZeroInteractions(producer);
    }

    @Test
    public void testIngestModelShouldNotSendMessageWhenImageBodyIsEmpty() throws Exception {
        final String sourceApiJson = loadFile("native-empty-payload-image-model.json");
        final ClientResponse response = client.resource(INGEST_IMAGE_MODEL_URI)
                .header(TRANSACTION_ID_HEADER, TRANSACTION_ID)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, sourceApiJson);

        assertThat(response.getStatus(), equalTo(204));
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

}
