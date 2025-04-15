package ru.practicum.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@AllArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {
    private StatsInterceptor statsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(statsInterceptor)
                .addPathPatterns("/**");
        log.info("Register statistic interceptor.");
    }
}

