package com.github.sszuev.ontdot;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A helper to browse ready dot-file.
 */
public class Graphviz {

    public static void browse(String txt) throws IOException {
        URI uri = toGraphvizOnlineURI(txt);
        // TODO: find shortening service ?
        java.awt.Desktop.getDesktop().browse(uri);
    }

    public static URI toGraphvizOnlineURI(String graph) {
        return URI.create("https://dreampuf.github.io/GraphvizOnline/#" +
                URLEncoder.encode(graph, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
    }
}
