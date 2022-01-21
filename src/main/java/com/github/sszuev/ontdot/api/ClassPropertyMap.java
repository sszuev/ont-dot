package com.github.sszuev.ontdot.api;

import com.github.owlcs.ontapi.jena.model.OntClass;
import org.apache.jena.rdf.model.Property;

import java.util.stream.Stream;

/**
 * An interface to provide mapping between class expression and properties,
 * that are supposed to be belonged to that expression.
 * <p>
 * Partially copy-pasted from
 * <a href='https://github.com/owlcs/ont-map/blob/master/src/main/java/com/github/owlcs/map/ClassPropertyMap.java'>ONT-MAP</a>
 * <p>
 * Created by @sszuev on 19.04.2018.
 */
public interface ClassPropertyMap {

    /**
     * Lists all properties by a class.
     *
     * @param ce {@link OntClass} with a model inside
     * @return a <b>distinct</b> {@code Stream} of {@link Property properties}
     */
    Stream<Property> properties(OntClass ce);

}
