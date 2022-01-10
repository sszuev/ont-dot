package com.github.sszuev.dot;

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
        m.write(System.out, "ttl");
        System.out.println("=".repeat(42));
        String res = writeToString(m);
        System.out.println(res);
        Assertions.assertTrue(res.contains(";n2->n3;n2->n4;"));
    }

    static String writeToString(OntModel m) {
        StringWriter sw = new StringWriter();
        DOTRenderer.draw(m, sw);
        return sw.toString();
    }

}
