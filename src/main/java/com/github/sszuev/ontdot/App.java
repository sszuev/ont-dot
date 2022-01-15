package com.github.sszuev.ontdot;

import com.github.owlcs.ontapi.OntFormat;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.api.OntVisualizer;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by @ssz on 09.01.2022.
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String... args) throws Exception {
        forceDisableExternalLogging();
        CLI cli = null;
        try {
            cli = CLI.parse(args);
        } catch (CLI.ExitException ex) {
            System.err.println(ex.getMessage());
            System.exit(ex.getCode());
        }
        org.apache.log4j.Level level = cli.verbose() ? org.apache.log4j.Level.DEBUG : org.apache.log4j.Level.FATAL;
        org.apache.log4j.Logger.getRootLogger().setLevel(level);

        LOGGER.info("Load ontology from {}", cli.source());
        OntModel ont = loadOntology(cli.source(), cli.format()).asGraphModel();

        OntVisualizer visualizer = OntVisualizer.create().prefixes(ont);
        if (cli.browse()) {
            LOGGER.info("Browse.");
            Graphviz.browse(visualizer.draw(ont));
        } else {
            LOGGER.info("Write to {}", cli.target());
            if (cli.printAsURL()) {
                URI uri = Graphviz.toGraphvizOnlineURI(visualizer.draw(ont));
                if (cli.target() != null) {
                    Files.writeString(cli.target(), uri.toString(), StandardCharsets.UTF_8);
                } else {
                    System.out.println(uri);
                }
            } else {
                try (Writer writer = openWriter(cli.target())) {
                    visualizer.write(ont, writer);
                    ;
                }
            }
        }
        LOGGER.info("Done.");
    }

    public static Ontology loadOntology(Path source, OntFormat format) throws OWLOntologyCreationException {
        OntologyManager m = OntManagers.createManager();
        if (format == null) {
            return m.loadOntologyFromOntologyDocument(source.toFile());
        }
        OWLOntologyDocumentSource src = new FileDocumentSource(source.toFile(), format.createOwlFormat());
        return m.loadOntologyFromOntologyDocument(src);
    }

    public static Writer openWriter(Path target) throws IOException {
        if (target == null) {
            return new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
        } else {
            return Files.newBufferedWriter(target);
        }
    }

    private static void forceDisableExternalLogging() {
        java.util.logging.LogManager.getLogManager().reset();
    }

}
