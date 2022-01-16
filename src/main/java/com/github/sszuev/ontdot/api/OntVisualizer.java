package com.github.sszuev.ontdot.api;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.renderers.DOTRendererFactory;
import org.apache.jena.shared.PrefixMapping;

import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
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
    private final Map<DOTSettings, Object> settings;

    protected OntVisualizer(PrefixMapping pm, Set<String> entities, Map<DOTSettings, Object> settings) {
        this.pm = Objects.requireNonNull(pm);
        this.entities = Objects.requireNonNull(entities);
        this.settings = Objects.requireNonNull(settings);
    }

    public static OntVisualizer create() {
        return of(OntModelFactory.STANDARD, Set.of(), Map.of());
    }

    protected static OntVisualizer of(PrefixMapping pm, Set<String> entities, Map<DOTSettings, Object> settings) {
        return new OntVisualizer(pm, entities, settings);
    }

    protected static Set<String> copy(Collection<String> entities) {
        return Objects.requireNonNull(entities).stream().map(Objects::requireNonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    protected static PrefixMapping copy(PrefixMapping pm) {
        return PrefixMapping.Factory.create().setNsPrefixes(pm).lock();
    }

    @SuppressWarnings("SameParameterValue")
    protected static Map<DOTSettings, Object> copy(Map<DOTSettings, Object> map, DOTSettings key, Object value) {
        EnumMap<DOTSettings, Object> res = new EnumMap<>(DOTSettings.class);
        res.putAll(map);
        res.put(key, value);
        return Collections.unmodifiableMap(res);
    }

    public OntVisualizer prefixes(PrefixMapping pm) {
        return of(copy(pm), this.entities, this.settings);
    }

    public OntVisualizer entities(Collection<String> entities) {
        return of(this.pm, copy(entities), this.settings);
    }

    public OntVisualizer witOption(DOTSettings key, boolean value) {
        return of(this.pm, this.entities, copy(this.settings, key, value));
    }

    @Override
    public PrefixMapping prefixes() {
        return pm;
    }

    @Override
    public Set<String> entities() {
        return entities;
    }

    @Override
    public boolean displayClassPropertiesMap() {
        return getSetting(DOTSettings.CLASS_PROPERTIES_MAP);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    protected <X> X getSetting(DOTSettings key) {
        return (X) settings.getOrDefault(key, key.defaultValue);
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
