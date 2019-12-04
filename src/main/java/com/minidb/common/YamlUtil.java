package com.minidb.common;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;


public class YamlUtil {
    private final static Yaml yaml = new Yaml();

    public static <T> T readPojo(String uri, Class<T> clazz) {
        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(uri);
        return yaml.loadAs(inputStream, clazz);
    }
}
