package com.github.sszuev.ontdot.api;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;

/**
 * A facility-helper to draw {@link Literal RDF-Literal} as a {@code String}.
 * <p>
 * Created by @ssz on 22.01.2022.
 */
public interface LiteralRenderer {

    /**
     * Represents the specified {@link Literal} as a {@code String}.
     *
     * @param value  {@link Literal}, not {@code null}
     * @param config {@link RenderOptions}, settings
     * @param pm     {@link PrefixMapping}
     * @return {@code String}
     */
    String print(Literal value, RenderOptions config, PrefixMapping pm);

    /**
     * Represents {@code value^^xsd:nonNegativeInteger}-literal as a {@code String}.
     *
     * @param value  a non-negative {@code long}
     * @param config {@link RenderOptions}, settings
     * @param pm     {@link PrefixMapping}
     * @return {@code String}
     */
    default String printNonNegativeInteger(long value, RenderOptions config, PrefixMapping pm) {
        return print(createNonNegativeInteger(value), config, pm);
    }

    /**
     * Creates a {@code value^^xsd:nonNegativeInteger}-literal.
     *
     * @param value {@code long}
     * @return {@link Literal}
     */
    static Literal createNonNegativeInteger(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException();
        }
        return ResourceFactory.createTypedLiteral(String.valueOf(value), XSDDatatype.XSDnonNegativeInteger);
    }
}
