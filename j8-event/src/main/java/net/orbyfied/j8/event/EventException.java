package net.orbyfied.j8.event;

public class EventException extends RuntimeException {

    final EventBus bus;

    public EventException(EventBus bus, String message) {
        super(message);
        this.bus = bus;
    }

    public EventException(EventBus bus, Exception e) {
        super(e);
        this.bus = bus;
    }

    public EventException(EventBus bus, String msg, Exception e) {
        super(msg, e);
        this.bus = bus;
    }

    public EventBus getBus() {
        return bus;
    }

}
