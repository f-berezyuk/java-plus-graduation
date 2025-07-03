package ru.practicum.stats.collector.producer;

import java.util.Properties;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProducerFabric {

    @Value("${spring.kafka.producer.properties.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.properties.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.properties.value-serializer}")
    private String valueSerializer;

    @Bean
    public Producer<Long, SpecificRecordBase> getProducer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        return new KafkaProducer<>(config);
    }
}
