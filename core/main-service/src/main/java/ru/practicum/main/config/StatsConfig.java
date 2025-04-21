package ru.practicum.main.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.interaction.ewm.stats.client.StatsClient;

@Configuration
public class StatsConfig {
    @Bean
    public StatsClient getStatClient(DiscoveryClient discoveryClient) {
        return new StatsClient(discoveryClient);
    }
}