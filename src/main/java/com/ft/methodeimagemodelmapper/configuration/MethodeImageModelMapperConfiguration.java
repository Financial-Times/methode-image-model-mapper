package com.ft.methodeimagemodelmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class MethodeImageModelMapperConfiguration extends Configuration {

    private final ConsumerConfiguration consumer;
    private final ProducerConfiguration producer;
    private final String contentUriPrefix;
    private final String externalBinaryUrlBasePath;

    public MethodeImageModelMapperConfiguration(@JsonProperty("consumer") ConsumerConfiguration consumer,
                                                @JsonProperty("producer") ProducerConfiguration producer,
                                                @JsonProperty("contentUriPrefix") String contentUriPrefix,
                                                @JsonProperty("externalBinaryUrlBasePath") final String externalBinaryUrlBasePath) {
        this.consumer = consumer;
        this.producer = producer;
        this.contentUriPrefix = contentUriPrefix;
        this.externalBinaryUrlBasePath = externalBinaryUrlBasePath;
    }

    public ConsumerConfiguration getConsumerConfiguration() {
        return consumer;
    }

    public ProducerConfiguration getProducerConfiguration() {
        return producer;
    }

    public String getContentUriPrefix() {
        return contentUriPrefix;
    }

    public String getExternalBinaryUrlBasePath() {
        return externalBinaryUrlBasePath;
    }
}
