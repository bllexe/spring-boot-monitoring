package org.tigris.springbootmonitoring;

import io.micrometer.core.instrument.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class MonitoringController {

    private final Counter counter;
    private final Timer timer;
    private final DistributionSummary distributionSummary;

    private List<Integer> queue = new ArrayList<>(Arrays.asList(1, 2, 3, 4));

    public MonitoringController(MeterRegistry registry) {
        counter = Counter.builder("visit_counter")
                .tags("counter_tag", "visitors")
                .description("Number of visits")
                .register(registry);

        timer = Timer.builder("visit_timer")
                .tags("timer_tag", "visitors")
                .description("Time spent on visits")
                .register(registry);

        Gauge.builder("queue_size", queue, List::size).register(registry);

        distributionSummary = DistributionSummary.builder("http_request_duration")
                .publishPercentileHistogram()
                .description("Time spent on visits")
                .register(registry);
    }

    @GetMapping("/visitApi")
    public String visitCounter() {
        counter.increment();
        return "Api  Called : " + counter.count();
    }

    @GetMapping("/getResponseTime")
    public String timerExample() throws InterruptedException {
        Timer.Sample sample = Timer.start();

        System.out.println("Doing some work");

        Thread.sleep(getRandomNumber(500, 1000));
        if (!queue.isEmpty()) {
            queue.remove(0);
        }
        double responseTimeInMilliSeconds = timer.record(() -> sample.stop(timer) / 1000000);
        return "ResponseTime example api called :" + responseTimeInMilliSeconds;

    }

    public int getRandomNumber(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    @GetMapping("/histogram")
    public String histogramExample() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        Thread.sleep(getRandomNumber(10, 1000));
        long duration = System.currentTimeMillis() - startTime;

        distributionSummary.record(duration);
        return "histogram api called :" + duration;
    }


}
