package com.rate_limiter.app.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AnalyticsResponse {
    private String clientKey;
    private long totalRequests;
    private long allowedRequests;
    private long rejectedRequests;
    private double rejectionRate;
    private LocalDateTime from;
    private LocalDateTime to;
}
