package com.github.sszuev.ontdot.api;

/**
 * Contains DOT-render settings.
 * Created by @ssz on 16.01.2022.
 */
public interface DOTOptions extends RenderOptions, LiteralOptions {

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
