package net.orbyfied.j8.event.service;

import net.orbyfied.j8.event.EventBus;

public class AbstractEventService implements EventService {

    protected final EventBus bus;

    public AbstractEventService(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public EventBus getBus() {
        return bus;
    }

}
