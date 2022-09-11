package net.orbyfied.j8.text.serialization;

import net.orbyfied.j8.text.Buffer;
import net.orbyfied.j8.text.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.Stack;

@SuppressWarnings("rawtypes")
public class Serialization<B extends Buffer> {

    // serializer list cache
    private static final HashMap<Class<? extends Component>, Serializers<? extends Component>> serializerCache = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <C extends Component> Serializers<C> getSerializersFor(C component) {
        // get class
        Class<C> klass = (Class<C>) component.getClass();

        // try and get from cache
        Serializers<C> serializers = (Serializers<C>) serializerCache.get(klass);

        try {
            // get serializers from reflection
            Field field = component.getClass().getDeclaredField("SERIALIZERS");
            field.setAccessible(true);

            // get serializers
            serializers = (Serializers<C>) field.get(null);
        } catch (NoSuchFieldException e) {
            System.err.println("No serializers defined for component type " + klass.getName());
        } catch (InaccessibleObjectException e) {
            System.err.println("Failed to access serializers for component type " + klass.getName());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to get serializers for component type " + klass.getName());
            e.printStackTrace();
        }

        // return
        return serializers;
    }

    //////////////////////////////////////////////

    // the buffer
    protected final B buffer;

    // the serialization type
    protected final String type;

    // the work component stack
    protected final Stack<Component> componentStack = new Stack<>();

    /**
     * Constructor.
     * @param buffer The buffer to serialize into.
     */
    public Serialization(String type, B buffer) {
        this.type   = type;
        this.buffer = buffer;
    }

    /* Getters. */

    public String getType() {
        return type;
    }

    public B getBuffer() {
        return buffer;
    }

    public Stack<Component> getComponentStack() {
        return componentStack;
    }

    /* Process. */

    public Serialization<B> serialize(Component component) {
        // begin component in buffer
        buffer.begin(component);

        // serialize component
        Serializers<Component> serializers = getSerializersFor(component);
        if (serializers == null) {
            buffer.end(Buffer.ExitReason.ERROR);
            return this;
        }

        serializers.serialize(this, component);

        // end component in buffer
        buffer.end(Buffer.ExitReason.END);

        // return
        return this;
    }

}
