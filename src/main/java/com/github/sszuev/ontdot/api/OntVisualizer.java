package com.github.sszuev.ontdot.api;

import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.sszuev.ontdot.renderers.DOTRendererFactory;
import com.github.sszuev.ontdot.utils.ClassPropertyMapImpl;
import com.github.sszuev.ontdot.utils.LiteralRendererImpl;
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
    private final Map<DOTSetting, Object> settings;
    private final ClassPropertyMap classProperties;
    private final LiteralRenderer literalRenderer;

    protected OntVisualizer(PrefixMapping pm,
                            ClassPropertyMap cpm,
                            LiteralRenderer lr,
                            Map<DOTSetting, Object> conf,
                            Set<String> entities) {
        this.pm = Objects.requireNonNull(pm);
        this.entities = Objects.requireNonNull(entities);
        this.settings = Objects.requireNonNull(conf);
        this.classProperties = Objects.requireNonNull(cpm);
        this.literalRenderer = Objects.requireNonNull(lr);
    }

    /**
     * Creates a visualizer with the default configuration.
     *
     * @return {@link OntVisualizer}
     */
    public static OntVisualizer create() {
        return of(OntModelFactory.STANDARD, new ClassPropertyMapImpl(), new LiteralRendererImpl(), Set.of(), Map.of());
    }

    protected static OntVisualizer of(PrefixMapping pm,
                                      ClassPropertyMap cpm,
                                      LiteralRenderer lr,
                                      Set<String> entities,
                                      Map<DOTSetting, Object> settings) {
        return new OntVisualizer(pm, cpm, lr, settings, entities);
    }

    protected static Set<String> copy(Collection<String> entities) {
        return Objects.requireNonNull(entities).stream().map(Objects::requireNonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    protected static PrefixMapping copy(PrefixMapping pm) {
        return PrefixMapping.Factory.create().setNsPrefixes(pm).lock();
    }

    @SuppressWarnings("SameParameterValue")
    protected static Map<DOTSetting, Object> addOption(Map<DOTSetting, Object> map, DOTSetting key, Object value) {
        EnumMap<DOTSetting, Object> res = new EnumMap<>(DOTSetting.class);
        res.putAll(map);
        res.put(key, value);
        return Collections.unmodifiableMap(res);
    }

    protected static Map<DOTSetting, Object> addOptions(Map<DOTSetting, Object> map, Map<DOTSetting, Object> other) {
        EnumMap<DOTSetting, Object> res = new EnumMap<>(DOTSetting.class);
        res.putAll(map);
        other.forEach((k, v) -> {
            if (!k.type().isInstance(v)) {
                throw new IllegalArgumentException("Wrong type: " + v);
            }
            res.put(k, v);
        });
        return Collections.unmodifiableMap(res);
    }

    /**
     * Sets prefix mapping.
     *
     * @param pm {@link PrefixMapping}, not {@code null}
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer prefixes(PrefixMapping pm) {
        return of(copy(pm), this.classProperties, this.literalRenderer, this.entities, this.settings);
    }

    /**
     * Sets entities to filter.
     *
     * @param entities a {@code Collection} of {@code String} - short (prefixed) or full (iri) names
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer entities(Collection<String> entities) {
        return of(this.pm, this.classProperties, this.literalRenderer, copy(entities), this.settings);
    }

    /**
     * Sets the specified class-properties mapping and turns on the corresponding property setting.
     *
     * @param cpm {@link ClassPropertyMap}, not {@code null}
     * @return a copied instance of {@link OntVisualizer} with new settings
     * @see DOTSetting#BOOLEAN_CLASS_PROPERTIES_MAP
     */
    public OntVisualizer withClassProperties(ClassPropertyMap cpm) {
        return of(this.pm, Objects.requireNonNull(cpm), this.literalRenderer, this.entities,
                addOption(this.settings, DOTSetting.BOOLEAN_CLASS_PROPERTIES_MAP, true));
    }

    /**
     * Sets the specified literal-renderer helper.
     *
     * @param literalRenderer {@link LiteralRenderer}, not {@code null}
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer withLiteralRenderer(LiteralRenderer literalRenderer) {
        return of(this.pm, this.classProperties, Objects.requireNonNull(literalRenderer), this.entities, this.settings);
    }

    /**
     * Sets options.
     *
     * @param settings a {@code Map} of new settings
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer withOptions(Map<DOTSetting, Object> settings) {
        return of(this.pm, this.classProperties, this.literalRenderer, this.entities, addOptions(this.settings, settings));
    }

    /**
     * Sets a boolean option.
     *
     * @param key   {@link DOTSetting} setting key
     * @param value {@code boolean}
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer withOption(DOTSetting key, boolean value) {
        if (Boolean.class != key.type) {
            throw new IllegalArgumentException("Wrong key: " + key);
        }
        return of(this.pm, this.classProperties, this.literalRenderer, this.entities, addOption(this.settings, key, value));
    }

    /**
     * Sets a string option.
     *
     * @param key   {@link DOTSetting} setting key
     * @param value {@code String}
     * @return a copied instance of {@link OntVisualizer} with new settings
     */
    public OntVisualizer withOption(DOTSetting key, String value) {
        if (String.class != key.type) {
            throw new IllegalArgumentException("Wrong key: " + key);
        }
        return of(this.pm, this.classProperties, this.literalRenderer, this.entities, addOption(this.settings, key, value));
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
    public ClassPropertyMap classProperties() {
        return classProperties;
    }

    @Override
    public LiteralRenderer literalRenderer() {
        return literalRenderer;
    }

    @Override
    public boolean getBoolean(DOTSetting key) {
        return getSetting(key);
    }

    @Override
    public String getString(DOTSetting key) {
        return getSetting(key);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    protected <X> X getSetting(DOTSetting key) {
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
