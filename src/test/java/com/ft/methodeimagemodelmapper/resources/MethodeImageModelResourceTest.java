package com.ft.methodeimagemodelmapper.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.api.jaxrs.errors.ErrorEntity;
import com.ft.api.jaxrs.errors.WebApplicationClientException;
import com.ft.api.jaxrs.errors.WebApplicationServerException;
import com.ft.content.model.Content;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.methodeimagemodelmapper.exception.MethodeContentNotSupportedException;
import com.ft.methodeimagemodelmapper.exception.TransformationException;
import com.ft.methodeimagemodelmapper.messaging.MessageProducingContentMapper;
import com.ft.methodeimagemodelmapper.model.EomFile;
import com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper;
import com.ft.methodeimagemodelmapper.validation.PublishingValidator;
import com.ft.methodeimagemodelmapper.validation.UuidValidator;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MethodeImageModelResourceTest {

    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final String UUID = "bd541104-105b-4d00-8304-a87f39460c0a";
    private static final Date LAST_MODIFIED_DATE = new Date(300L);
    private static final String INVALID_UUID = "Invalid uuid";
    private static final String CONTENT_TYPE_NOT_SUPPORTED = "Unsupported type - not an image.";
    private static final String CONTENT_CANNOT_BE_MAPPED = "Content cannot be mapped.";
    private static final String UNABLE_TO_WRITE_JSON_MESSAGE = "Unable to write JSON for message";
    private static final UriBuilder URI_BUILDER = UriBuilder.fromUri("http://www.example.org/content").path("{uuid}");
    private static final String SYSTEM_ID = "junit_system";

    @Mock
    private MethodeImageModelMapper imageModelMapper;

    @Mock
    private HttpHeaders headers;

    @Mock
    private MessageProducer producer;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private EomFile file;

    private MethodeImageModelResource resource;

    private MessageProducingContentMapper contentMapper;

    @Before
    public void setup() {
        contentMapper = new MessageProducingContentMapper(imageModelMapper, new ObjectMapper(), SYSTEM_ID,
                producer, URI_BUILDER);

        resource = new MethodeImageModelResource(imageModelMapper, contentMapper, new UuidValidator(), new PublishingValidator());
        when(headers.getRequestHeader(TRANSACTION_ID_HEADER)).thenReturn(Collections.singletonList(TRANSACTION_ID));
        byte[] image = new byte[20];
        new Random().nextBytes(image);
        file = new EomFile(UUID, "Image", image, "attributes", "workflow",
                "sysattributes", "usageTickets", LAST_MODIFIED_DATE);
    }

    @Test
    public void getImageModelShouldReturn200IfUuidAndTransactionIdOk() {
        final Content expectedContent = Content.builder().withUuid(java.util.UUID.fromString(UUID))
                .withPublishReference(TRANSACTION_ID).build();
        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenReturn(expectedContent);

        final Content content = resource.mapImageModel(file, headers);

        assertThat(content.getUuid(), equalTo(UUID));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test
    public void getImageModelShouldReturn400IfUuidInvalid() {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(INVALID_UUID));
        String invalidUuid = "someInvalidUuid";
        EomFile eomFile = new EomFile(invalidUuid, "image", null, "attributes",
                "workflow", "sysattributes", "usageTickets", LAST_MODIFIED_DATE);
        when(imageModelMapper.mapImageModel(eq(eomFile), eq(TRANSACTION_ID), any(Date.class))).thenThrow(new IllegalArgumentException());

        resource.mapImageModel(eomFile, headers);
    }

    @Test
    public void getImageModelShouldReturn400IfContentTypeNotImage() throws IOException {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(CONTENT_TYPE_NOT_SUPPORTED));

        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenThrow(new MethodeContentNotSupportedException(""));

        resource.mapImageModel(file, headers);
    }

    @Test
    public void getImageModelShouldReturn400IfContentIsNotValid() throws IOException {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(CONTENT_CANNOT_BE_MAPPED));

        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenThrow(new TransformationException(new IOException()));

        resource.mapImageModel(file, headers);
    }

    @Test
    public void getImageModelShouldFireIllegalStateExceptionIfNoTransactionHeaderSupplied() {
        when(headers.getRequestHeader(TRANSACTION_ID_HEADER)).thenReturn(new LinkedList<>());
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Transaction ID not found.");

        resource.mapImageModel(file, headers);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void importContentShouldSucceedWhenContentIsEligibleToPublish() {
        final Content expectedContent = Content.builder().withUuid(java.util.UUID.fromString(UUID))
                .withPublishReference(TRANSACTION_ID).withLastModified(LAST_MODIFIED_DATE).build();
        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenReturn(expectedContent);

        resource.ingestImageModel(file, headers);

        verify(producer).send(anyList());
    }

    @Test
    public void importContentShouldReturn400ForContentWithInvalidUuid() {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(CONTENT_CANNOT_BE_MAPPED));

        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenThrow(new TransformationException(new IOException()));

        resource.ingestImageModel(file, headers);

        verifyZeroInteractions(contentMapper);
    }

    @Test
    public void importContentShouldReturn400IfContentTypeNotImage() {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(CONTENT_TYPE_NOT_SUPPORTED));

        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenThrow(new MethodeContentNotSupportedException(""));

        resource.ingestImageModel(file, headers);

        verifyZeroInteractions(contentMapper);
    }

    @Test
    public void importContentShouldReturn400IfContentIsNotValid() throws IOException {
        exception.expect(WebApplicationClientException.class);
        exception.expect(hasResponseStatus(400));
        exception.expect(hasResponseMessage(CONTENT_CANNOT_BE_MAPPED));

        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenThrow(new TransformationException(new IOException()));

        resource.ingestImageModel(file, headers);
    }

    @Test
    public void importContentShouldFireIllegalStateExceptionIfNoTransactionHeaderSupplied() {
        when(headers.getRequestHeader(TRANSACTION_ID_HEADER)).thenReturn(new LinkedList<>());
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Transaction ID not found.");

        resource.ingestImageModel(file, headers);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void importContentShouldReturn500IfMessageCannotBeSent() throws JsonProcessingException {
        exception.expect(WebApplicationServerException.class);
        exception.expect(hasResponseStatus(500));
        exception.expect(hasResponseMessage(UNABLE_TO_WRITE_JSON_MESSAGE));

        final Content expectedContent = Content.builder().withUuid(java.util.UUID.fromString(UUID))
                .withPublishReference(TRANSACTION_ID).withLastModified(LAST_MODIFIED_DATE).build();
        when(imageModelMapper.mapImageModel(eq(file), eq(TRANSACTION_ID), any(Date.class)))
                .thenReturn(expectedContent);

        when(mockObjectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        contentMapper = new MessageProducingContentMapper(imageModelMapper, mockObjectMapper, SYSTEM_ID,
                producer, URI_BUILDER);

        resource = new MethodeImageModelResource(imageModelMapper, contentMapper, new UuidValidator(), new PublishingValidator());

        resource.ingestImageModel(file, headers);
    }

    private Matcher<WebApplicationException> hasResponseStatus(final int statusCode) {
        return new BaseMatcher<WebApplicationException>() {
            @Override
            public boolean matches(final Object o) {
                final WebApplicationException exception = (WebApplicationException) o;
                return statusCode == exception.getResponse().getStatus();
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Response status should be ").appendValue(statusCode);
            }
        };
    }

    private Matcher<WebApplicationException> hasResponseMessage(final String message) {
        return new BaseMatcher<WebApplicationException>() {
            @Override
            public boolean matches(final Object o) {
                final WebApplicationException exception = (WebApplicationException) o;
                return message.equals(((ErrorEntity) exception.getResponse().getEntity()).getMessage());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("Response message should be ").appendValue(message);
            }
        };
    }
}
