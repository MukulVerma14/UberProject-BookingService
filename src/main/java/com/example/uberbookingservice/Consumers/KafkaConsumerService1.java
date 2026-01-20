package com.example.uberbookingservice.Consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService1 {

    @KafkaListener(topics = "sample-topic", groupId = "sample-group-3")
    public void listen(String message) {
        System.out.println("Kafka message from new topic sample-topic : " +message);
    }
}
