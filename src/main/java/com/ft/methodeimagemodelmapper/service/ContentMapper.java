package com.ft.methodeimagemodelmapper.service;

import com.ft.content.model.Content;
import com.ft.methodeimagemodelmapper.model.EomFile;

import java.util.Date;

public interface ContentMapper {

    public Content mapImageModel(final EomFile eomFile, final String transactionId, Date lastModifiedDate);
}
