package com.example.types.constant;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class FileUploadConstants {

    public static final int MAX_FILENAME_LENGTH = 255;

    public static final Pattern SAFE_EXTENSION = Pattern.compile("[a-zA-Z0-9]{1,16}");

    public static final DateTimeFormatter KEY_DATE = DateTimeFormatter.ofPattern("uuuu/MM", Locale.ROOT);

    public static final String IMAGES_DIRECTORY = "images/";
}
