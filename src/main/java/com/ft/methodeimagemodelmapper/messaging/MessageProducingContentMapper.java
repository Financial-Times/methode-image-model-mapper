package com.ft.methodeimagemodelmapper.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.content.model.Content;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.methodeimagemodelmapper.exception.ContentMapperException;
import com.ft.methodeimagemodelmapper.model.EomFile;
import com.ft.methodeimagemodelmapper.service.MethodeImageModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.stream.Collectors;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.time.ZoneOffset.UTC;

public class MessageProducingContentMapper {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProducingContentMapper.class);
    private static final String CMS_CONTENT_PUBLISHED = "cms-content-published";
    private static final DateTimeFormatter RFC3339_FMT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.STRICT);

    private final MethodeImageModelMapper delegate;
    private final MessageProducer producer;
    private final ObjectMapper objectMapper;
    private final String systemId;
    private final UriBuilder contentUriBuilder;

    public MessageProducingContentMapper(MethodeImageModelMapper delegate, ObjectMapper objectMapper, String systemId,
                                         MessageProducer producer, UriBuilder contentUriBuilder) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
        this.systemId = systemId;
        this.producer = producer;
        this.contentUriBuilder = contentUriBuilder;
    }

    public Content mapImageModel(EomFile eomFile, String transactionId, Date lastModifiedDate) {
        List<Content> contents = Collections.singletonList(delegate.mapImageModel(eomFile, transactionId, lastModifiedDate));
        producer.send(contents.stream().map(this::createMessage).collect(Collectors.toList()));
        LOG.info("sent {} messages", contents.size());
        return contents.get(0);
    }

    private Message createMessage(Content content) {
        LOG.info("Last Modified Date is: " + content.getLastModified());
        Map<String, Object> messageBody = new LinkedHashMap<>();
        URI contentUri = contentUriBuilder.build(content.getUuid());
        messageBody.put("contentUri", contentUri.toString());
        messageBody.put("uuid", content.getUuid());
        URI relativeUrl = UriBuilder.fromPath(contentUri.getPath()).replaceQuery(contentUri.getQuery()).build();
        messageBody.put("relativeUrl", relativeUrl);
        messageBody.put("destination", "methode-image-model-transformer");
        messageBody.put("payload", content);
        String lastModified = RFC3339_FMT.format(OffsetDateTime.ofInstant(content.getLastModified().toInstant(), UTC));
        messageBody.put("lastModified", lastModified);

        Message msg;
        try {
            msg = new Message.Builder().withMessageId(UUID.randomUUID())
                    .withMessageType(CMS_CONTENT_PUBLISHED)
                    .withMessageTimestamp(new Date())
                    .withOriginSystemId(systemId)
                    .withContentType("application/json")
                    .withMessageBody(objectMapper.writeValueAsString(messageBody))
                    .build();

            msg.addCustomMessageHeader(TRANSACTION_ID_HEADER, content.getPublishReference());
            msg = KeyedMessage.forMessageAndKey(msg, content.getUuid());
        } catch (JsonProcessingException e) {
            LOG.error("unable to write JSON for message", e);
            throw new ContentMapperException("Unable to write JSON for message", e);
        }
        return msg;
    }
}
