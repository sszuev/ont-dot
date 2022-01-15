package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.Ontology;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.utils.ModelData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Set;

/**
 * Created by @ssz on 15.01.2022.
 */
public class WithEntityFilterDOTRendererTest {

    @Test
    public void testFilteredPizza() {
        OntModel ont = ((Ontology) ModelData.PIZZA.fetch(OntManagers.createManager())).asGraphModel();
        Set<String> filter = Set.of(ont.expandPrefix(":Germany"),
                ":America", "http://www.co-ode.org/ontologies/pizza/pizza.owl#Veneziana");
        String res = writeStr(ont, filter);
        System.out.println(res);
        Assertions.assertEquals(109, res.split("->").length);
    }

    public static String writeStr(OntModel m, Set<String> entities) {
        StringWriter sw = new StringWriter();
        new WithEntityFilterDOTRenderer(m, sw, entities).render(m);
        return sw.toString();
    }
}
