package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntClass;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.api.DOTSetting;
import com.github.sszuev.ontdot.api.OntVisualizer;
import com.github.sszuev.tests.utils.ResourceUtils;
import org.apache.jena.rdf.model.Property;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

/**
 * Created by @ssz on 10.01.2022.
 */
public class GraphDOTWriterTest {

    @Test
    public void testSimpleSubClassOfDataMaxCardinality() {
        String expected = ResourceUtils.getResource("/simple-subclassof.dot");

        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD);
        m.createOntClass("A")
                .addSuperClass(m.createDataMaxCardinality(m.getOWLTopDataProperty(), 1, m.getRDFSLiteral()));

        String res = writeStr(m, OntVisualizer.create());
        Assertions.assertTrue(containsLink(res, "n1", "n2"));
        Assertions.assertTrue(containsLink(res, "n2", "n3"));
        Assertions.assertTrue(containsLink(res, "n2", "n4"));

        Assertions.assertEquals(expected, res);
    }

    @Test
    public void testDefaultClassPropertiesMap() {
        String expected = ResourceUtils.getResource("/simple-default-cpm.dot");

        String ns = "http://x#";
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("x", ns);
        OntClass cl = m.createOntClass(ns + "C");
        cl.addProperty(m.createAnnotationProperty(ns + "ap"), "test");
        m.createDataProperty(ns + "ap").addDomain(cl);
        m.createObjectProperty(ns + "op").addDomain(cl);

        String res = writeStr(m, OntVisualizer.create()
                .withOption(DOTSetting.BOOLEAN_CLASS_PROPERTIES_MAP, true)
                .withOption(DOTSetting.STRING_CLASS_COLOR, "red"));
        Assertions.assertEquals(expected, res);
    }

    @Test
    public void testCustomClassPropertiesMap() {
        String expected = ResourceUtils.getResource("/simple-custom-cpm.dot");

        String ns = "http://x#";
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("my", ns);
        m.createOntClass(ns + "SubClass").addSubClassOfStatement(m.createOntClass(ns + "SuperClass"));

        List<Property> builtins = List.of(m.getOWLTopObjectProperty(), m.getOWLTopDataProperty(),
                m.getRDFSComment(), m.getRDFSLabel());

        String res = writeStr(m, OntVisualizer.create()
                .withClassProperties(ce -> builtins.stream())
                .withOption(DOTSetting.STRING_CLASS_COLOR, "green"));

        Assertions.assertEquals(expected, res);
    }

    @Test
    public void testDefaultLiteralRenderer() {
        String expected = ResourceUtils.getResource("/simple-default-lr.dot");

        String ns = "http://x#";
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("my", ns);
        m.createOntClass(ns + "C").addSuperClass(m.createDataHasValue(m.createDataProperty(ns + "dp"),
                m.createLiteral("xxx".repeat(1000))));

        String res = writeStr(m, OntVisualizer.create().withOption(DOTSetting.STRING_CLASS_COLOR, "yellow"));

        Assertions.assertEquals(expected, res);
    }


    @Test
    public void testCustomLiteralRenderer() {
        String expected = ResourceUtils.getResource("/simple-custom-lr.dot");

        String ns = "http://x#";
        OntModel m = OntModelFactory.createModel().setNsPrefixes(OntModelFactory.STANDARD).setNsPrefix("my", ns);
        m.createOntClass(ns + "Clazz")
                .addEquivalentClass(m.createDataHasValue(m.createDataProperty(ns + "dataProperty"),
                        m.createTypedLiteral(42)));

        String res = writeStr(m, OntVisualizer.create()
                .withLiteralRenderer((value, config, pm) -> {
                    if (value.getInt() == 42)
                        return "Unknown";
                    throw new AssertionError();
                })
                .withOption(DOTSetting.STRING_CLASS_COLOR, "gray"));

        Assertions.assertEquals(expected, res);
    }

    public static boolean containsLink(String dot, String left, String right) {
        return dot.contains(left + "->" + right);
    }

    public static String writeStr(OntModel m, OntVisualizer viz) {
        StringWriter sw = new StringWriter();
        new GraphDOTWriter(m, viz.classProperties(), viz.literalRenderer(), sw, viz).write(m);
        return sw.toString();
    }
}
