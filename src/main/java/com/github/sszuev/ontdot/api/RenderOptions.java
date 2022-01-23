package com.github.sszuev.ontdot.api;

/**
 * The generic facility to retrieve desired config options.
 * <p>
 * Created by @ssz on 23.01.2022.
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
     * Answers {@code Integer}-value associated with the key.
     *
     * @param key {@link DOTSetting} the key
     * @return {@code Integer}
     */
    Integer getInteger(DOTSetting key);
}
