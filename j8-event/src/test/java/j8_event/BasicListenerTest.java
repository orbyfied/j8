package j8_event;

import net.orbyfied.j8.event.BusEvent;
import net.orbyfied.j8.event.BusHandler;
import net.orbyfied.j8.event.EventBus;
import net.orbyfied.j8.event.EventListener;
import net.orbyfied.j8.event.handler.BasicHandler;
import net.orbyfied.j8.event.pipeline.PipelineAccess;
import net.orbyfied.j8.event.util.Pipelines;
import org.junit.jupiter.api.Test;

public class BasicListenerTest {

    @Test
    void test_BasicListener() {
        EventBus bus = new EventBus();
        bus.register(new MyListener());

        bus.post(new MyEvent());
    }

    public static class MyListener implements EventListener {

        @BasicHandler
        void onEvent(MyEvent event) {
            System.out.println("called");
        }

    }

    public static class MyEvent extends BusEvent {

        public static PipelineAccess<BusEvent> getPipeline(EventBus bus) {
            return Pipelines.mono(bus);
        }

    }

}
