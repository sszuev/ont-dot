package com.github.sszuev;

import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A helper to browse ready dot-file.
 */
public class Graphviz {

    public static void browse(String txt) throws IOException {
        URI uri = toGraphvizOnlineURI(txt);
        if (uri.toString().length() > 2048) {
            uri = shorten(uri);
        }
        java.awt.Desktop.getDesktop().browse(uri);
    }

    private static URI toGraphvizOnlineURI(String graph) {
        return URI.create("https://dreampuf.github.io/GraphvizOnline/#" +
                URLEncoder.encode(graph, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
    }

    private static URI shorten(URI orig) throws IOException {
        // use apache http-client (not java) for multipart/form-data support
        URI dom = URI.create("https://git.io/");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(dom.resolve("create"));
            request.setEntity(new UrlEncodedFormEntity(List.of(new BasicNameValuePair("url", orig.toString()))));
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine line = response.getStatusLine();
                if (line.getStatusCode() != 200) {
                    throw new IOException(line.toString());
                }
                String res = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                return dom.resolve(res);
            }
        }
    }
}
