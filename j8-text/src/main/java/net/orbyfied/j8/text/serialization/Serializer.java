package net.orbyfied.j8.text.serialization;

import net.orbyfied.j8.text.Buffer;
import net.orbyfied.j8.text.Component;

@SuppressWarnings("rawtypes")
public interface Serializer<C extends Component, B extends Buffer, S extends Serialization> {

    void serialize(C comp, B buf, S ctx);

}
