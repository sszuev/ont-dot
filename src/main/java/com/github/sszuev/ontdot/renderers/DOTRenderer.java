package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.OntModel;

/**
 * Created by @ssz on 15.01.2022.
 */
public interface DOTRenderer {

    /**
     * Renders the given ontology.
     *
     * @param ont {@link OntModel}, not {@code null}
     */
    void render(OntModel ont);
}
