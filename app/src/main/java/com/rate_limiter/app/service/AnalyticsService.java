package com.rate_limiter.app.service;

import com.rate_limiter.app.models.RateLimitEvent;
import com.rate_limiter.app.repository.RateLimitEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final RateLimitEventRepository eventRepository;

    public Map<String, Object> getStats(String clientKey, int lastNMinutes){
        LocalDateTime from = LocalDateTime.now().minusMinutes(lastNMinutes);
        LocalDateTime now = LocalDateTime.now();

        List<RateLimitEvent> events = eventRepository.findByClientKeyAndTimestampBetween(clientKey, from, now);

        long total = events.size();
        long allowed = events.stream().filter(RateLimitEvent::isAllowed).count();
        long rejected = total - allowed;

        double rejectionRate = total == 0 ? 0.0 : (double) rejected/total * 100;

        return Map.of(
                "clientKey", clientKey,
                "from", from.toString(),
                "to", now.toString(),
                "totalRequests", total,
                "allowedRequests", allowed,
                "rejectedRequests", rejected,
                "rejectionRate", String.format("%.2f%%", rejectionRate)
        );
    }
}
