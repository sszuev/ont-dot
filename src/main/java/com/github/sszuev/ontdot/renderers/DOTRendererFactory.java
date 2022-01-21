package com.github.sszuev.ontdot.renderers;

import com.github.sszuev.ontdot.api.DOTConfig;

import java.io.Writer;

/**
 * Created by @ssz on 15.01.2022.
 */
public class DOTRendererFactory {

    /**
     * Creates a {@link DOTRenderer} to write into specified {@link Writer}.
     *
     * @param config {@link DOTConfig}, not {@code null}
     * @param wr     {@link Writer}
     * @return {@link DOTRenderer}
     */
    public static DOTRenderer create(DOTConfig config, Writer wr) {
        if (config.entities().isEmpty()) {
            return new GraphDOTRenderer(config.prefixes(), config.classProperties(), config, wr);
        }
        return new EntitiesDOTRenderer(config.prefixes(), config.classProperties(), config, config.entities(), wr);
    }
}
