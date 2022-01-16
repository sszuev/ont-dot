package com.github.sszuev.ontdot.api;

/**
 * Collection of available settings.
 */
public enum DOTSetting {
    BOOLEAN_CLASS_PROPERTIES_MAP("classPropertiesMap", Boolean.class, false),
    ;
    final String name;
    final Object defaultValue;
    final Class<?> type;

    DOTSetting(String name, Class<?> type, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public Class<?> type() {
        return type;
    }

    public static DOTSetting ofKey(String key) {
        for (DOTSetting k : values()) {
            if (k.name.equalsIgnoreCase(key)) {
                return k;
            }
        }
        return null;
    }
}
