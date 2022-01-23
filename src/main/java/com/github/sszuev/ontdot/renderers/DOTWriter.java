package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.OntModel;

/**
 * Created by @ssz on 15.01.2022.
 */
public interface DOTWriter extends AutoCloseable {

    /**
     * Writes the given ontology.
     *
     * @param ont {@link OntModel}, not {@code null}
     */
    void write(OntModel ont);
}
