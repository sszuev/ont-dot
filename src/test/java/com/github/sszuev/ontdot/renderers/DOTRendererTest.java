package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

/**
 * Created by @ssz on 10.01.2022.
 */
public class DOTRendererTest {

    @Test
    public void testAccept() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.createOntClass("A")
                .addSuperClass(m.createDataMaxCardinality(m.getOWLTopDataProperty(), 1, m.getRDFSLiteral()));
        System.out.println("=".repeat(42));
        String res = writeStr(m);
        Assertions.assertTrue(containsLink(res, "n1", "n2"));
        Assertions.assertTrue(containsLink(res, "n2", "n3"));
        Assertions.assertTrue(containsLink(res, "n2", "n4"));

        Assertions.assertEquals("digraph OWL {\n" +
                " rankdir=\"LR\";\n" +
                " node[shape=plaintext];\n" +
                "n1[style=filled,fillcolor=orangered,label=<\n" +
                "<table border='0' cellborder='1' cellspacing='0'> <tr>\n" +
                "  <td>A</td>\n" +
                " </tr>\n" +
                "</table>\n" +
                ">\n" +
                "];n2[color=orangered,style=filled,fillcolor=yellow1,label=<\n" +
                "<table border='0' cellborder='1' cellspacing='0'>\n" +
                " <th port=\"header\">\n" +
                "  <td colspan='2' bgcolor='orangered'>DataMaxCardinality  </td>\n" +
                " </th>\n" +
                " <tr>\n" +
                "  <td>owl:topDataProperty</td>\n" +
                "  <td>rdfs:Literal</td>\n" +
                " </tr>\n" +
                "</table>\n" +
                ">\n" +
                "];n2->n3;n2->n4;n1->n2[color=orangered];\n" +
                "}\n", res);
    }

    public static boolean containsLink(String dot, String left, String right) {
        return dot.contains(left + "->" + right);
    }

    public static String writeStr(OntModel m) {
        StringWriter sw = new StringWriter();
        new DOTRenderer(m, sw).render(m);
        return sw.toString();
    }
}
