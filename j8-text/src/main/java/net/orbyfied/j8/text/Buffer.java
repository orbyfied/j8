package net.orbyfied.j8.text;

import net.orbyfied.j8.text.serialization.Serialization;

/**
 * Class to append and store the final
 * output of the serialization process.
 * @param <F> The final output type.
 */
public abstract class Buffer<F> {

    public enum ExitReason {

        END,
        ERROR

    }

    //////////////////////////////////////////

    // serialization context
    protected final Serialization serialization;

    /**
     * Constructor.
     * @param serialization The serialization context.
     */
    public Buffer(Serialization serialization) {
        this.serialization = serialization;
    }

    public abstract void begin(Component component);

    public abstract void beginWrite();

    public abstract void end(ExitReason reason);

    public abstract F build();

}
