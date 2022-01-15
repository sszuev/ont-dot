package com.github.sszuev.ontdot.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Created by @ssz on 15.01.2022.
 */
public class ResourceUtils {

    public static String getResource(String file) {
        try {
            return Files.readString(getResourcePath(file)).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path getResourcePath(String file) {
        try {
            return Paths.get(Objects.requireNonNull(ResourceUtils.class.getResource(file)).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
