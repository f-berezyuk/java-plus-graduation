package ru.practicum.main.config;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.main.statistic.service.StatsService;

@Component
@AllArgsConstructor
@Slf4j
public class StatsInterceptor implements HandlerInterceptor {

    private final StatsService statsService;

    @Override
    public void afterCompletion(HttpServletRequest request,
                                @Nullable HttpServletResponse response,
                                @Nullable Object handler,
                                Exception ex) {
        statsService.createStats(request.getRequestURI(), request.getRemoteAddr());
        log.info("process statistic for {}", request.getRequestURI());
    }
}
