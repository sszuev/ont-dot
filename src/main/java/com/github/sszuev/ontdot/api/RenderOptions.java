package com.github.sszuev.ontdot.api;

/**
 * Contains render settings.
 * Created by @ssz on 16.01.2022.
 */
public interface RenderOptions {

    /**
     * Answers {@code true} if the property is enabled.
     *
     * @param key {@link DOTSetting} the key
     * @return {@code boolean}
     * @see ClassPropertyMap
     */
    boolean getBoolean(DOTSetting key);

    /**
     * Answers {@code String}-value associated with the key.
     *
     * @param key {@link DOTSetting} the key
     * @return {@code String}
     */
    String getString(DOTSetting key);

    /**
     * Answers {@code true} if the class-properties belonging table should also be displayed.
     *
     * @return {@code boolean}
     * @see ClassPropertyMap
     */
    default boolean displayClassPropertiesMap() {
        return getBoolean(DOTSetting.BOOLEAN_CLASS_PROPERTIES_MAP);
    }

    default String classColor() {
        return getString(DOTSetting.STRING_CLASS_COLOR);
    }

    default String individualColor() {
        return getString(DOTSetting.STRING_INDIVIDUAL_COLOR);
    }

    default String datatypeColor() {
        return getString(DOTSetting.STRING_DATATYPE_COLOR);
    }

    default String objectPropertyColor() {
        return getString(DOTSetting.STRING_OBJECT_PROPERTY_COLOR);
    }

    default String dataPropertyColor() {
        return getString(DOTSetting.STRING_DATA_PROPERTY_COLOR);
    }

    default String annotationPropertyColor() {
        return getString(DOTSetting.STRING_ANNOTATION_PROPERTY_COLOR);
    }

    default String classExpressionColor() {
        return getString(DOTSetting.STRING_CLASS_EXPRESSION_COLOR);
    }

    default String literalColor() {
        return getString(DOTSetting.STRING_LITERAL_COLOR);
    }
}
