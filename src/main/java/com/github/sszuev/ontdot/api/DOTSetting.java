package com.github.sszuev.ontdot.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Collection of available settings.
 */
public enum DOTSetting {
    BOOLEAN_CLASS_PROPERTIES_MAP("classPropertiesMap", Boolean.class, false, "display class-properties table"),
    BOOLEAN_ENTITY_ANNOTATIONS("entityAnnotations", Boolean.class, false, "display entity annotations table"),

    STRING_CLASS_COLOR("classColor", String.class, "#CFA500"),
    STRING_DATATYPE_COLOR("datatypeColor", String.class, "#AD3B45"),
    STRING_INDIVIDUAL_COLOR("individualColor", String.class, "#874B82"),
    STRING_OBJECT_PROPERTY_COLOR("objectPropertyColor", String.class, "#0079BA"),
    STRING_DATA_PROPERTY_COLOR("dataPropertyColor", String.class, "#38A14A"),
    STRING_ANNOTATION_PROPERTY_COLOR("annotationPropertyColor", String.class, "#D17A00"),
    STRING_LITERAL_COLOR("literalColor", String.class, "gray"),
    STRING_CLASS_EXPRESSION_COLOR("classExpressionColor", String.class, "#CCCC00"),
    STRING_COMPONENT_RESTRICTION_COLOR("componentRestrictionColor", String.class, "yellow1"),
    STRING_COMPONENTS_CE_COLOR("componentsClassExpressionColor", String.class, "yellow2"),
    STRING_COMPLEMENT_CE_COLOR("complementOfClassExpressionColor", String.class, "yellow3"),

    INT_LITERAL_ROW_LENGTH("literalRowLength", Integer.class, 42),
    INT_LITERAL_ROWS_NUM("literalRowsNumber", Integer.class, 4),
    ;
    final String key;
    final String description;
    final Object defaultValue;
    final Class<?> type;

    DOTSetting(String name, Class<?> type, Object defaultValue) {
        this(name, type, defaultValue, null);
    }

    DOTSetting(String name, Class<?> type, Object defaultValue, String description) {
        this.key = name;
        this.defaultValue = defaultValue;
        this.type = type;
        this.description = description == null ? defaultDescription(name()) : description;
    }

    public Class<?> type() {
        return type;
    }

    public String key() {
        return key;
    }

    public String description() {
        return description;
    }

    public static DOTSetting ofKey(String key) {
        for (DOTSetting k : values()) {
            if (k.key.equalsIgnoreCase(key)) {
                return k;
            }
        }
        return null;
    }

    private static String defaultDescription(String name) {
        return Arrays.stream(name.split("_")).skip(1)
                .map(x -> x.toLowerCase(Locale.ENGLISH)).collect(Collectors.joining(" "));
    }
}
