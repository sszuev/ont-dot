package com.github.sszuev.ontdot.api;

/**
 * Collection of available settings.
 */
public enum DOTSettings {
    CLASS_PROPERTIES_MAP(false),
    ;
    final Object defaultValue;

    DOTSettings(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
