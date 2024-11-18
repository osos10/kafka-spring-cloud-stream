package org.sdia.demo_springcloud_streams_kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.sdia.demo_springcloud_streams_kafka.entities.PageEvent1;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class PageEventService {
    @Bean
    public Consumer<PageEvent1> pageEvent1Consumer(){
        return (input)->{
            System.out.println("***********");
            System.out.println(input.toString());
            System.out.println("***********");
        };

    }
    @Bean
    public Supplier<PageEvent1> pageEvent1Supplier(){
        return ()->new PageEvent1(
                Math.random()>0.5?"P1":"P2",
                Math.random()>0.5?"U1":"U2",
                new Date(),
                new Random().nextInt(9000));



    }
    @Bean
    public Function<PageEvent1,PageEvent1> pageEvent1Function(){
        return (input)->{
            input.setName("Page Event");
            input.setUser("UUUU");
            return input;
        };
    }


    @Bean
    public Function<KStream<String, PageEvent1>, KStream<String, Long>> KStream1Function() {
        return input -> {
            return input
                    .filter((k, v) -> v.getDuration() > 100) // Filter events
                    .map((k, v) -> new KeyValue<>(v.getName(), 0L)) // Map to new key-value pair
                    .groupBy((k, v) -> k, Grouped.with(Serdes.String(), Serdes.Long())) // Group by key
                    .windowedBy(TimeWindows.of(Duration.ofMillis(5000))) // Windowing
                    .count(Materialized.as("page-count")) // Count occurrences
                    .toStream()
                    .map((k, v) -> new KeyValue<>(
                            "=>" + k.window().startTime() + "-" + k.window().endTime() + ": " + k.key(),
                            v
                    )); // Map to output key-value
        };
    }



}
