package com.ft.methodeimagemodelmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import io.dropwizard.Configuration;

public class MethodeImageModelMapperConfiguration extends Configuration implements ConfigWithAppInfo {

    private final ConsumerConfiguration consumer;
    private final ProducerConfiguration producer;
    private final String contentUriPrefix;
    private final String externalBinaryUrlBasePath;

    @JsonProperty
    private AppInfo appInfo = new AppInfo();

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

    @Override
    public AppInfo getAppInfo() {
        return appInfo;
    }
}
