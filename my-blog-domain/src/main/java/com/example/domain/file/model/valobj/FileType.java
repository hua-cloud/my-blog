package com.example.domain.file.model.valobj;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FileType {

    PNG("image/png"),

    JPEG("image/jpeg"),

    GIF("image/gif");

    private final String mimeType;

    public static boolean supportsMimeType(String mimeType) {
        return Arrays.stream(values())
                .anyMatch(type -> type.mimeType.equals(mimeType));
    }
}
