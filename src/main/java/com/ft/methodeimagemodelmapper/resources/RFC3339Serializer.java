package com.ft.methodeimagemodelmapper.resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;


public class RFC3339Serializer
        extends com.fasterxml.jackson.databind.ser.std.StdSerializer<OffsetDateTime> {

    private static final DateTimeFormatter FMT_RFC3339 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

    public RFC3339Serializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        if (value != null) {
            jgen.writeString(FMT_RFC3339.format(value));
        }
    }
}
