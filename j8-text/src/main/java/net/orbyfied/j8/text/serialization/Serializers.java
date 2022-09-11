package net.orbyfied.j8.text.serialization;

import net.orbyfied.j8.text.Buffer;
import net.orbyfied.j8.text.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class Serializers<C extends Component> {

    // component type
    final Class<C> componentType;

    // serializer for any type
    Serializer<C, Buffer, Serialization<? extends Buffer>> anySerializer;

    // serializers for type
    Map<Class<? extends Serialization<?>>, Serializer<C, Buffer, Serialization<? extends Buffer>>> serializersByType = new HashMap<>();

    /**
     * Constructor.
     * @param componentType The component type.
     */
    public Serializers(Class<C> componentType) {
        this.componentType = componentType;
    }

    /* Getter. */

    public Class<C> getComponentType() {
        return componentType;
    }

    public Serializer<C, ?, ?> getSerializerForAny() {
        return anySerializer;
    }

    public Map<Class<? extends Serialization<?>>, Serializer<C, ?, ?>> getSerializersByType() {
        return Collections.unmodifiableMap(serializersByType);
    }

    public Serializer<C, ?, ?> getSerializerForType(Class<? extends Serialization<?>> kl) {
        return serializersByType.get(kl);
    }

    /* Modify. */

    public Serializers<C> forAny(Serializer<C, Buffer, Serialization<? extends Buffer>> serializer) {
        this.anySerializer = serializer;
        return this;
    }

    public <B extends Buffer, S extends Serialization<B>>
    Serializers<C> forType(Class<? extends Serialization<?>> sType, Serializer<C, Buffer, Serialization<? extends Buffer>> serializer) {
        serializersByType.put(sType, serializer);
        return this;
    }

    /* Process. */

    public Serializers<C> serialize(Serialization<? extends Buffer> serialization, C component) {
        // call any serializer
        if (anySerializer != null)
            anySerializer.serialize(component, serialization.getBuffer(), serialization);

        // call typed serializer
        Serializer<C, Buffer, Serialization<? extends Buffer>> typedSerializer = serializersByType.get(serialization.getClass());
        if (typedSerializer != null)
            typedSerializer.serialize(component, serialization.getBuffer(), serialization);

        // return
        return this;
    }

}
