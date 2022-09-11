package net.orbyfied.j8.text.component;

import net.orbyfied.j8.text.Component;
import net.orbyfied.j8.text.serialization.Serializers;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class Compound extends Component {

    // serializers
    public static final Serializers<Compound> SERIALIZERS = new Serializers<>(Compound.class)
            .forAny((comp, buf, ctx) -> {
                for (Component sub : comp.components) {
                    ctx.serialize(sub);
                }
            });

    ///////////////////////////

    // list of subcomponents
    final List<Component> components = new ArrayList<>();

    ///////////////////////////

    public Compound addLast(Component component) {
        components.add(component);
        return this;
    }

    public Compound addFirst(Component component) {
        components.add(0, component);
        return this;
    }

}
