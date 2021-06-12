package io.lcalmsky.springbootwithkubernetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAsync
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @RestController
    public static class ControlPlaneController {
        private final ApplicationEventPublisher eventPublisher;

        public ControlPlaneController(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @GetMapping("/block")
        @ResponseStatus(HttpStatus.OK)
        public void block() {
            AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
        }

        @GetMapping("/turnoff")
        @ResponseStatus(HttpStatus.OK)
        public void turnoff() {
            AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
        }

        @Async
        @EventListener
        public void onStateChanged(AvailabilityChangeEvent<ReadinessState> event) throws InterruptedException {
            if (event.getState() == ReadinessState.REFUSING_TRAFFIC) {
                Thread.sleep(15000L);
                AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
            }
        }
    }
}
