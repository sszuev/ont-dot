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
