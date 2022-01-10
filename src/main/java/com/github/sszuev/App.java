package com.github.sszuev;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.sszuev.dot.DOTRenderer;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by @ssz on 09.01.2022.
 */
public class App {

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage: input-file.ttl [output-file.dot]");
            System.exit(0);
        }
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        Path source = Path.of(argList.remove(0)).toRealPath();
        boolean browse = false;
        for (int i = 0; i < argList.size(); i++) {
            if (argList.get(i).equals("-b")) {
                argList.remove(i);
                browse = true;
                break;
            }
        }
        Path target = null;
        if (!argList.isEmpty()) {
            target = Path.of(argList.remove(0)).toAbsolutePath();
        }
        OntologyManager m = OntManagers.createManager();
        Ontology ont = m.loadOntologyFromOntologyDocument(source.toFile());

        if (browse) {
            browse(DOTRenderer.drawAsString(ont.asGraphModel()));
        }

        try (Writer writer = openWriter(target)) {
            DOTRenderer.draw(ont.asGraphModel(), writer);
        }
    }

    private static Writer openWriter(Path target) throws IOException {
        if (target == null) {
            return new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        } else {
            return Files.newBufferedWriter(target);
        }
    }

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
