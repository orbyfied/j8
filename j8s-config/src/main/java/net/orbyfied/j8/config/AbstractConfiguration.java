package net.orbyfied.j8.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Abstract implementation of
 * {@link net.orbyfied.carbon.config.Configuration}
 * @see net.orbyfied.carbon.config.Configuration
 */
public abstract class AbstractConfiguration implements net.orbyfied.carbon.config.Configuration {

    /**
     * The configurable that this
     * configuration is for. Can be null.
     */
    protected final Configurable<?> configurable;

    /** Constructor. */
    public AbstractConfiguration(Configurable<?> configurable) {
        this.configurable = configurable;
    }

    /**
     * @see net.orbyfied.carbon.config.Configuration#getConfigurable()
     */
    @Override
    public Configurable<?> getConfigurable() {
        return configurable;
    }

    // Override save and load to make them final.

    @Override
    public final void save(ConfigurationSection config) {
        net.orbyfied.carbon.config.Configuration.super.save(config);
    }

    @Override
    public final void load(ConfigurationSection config) {
        net.orbyfied.carbon.config.Configuration.super.load(config);
    }

}
