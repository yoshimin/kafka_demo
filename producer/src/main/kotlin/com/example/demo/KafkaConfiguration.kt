package com.example.demo

import com.example.demo.model.User
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@ConstructorBinding
@ConfigurationProperties(prefix = "kafka")
class KafkaProperties(
        var server: String,
        var topic: Topic
){
    class Topic(
            var name: String,
            var numPartitions: Int,
            var replicationFactor: Short
    )
}

@Configuration
class KafkaConfiguration(private val properties: KafkaProperties) {
    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs = mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to properties.server)
        return KafkaAdmin(configs)
    }

    @Bean
    fun topic(): NewTopic {
        val topic = properties.topic
        return NewTopic(topic.name, topic.numPartitions, topic.replicationFactor)
    }

    @Bean
    fun producerConfigs(): Map<String, Any> {
        return mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.server,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
    }

    @Bean
    fun producerFactory(): ProducerFactory<String, User> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, User> {
        val template = KafkaTemplate(producerFactory())
        template.defaultTopic = properties.topic.name
        return template
    }
}
