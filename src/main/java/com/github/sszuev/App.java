package com.github.sszuev;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.sszuev.dot.DOTRenderer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by @ssz on 09.01.2022.
 */
public class App {

    public static void main(String... args) throws Exception {
        if (args.length == 0) {
            System.out.println("usage: input-file.ttl [output-file.dot]");
            System.exit(0);
        }
        Path source = Path.of(args[0]).toRealPath();
        Path target = null;
        if (args.length > 1) {
            target = Path.of(args[1]).toAbsolutePath();
        }

        OntologyManager m = OntManagers.createManager();
        Ontology ont = m.loadOntologyFromOntologyDocument(source.toFile());

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

}
