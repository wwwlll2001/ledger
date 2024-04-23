package com.assignment.ledger.common.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static com.assignment.ledger.common.Constant.CLOUD_EVENT_SOURCE;
import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;


/**
 * Message utils, support building cloud event conveniently
 */
public class MessageHelper {

    public static CloudEvent buildCloudEvent(Object data, String type) {
        CloudEventBuilder eventTemplate = CloudEventBuilder.v1()
                .withSource(URI.create(CLOUD_EVENT_SOURCE))
                .withType(type);

        String id = UUID.randomUUID().toString();
        byte[] dataByte;
        try {
            dataByte = getObjectMapper().writeValueAsString(data).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return eventTemplate.newBuilder()
                .withId(id)
                .withTime(OffsetDateTime.now())
                .withData(dataByte)
                .build();
    }
}
