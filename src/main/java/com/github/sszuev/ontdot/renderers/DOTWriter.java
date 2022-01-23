package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.OntModel;

import java.io.Closeable;

/**
 * Created by @ssz on 15.01.2022.
 */
public interface DOTWriter extends Closeable {

    /**
     * Writes the given ontology.
     *
     * @param ont {@link OntModel}, not {@code null}
     */
    void write(OntModel ont);
}
