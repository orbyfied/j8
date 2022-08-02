package net.orbyfied.j8.event.handler;

import net.orbyfied.j8.event.BusHandler;
import net.orbyfied.j8.event.EventBus;
import net.orbyfied.j8.event.RegisteredListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a basic event handler method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@HandlerDescriptor
public @interface BasicHandler {

    HandlerProvider<BasicHandler> PROVIDER = new Provider();

    class Provider implements HandlerProvider<BasicHandler> {

        @Override
        public void configure(BusHandler handler, BasicHandler desc) { }

        @Override
        public void register(EventBus bus, BusHandler handler) {
            bus.getPipelineFor(handler.getProperty(RegisteredListener.HANDLER_PROPERTY_EVENT_CLASS))
                    .base()
                    .handler(handler).register();
        }

        @Override
        public void unregister(EventBus bus, BusHandler handler) {
            bus.getPipelineFor(handler.getProperty(RegisteredListener.HANDLER_PROPERTY_EVENT_CLASS))
                    .base()
                    .handler(handler).unregister();
        }

    }

}
