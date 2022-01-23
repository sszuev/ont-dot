package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.api.DOTSetting;
import com.github.sszuev.ontdot.api.OntVisualizer;
import com.github.sszuev.ontdot.utils.ModelData;
import com.github.sszuev.ontdot.utils.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Set;

/**
 * Created by @ssz on 15.01.2022.
 */
public class EntitiesDOTRendererTest {

    @Test
    public void testFilteredPizza() {
        OntModel ont = ((Ontology) ModelData.PIZZA.fetch(OntManagers.createManager())).asGraphModel();
        Set<String> filter = Set.of(ont.expandPrefix(":Germany"),
                ":America", "http://www.co-ode.org/ontologies/pizza/pizza.owl#Veneziana");
        String res = writeStr(ont, filter);
        Assertions.assertEquals(105, res.split("->").length);
    }

    @Test
    public void testFilteredKoalaQuokka() {
        OntModel ont = ((Ontology) ModelData.KOALA.fetch(OntManagers.createManager())).asGraphModel();
        Set<String> filter = Set.of(":Quokka");
        String res = writeStr(ont, filter);

        String expected = ResourceUtils.getResource("/koala-quokka.dot");
        Assertions.assertEquals(expected, res);
    }

    public static String writeStr(OntModel m, Set<String> entities) {
        return writeStr(m, OntVisualizer.create()
                .withOption(DOTSetting.BOOLEAN_CLASS_PROPERTIES_MAP, false), entities);
    }

    public static String writeStr(OntModel m, OntVisualizer viz, Set<String> entities) {
        StringWriter sw = new StringWriter();
        new EntitiesDOTRenderer(m, viz.classProperties(), viz.literalRenderer(), viz, entities, sw).render(m);
        return sw.toString();
    }
}
