package com.example.demo

import com.example.demo.model.User
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff


@ConstructorBinding
@ConfigurationProperties(prefix = "kafka")
class KafkaProperties(
        var server: String,
        var groupId: String
)

@Configuration
class KafkaConfiguration(private val properties: KafkaProperties) {
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
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerConfigs(): Map<String, Any> {
        return mapOf(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to properties.server,
                ConsumerConfig.GROUP_ID_CONFIG to properties.groupId,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java
        )
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, User> {
        return DefaultKafkaConsumerFactory(
                consumerConfigs(),
                StringDeserializer(),
                JsonDeserializer(User::class.java)
                )
    }

    @Bean
    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, User>,
                                      kafkaTemplate: KafkaTemplate<String, User>): ConcurrentKafkaListenerContainerFactory<String, User> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, User>()
        factory.consumerFactory = consumerFactory

        val errorHandler = SeekToCurrentErrorHandler(
                DeadLetterPublishingRecoverer(kafkaTemplate),
                FixedBackOff(5000, 5)
        )
        factory.setErrorHandler(errorHandler)
        return factory
    }
}
