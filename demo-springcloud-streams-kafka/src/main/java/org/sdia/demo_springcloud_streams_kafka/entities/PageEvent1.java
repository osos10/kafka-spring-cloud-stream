package org.sdia.demo_springcloud_streams_kafka.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
@Data@NoArgsConstructor@AllArgsConstructor@ToString
public class PageEvent1 {
    private String name;
    private String user;
    private Date date;
    private long duration;
}
