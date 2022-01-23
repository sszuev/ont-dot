package com.github.sszuev.ontdot.renderers;

import com.github.sszuev.ontdot.api.DOTConfig;

import java.io.Writer;

/**
 * Created by @ssz on 15.01.2022.
 */
public class DOTWriterFactory {

    /**
     * Creates a {@link DOTWriter} to write into specified {@link Writer}.
     *
     * @param conf {@link DOTConfig}, not {@code null}
     * @param wr   {@link Writer}
     * @return {@link DOTWriter}
     */
    public static DOTWriter create(DOTConfig conf, Writer wr) {
        if (conf.entities().isEmpty()) {
            return new GraphDOTWriter(conf.prefixes(), conf.classProperties(), conf.literalRenderer(), wr, conf);
        }
        return new EntitiesDOTWriter(conf.prefixes(), conf.classProperties(), conf.literalRenderer(), conf, conf.entities(), wr);
    }
}
