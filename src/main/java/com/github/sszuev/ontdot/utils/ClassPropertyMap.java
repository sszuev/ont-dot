package com.github.sszuev.ontdot.utils;

import com.github.owlcs.ontapi.jena.model.*;
import org.apache.jena.rdf.model.Property;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An interface to provide mapping between class expression and properties,
 * that are supposed to be belonged to that expression.
 * <p>
 * Partially copy-pasted from <a href='https://github.com/owlcs/ont-map'>ONT-MAP</a>
 * <p>
 * Created by @szuev on 19.04.2018.
 */
public interface ClassPropertyMap {

    /**
     * Lists all properties by a class.
     *
     * @param ce {@link OntClass} with a model inside
     * @return <b>distinct</b> Stream of {@link Property properties}
     */
    Stream<Property> properties(OntClass ce);

    /**
     * Lists all classes by a property.
     * A reverse operation to the {@link #properties(OntClass)}.
     *
     * @param pe {@link OntProperty} - an property, which in OWL2 can be either {@link OntDataProperty},
     *           {@link OntAnnotationProperty} or {@link OntObjectProperty}
     * @return <b>distinct</b> Stream of {@link OntClass class-expressions}
     */
    default Stream<OntClass> classes(OntProperty pe) {
        return pe.getModel().ontObjects(OntClass.class)
                .filter(c -> properties(c).anyMatch(p -> Objects.equals(p, pe.asProperty())))
                .distinct();
    }

}
