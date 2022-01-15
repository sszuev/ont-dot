package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.OntEntity;
import com.github.owlcs.ontapi.jena.model.OntStatement;
import com.github.owlcs.ontapi.jena.vocabulary.XSD;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.shared.PrefixMapping;

import java.util.stream.Stream;

/**
 * Created by @ssz on 15.01.2022.
 */
public class ModelUtils {

    /**
     * Answers {@code true} iff entity has explicit declaration in graph.
     * Built-ins are not declared usually.
     *
     * @param e {@link OntEntity}
     * @return {@code boolean}
     */
    public static boolean isDeclared(OntEntity e) {
        try (Stream<OntStatement> s = e.spec()) {
            return s.findFirst().isPresent();
        }
    }

    /**
     * Represents {@link Literal} as a {@code String}.
     *
     * @param node    {@link Literal}
     * @param quoting {@code boolean} (to use quoting or not)
     * @param pm      {@link PrefixMapping}
     * @return {@code String}
     */
    public static String print(Literal node, boolean quoting, PrefixMapping pm) {
        return printLiteral(node.getLexicalForm(), quoting, pm, node.getDatatypeURI(), node.getLanguage());
    }

    public static String printLiteral(String text, boolean quoting, PrefixMapping pm, String type, String lang) {
        StringBuilder b = new StringBuilder();
        if (quoting) {
            b.append('"');
        }
        b.append(text.replace("\"", "\\\""));
        if (quoting) {
            b.append('"');
        }
        if (lang != null && !lang.equals("")) {
            b.append("@").append(lang);
        } else if (type != null) {
            if (!XSD.xstring.getURI().equals(type)) {
                b.append("^^").append(pm.shortForm(type));
            }
        }
        return b.toString();
    }

}
