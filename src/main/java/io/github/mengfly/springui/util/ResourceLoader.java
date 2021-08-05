package io.github.mengfly.springui.util;

import java.net.URL;

/**
 * @author Mengfly
 */
public class ResourceLoader {

    public static URL loadResource(String location) {
        return ResourceLoader.class.getClassLoader().getResource(location);
    }
}
