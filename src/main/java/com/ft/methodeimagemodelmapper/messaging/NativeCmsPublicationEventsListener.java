package com.ft.methodeimagemodelmapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodeimagemodelmapper.exception.IngesterException;
import com.ft.methodeimagemodelmapper.model.EomFile;
import com.ft.methodeimagemodelmapper.validation.PublishingValidator;
import com.ft.uuidutils.UuidValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Predicate;

public class NativeCmsPublicationEventsListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final Predicate<Message> filter;
    private final MessageProducingContentMapper mapper;
    private final ObjectMapper objectMapper;
    private final SystemId systemId;
    private final PublishingValidator publishingValidator;

    public NativeCmsPublicationEventsListener(String systemCode, MessageProducingContentMapper mapper, ObjectMapper objectMapper,
                                              PublishingValidator publishingValidator) {
        this.systemId = SystemId.systemIdFromCode(systemCode);
        this.filter = msg -> (systemId.equals(msg.getOriginSystemId()));
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.publishingValidator = publishingValidator;
    }

    @Override
    public boolean onMessage(Message message, String transactionId) {
        if (filter.test(message)) {
            LOG.info("Process message");
            handleMessage(message, transactionId);
        } else {
            LOG.info("Skip message from [{}]", message.getOriginSystemId());
        }
        return true;
    }

    private void handleMessage(Message message, String transactionId) {
        try {
            EomFile methodeContent = objectMapper.reader(EomFile.class).readValue(message.getMessageBody());
            UuidValidation.of(methodeContent.getUuid());
            if (publishingValidator.isValidForPublishing(methodeContent)) {
                LOG.info("Importing content [{}] of type [{}] .", methodeContent.getUuid(), methodeContent.getType());
                mapper.mapImageModel(methodeContent, transactionId, message.getMessageTimestamp());
            } else {
                LOG.info("Skip message [{}] of type [{}]", methodeContent.getUuid(), methodeContent.getType());
            }
        } catch (IOException e) {
            throw new IngesterException("Unable to parse Methode content message", e);
        }
    }

}
