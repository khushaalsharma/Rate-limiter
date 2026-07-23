package com.rate_limiter.app.kafka;

import com.rate_limiter.app.DTO.RateLimitEventDto;
import com.rate_limiter.app.models.RateLimitEvent;
import com.rate_limiter.app.repository.RateLimitEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitEventConsumer {

    private final RateLimitEventRepository eventRepository;

    /*
    * Spring creates a background thread that continuously polls the rate-limit-events topic.
    * Every time a message arrives, it deserializes the bytes into RateLimitEventDto
    * (using the factory you configured) and calls this method.
    * */

    @RetryableTopic(
        attempts = "4",
        backOff = @BackOff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener( //this annotation marks this as a consumer
            topics = "rate-limit-events",
            groupId = "analytics-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload RateLimitEventDto eventDto, //tells this parameter is deserialized message value
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, //pulled from kafka message metadata
            @Header(KafkaHeaders.OFFSET) long offset, //pulled from kafka message metadata
            Acknowledgment acknowledgement
    ){
        log.debug("Consuming event - clientKey={} allowed={} partition={} offset={}", eventDto.getClientKey(), eventDto.isAllowed(), partition, offset);

        if(eventRepository.existsByEventId(eventDto.getEventId())){
                acknowledgement.acknowledge();
                return;
        }

        RateLimitEvent event = RateLimitEvent.builder()
                .clientKey(eventDto.getClientKey())
                .endpoint(eventDto.getEndpoint())
                .algorithm(eventDto.getAlgorithm())
                .allowed(eventDto.isAllowed())
                .remainingRequests(eventDto.getRemainingRequests())
                .timestamp(eventDto.getTimestamp())
                .build();

        eventRepository.saveAndFlush(event);
        acknowledgement.acknowledge(); //manual acknowledgement

        /*
        * Why save to Postgres here and not in the service? Because the service is on the hot path — it
        * runs on every single HTTP request. A DB write there adds latency directly visible to the caller.
        * Here in the consumer, you're running asynchronously on a background thread.
        * The HTTP response has already been sent. The DB can be slow, the consumer can lag, and none of
        * it affects your rate limit check latency. That's the whole point of putting Kafka in the middle.
        * */
    }

    @DltHandler
    public void handleDlt(
        @Payload RateLimitEventDto eventDto,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.OFFSET) long offset
    ){
        log.error("Message in Kafka DLT topic={} offset={} event={}", topic, offset, eventDto);
    }
}
