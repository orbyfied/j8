package net.orbyfied.j8.event.service;

import net.orbyfied.j8.event.EventBus;

/**
 * A service that can be applied
 * to an event bus.
 */
public interface EventService {

    EventBus getBus();

}
