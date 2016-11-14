package com.ft.methodeimagemodelmapper.model;

public enum EomFileContentType {
    IMAGE("Image");

    private String contentType;

    EomFileContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
