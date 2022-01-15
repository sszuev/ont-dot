package com.github.sszuev.ontdot.renderers;

import com.github.owlcs.ontapi.jena.model.OntModel;

/**
 * Created by @ssz on 15.01.2022.
 *
 * @see <a href='https://www.w3.org/TR/owl2-quick-reference/'>A Quick Guide</a>
 * @see <a href='https://www.w3.org/TR/owl2-syntax/'>OWL 2 Web Ontology Language Structural Specification and Functional-Style Syntax (Second Edition)</a>
 */
public interface DOTRenderer {

    /**
     * Renders the given ontology.
     *
     * @param ont {@link OntModel}, not {@code null}
     */
    void render(OntModel ont);
}
