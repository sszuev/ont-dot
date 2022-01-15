package com.github.sszuev.ontdot.renderers;

import com.github.sszuev.ontdot.api.DOTConfig;

import java.io.Writer;

/**
 * Created by @ssz on 15.01.2022.
 */
public class DOTRendererFactory {

    public static DOTRenderer create(DOTConfig config, Writer w) {
        if (config.entities().isEmpty()) {
            return new DOTRenderer(config.prefixes(), w);
        }
        return new WithEntityFilterDOTRenderer(config.prefixes(), w, config.entities());
    }
}
