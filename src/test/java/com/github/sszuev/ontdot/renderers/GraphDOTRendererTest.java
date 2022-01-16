package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntClass;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.utils.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

/**
 * Created by @ssz on 10.01.2022.
 */
public class GraphDOTRendererTest {

    @Test
    public void testSimpleSubClassOfDataMaxCardinality() {
        String expected = ResourceUtils.getResource("/simple-subclassof.dot");

        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.createOntClass("A")
                .addSuperClass(m.createDataMaxCardinality(m.getOWLTopDataProperty(), 1, m.getRDFSLiteral()));

        String res = writeStr(m, false);
        Assertions.assertTrue(containsLink(res, "n1", "n2"));
        Assertions.assertTrue(containsLink(res, "n2", "n3"));
        Assertions.assertTrue(containsLink(res, "n2", "n4"));

        Assertions.assertEquals(expected, res);
    }

    @Test
    public void testSimpleClassPropertiesMap() {
        String expected = ResourceUtils.getResource("/simple-classpropertiesmap.dot");

        String ns = "http://x#";
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("x", ns);
        OntClass cl = m.createOntClass(ns + "C");
        cl.addProperty(m.createAnnotationProperty(ns + "ap"), "test");
        m.createDataProperty(ns + "ap").addDomain(cl);
        m.createObjectProperty(ns + "op").addDomain(cl);

        String res = writeStr(m, true);
        Assertions.assertEquals(expected, res);
    }

    public static boolean containsLink(String dot, String left, String right) {
        return dot.contains(left + "->" + right);
    }

    public static String writeStr(OntModel m, boolean withClassPropertiesMap) {
        StringWriter sw = new StringWriter();
        new GraphDOTRenderer(m, () -> withClassPropertiesMap, sw).render(m);
        return sw.toString();
    }
}
