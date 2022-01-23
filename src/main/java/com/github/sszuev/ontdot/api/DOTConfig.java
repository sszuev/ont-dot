package com.github.sszuev.ontdot.api;

import org.apache.jena.shared.PrefixMapping;

import java.util.Set;

/**
 * DOT-render configuration.
 * <p>
 * Created by @ssz on 14.01.2022.
 */
public interface DOTConfig extends DOTOptions {

    /**
     * Returns prefix map for using while rendering.
     *
     * @return {@link PrefixMapping}
     */
    PrefixMapping prefixes();

    /**
     * Returns a {@code Set} of entities to render.
     * The empty {@code Set} means that the whole graph will be rendered.
     *
     * @return a {@code Set} of {@link String}s
     */
    Set<String> entities();

    /**
     * Returns a class-properties mapping.
     *
     * @return {@link ClassPropertyMap}
     */
    ClassPropertyMap classProperties();

    /**
     * Returns a literal-renderer,
     * that is a facility to represent {@link org.apache.jena.rdf.model.Literal}s as {@code String}s in DOT.
     *
     * @return {@link LiteralRenderer}
     */
    LiteralRenderer literalRenderer();
}
