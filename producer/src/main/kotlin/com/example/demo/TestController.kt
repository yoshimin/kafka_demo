package com.example.demo

import com.example.demo.model.User
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController(private val kafkaTemplate: KafkaTemplate<String, User>) {
    @PostMapping("/produce")
    private fun produce(@RequestBody user: User): HttpStatus {
        kafkaTemplate.send(kafkaTemplate.defaultTopic, user)
        return HttpStatus.OK
    }
}
