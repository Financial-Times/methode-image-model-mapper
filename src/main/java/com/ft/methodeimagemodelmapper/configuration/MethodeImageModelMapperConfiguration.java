package com.ft.methodeimagemodelmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class MethodeImageModelMapperConfiguration extends Configuration {

    private final ConsumerConfiguration consumer;
    private final ProducerConfiguration producer;
    private final String contentUriPrefix;
    private final BinaryTransformerConfiguration binaryTransformerConfiguration;
    private final String externalBinaryUrlBasePath;

    public MethodeImageModelMapperConfiguration(@JsonProperty("consumer") ConsumerConfiguration consumer,
                                                @JsonProperty("producer") ProducerConfiguration producer,
                                                @JsonProperty("contentUriPrefix") String contentUriPrefix,
                                                @JsonProperty("binaryTransformer") final BinaryTransformerConfiguration binaryTransformerConfiguration,
                                                @JsonProperty("externalBinaryUrlBasePath") final String externalBinaryUrlBasePath) {
        this.consumer = consumer;
        this.producer = producer;
        this.contentUriPrefix = contentUriPrefix;
        this.binaryTransformerConfiguration = binaryTransformerConfiguration;
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

    public BinaryTransformerConfiguration getBinaryTransformerConfiguration() {
        return binaryTransformerConfiguration;
    }

    public String getExternalBinaryUrlBasePath() {
        return externalBinaryUrlBasePath;
    }
}
