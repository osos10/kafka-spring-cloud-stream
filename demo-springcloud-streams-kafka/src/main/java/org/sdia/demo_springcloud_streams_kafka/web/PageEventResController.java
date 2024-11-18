package org.sdia.demo_springcloud_streams_kafka.web;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.sdia.demo_springcloud_streams_kafka.entities.PageEvent1;
import reactor.core.publisher.Flux;


import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
public class PageEventResController {
    @Autowired
    private StreamBridge  streamBridge;
    @Autowired
    private InteractiveQueryService interactiveQueryService;
    @GetMapping("/publish/{topic}/{name}")
    public PageEvent1 publish(@PathVariable String topic,@PathVariable String name){
        PageEvent1 pageEvent1=new PageEvent1(name,Math.random()>0.5?"U1":"U2",new Date(),new Random().nextInt(9000));
        streamBridge.send(topic,pageEvent1);
        return  pageEvent1;
    }
    @GetMapping(value = "/analytics",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String,Long>> analytics(){
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq->{
                    Map<String,Long> map=new HashMap<>();
                    ReadOnlyWindowStore<String, Long> stats = interactiveQueryService.getQueryableStore("page-count", QueryableStoreTypes.windowStore());
                    Instant now=Instant.now();
                    Instant from=now.minusSeconds(5);
                    KeyValueIterator<Windowed<String>, Long> fetchAll = stats.fetchAll(from,now);
                    while (fetchAll.hasNext()){
                        KeyValue<Windowed<String>, Long> next = fetchAll.next();
                        map.put(next.key.key(),next.value);
                    }
                    return map;
                });
    }

}
