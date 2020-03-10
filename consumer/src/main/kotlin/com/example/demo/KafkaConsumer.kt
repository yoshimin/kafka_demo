package com.example.demo

import com.example.demo.model.User
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class KafkaConsumer() {
    @KafkaListener(topics = ["test-topic"], groupId = "test-group")
    fun listen(@Payload user: User) {
        if (user.name == "error") {
            throw RuntimeException()
        }
        println("message received: $user")
    }

    @KafkaListener(topics = ["test-topic.DLT"], groupId = "test-group")
    fun recover(@Payload user: User) {
        println("message failed: $user")
    }
}
