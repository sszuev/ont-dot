package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.api.DOTConfig;
import org.apache.jena.shared.PrefixMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Set;

/**
 * Created by @ssz on 10.01.2022.
 */
public class DOTRendererTest {

    @Test
    public void testAccept() {
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.createOntClass("A")
                .addSuperClass(m.createDataMaxCardinality(m.getOWLTopDataProperty(), 1, m.getRDFSLiteral()));
        m.write(System.out, "ttl");
        System.out.println("=".repeat(42));
        String res = writeStr(m);
        System.out.println(res);
        Assertions.assertTrue(containsLink(res, "n1", "n2"));
        Assertions.assertTrue(containsLink(res, "n2", "n3"));
        Assertions.assertTrue(containsLink(res, "n2", "n4"));
    }

    public static boolean containsLink(String dot, String left, String right) {
        return dot.contains(left + "->" + right);
    }

    public static String writeStr(OntModel m) {
        StringWriter sw = new StringWriter();
        DOTRenderer.create(createConfig(m), sw).render(m);
        return sw.toString();
    }

    public static DOTConfig createConfig(PrefixMapping pm) {
        return new DOTConfig() {
            @Override
            public PrefixMapping prefixes() {
                return pm;
            }

            @Override
            public Set<String> entities() {
                return Set.of();
            }
        };
    }

}
