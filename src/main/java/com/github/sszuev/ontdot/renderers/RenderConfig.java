package com.github.sszuev.ontdot.renderers;

/**
 * Contains render settings.
 * Created by @ssz on 16.01.2022.
 */
public interface RenderConfig {

    /**
     * Answers {@code true} if the class-properties belonging table should also be displayed.
     *
     * @return {@code boolean}
     * @see com.github.sszuev.ontdot.utils.ClassPropertyMap
     */
    boolean displayClassPropertiesMap();
}
