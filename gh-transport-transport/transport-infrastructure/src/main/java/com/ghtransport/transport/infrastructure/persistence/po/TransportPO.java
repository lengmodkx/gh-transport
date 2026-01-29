package com.ghtransport.transport.infrastructure.persistence.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * 运输持久化对象（MongoDB）
 */
@Data
@Document(collection = "transport")
public class TransportPO {
    @Id
    private String id;
    private String waybillNo;
    private String dispatchId;
    private String status;
    @Field("current_location")
    private LocationPO currentLocation;
    private Double speed;
    private Double direction;
    @Field("estimated_arrival")
    private LocalDateTime estimatedArrival;
    @Field("created_at")
    private LocalDateTime createdAt;
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Data
    public static class LocationPO {
        private Double lng;
        private Double lat;
    }
}
