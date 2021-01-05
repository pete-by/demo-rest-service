package com.axamit.springboot.demo.demorestservice.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static org.springframework.boot.availability.AvailabilityChangeEvent.publish;
import static org.springframework.boot.availability.LivenessState.BROKEN;
import static org.springframework.boot.availability.LivenessState.CORRECT;
import static org.springframework.boot.availability.ReadinessState.ACCEPTING_TRAFFIC;
import static org.springframework.boot.availability.ReadinessState.REFUSING_TRAFFIC;

/**
 * Graceful shutdown:
 * 1. Receive SIGTERM or PreStop LiveCycle hook
 * 2. Fail Readiness
 * 3. Receive requests until Kubernetes reads readiness probe failure and stops traffic to the pod
 * 4. Finish processing in-flight requests
 * 5. Shutdown
 */
@RestController
class FailureSimulatorController {

    private static final Logger logger = LoggerFactory.getLogger(FailureSimulatorController.class);

    private ApplicationEventPublisher eventPublisher;

    @Inject
    public FailureSimulatorController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/complete-normally")
    public String completeNormally() {
        return "Hello from Controller";
    }

    @GetMapping("/i-will-sleep-for-30sec")
    public String destroy() throws Exception {
        Thread.sleep(30000);
        return "sleep complete";
    }

    @GetMapping("/readiness/accepting")
    public String markAcceptingTraffic() {
        publish(eventPublisher, this, ACCEPTING_TRAFFIC);
        return "Readiness marked as ACCEPTING_TRAFFIC";
    }

    @GetMapping("/readiness/refuse")
    public String markRefusingTraffic() {
        publish(eventPublisher, this, REFUSING_TRAFFIC);
        return "Readiness marked as REFUSING_TRAFFIC";
    }

    @GetMapping("/liveness/correct")
    public String markCorrect() {
        publish(eventPublisher, this, CORRECT);
        return "Liveness marked as CORRECT";
    }

    @GetMapping("/liveness/broken")
    public String markBroken() {
        publish(eventPublisher, this, BROKEN);
        return "Liveness marked as BROKEN";
    }

    @GetMapping("/events/preStop")
    String stopping() {
        logger.info("stopping...");
        return "Stopping...";
    }

}
