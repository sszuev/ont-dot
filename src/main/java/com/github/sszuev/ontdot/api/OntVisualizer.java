package com.github.sszuev.ontdot.api;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.renderers.DOTRendererFactory;
import org.apache.jena.shared.PrefixMapping;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A facade (builder and configurator) for dot-render,
 * which is a facility to draw {@link OntModel ontology-graph} in DOT format.
 * <p>
 * Created by @ssz on 14.01.2022.
 *
 * @see <a href='https://en.wikipedia.org/wiki/DOT_(graph_description_language)'>DOT (graph description language)</a>
 */
public class OntVisualizer implements DOTConfig {
    private final PrefixMapping pm;
    private final Set<String> entities;

    protected OntVisualizer(PrefixMapping pm, Set<String> entities) {
        this.pm = Objects.requireNonNull(pm);
        this.entities = Objects.requireNonNull(entities);
    }

    public static OntVisualizer create() {
        return of(OntModelFactory.STANDARD, Set.of());
    }

    protected static OntVisualizer of(PrefixMapping pm, Set<String> entities) {
        return new OntVisualizer(pm, entities);
    }

    public OntVisualizer prefixes(PrefixMapping pm) {
        return of(PrefixMapping.Factory.create().setNsPrefixes(pm).lock(), this.entities);
    }

    public OntVisualizer entities(Collection<String> entities) {
        return of(this.pm, Objects.requireNonNull(entities).stream().map(Objects::requireNonNull)
                .collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public PrefixMapping prefixes() {
        return pm;
    }

    @Override
    public Set<String> entities() {
        return entities;
    }

    /**
     * Draws the specified {@link OntModel OWL Graph} as a {@code String}.
     *
     * @param model {@link OntModel}
     * @return {@code String}
     */
    public String draw(OntModel model) {
        StringWriter sw = new StringWriter();
        write(model, sw);
        return sw.toString();
    }

    /**
     * Draws the specified {@link OntModel OWL Graph} to the destination represented by {@link Writer}.
     *
     * @param model  {@link OntModel} to write, not {@code null}
     * @param writer {@link Writer}, not {@code null}
     */
    public void write(OntModel model, Writer writer) {
        DOTRendererFactory.create(this, writer).render(model);
    }
}
