package net.orbyfied.j8.event.handler;

import net.orbyfied.j8.event.handler.priority.Priorities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@HandlerDescriptor
public @interface PrioritizedHandler {

    Priorities priority() default Priorities.NORMAL;

}
