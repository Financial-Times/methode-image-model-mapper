package com.ft.methodeimagemodelmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BinaryTransformerConfiguration {
    private final String hostAddress;
    private final String urlAddress;

    public BinaryTransformerConfiguration(@JsonProperty("hostAddress") final String hostAddress,
                                          @JsonProperty("urlAddress") final String urlAddress) {
        this.hostAddress = hostAddress;
        this.urlAddress = urlAddress;
    }

    public String buildInternalDataUrl(String uuid) {
        return "http://" + hostAddress + String.format(urlAddress, uuid);
    }
}
