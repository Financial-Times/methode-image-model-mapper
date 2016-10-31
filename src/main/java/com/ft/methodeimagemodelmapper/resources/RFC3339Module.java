package com.ft.methodeimagemodelmapper.resources;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class RFC3339Module extends SimpleModule {

    public RFC3339Module() {
        super("RFC3339Module", new Version(1, 0, 0, null, "com.ft.methodelistmapper", "rfc3339-java8-datetime-serializer"));
        addSerializer(new RFC3339Serializer());
    }
}
