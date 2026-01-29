package com.ghtransport.transport.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 运输聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Transport extends AggregateRoot<TransportId> {

    private String waybillNo;
    private String dispatchId;
    private TransportStatus status;
    private Location currentLocation;
    private Double speed;
    private Double direction;
    private LocalDateTime estimatedArrival;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Transport() { super(); }

    public static Transport create(String dispatchId, String origin, String destination) {
        Transport t = new Transport(TransportId.generate());
        t.waybillNo = WaybillNo.generate();
        t.dispatchId = dispatchId;
        t.status = TransportStatus.PICKING_UP;
        t.createdAt = LocalDateTime.now();
        t.updatedAt = LocalDateTime.now();
        return t;
    }

    public void updateLocation(Double lng, Double lat) {
        this.currentLocation = new Location(lng, lat);
        this.updatedAt = LocalDateTime.now();
    }

    public void setStatus(TransportStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    @Override public String getAggregateType() { return "Transport"; }

    @Data @ValueObject public static class TransportId { private final String value; public static TransportId of(String v){return new TransportId(v);} public static TransportId generate(){return new TransportId(IdGenerator.generateUUID());} }
    @Data @ValueObject public static class WaybillNo { private final String value; public static WaybillNo of(String v){return new WaybillNo(v);} public static WaybillNo generate(){return new WaybillNo(IdGenerator.generateWaybillNo());} }
    @Data @ValueObject public static class Location { private final Double lng; private final Double lat; public Location(Double lng, Double lat){this.lng=lng;this.lat=lat;} }
    @Getter @ValueObject public static class TransportStatus {
        public static final TransportStatus PICKING_UP = new TransportStatus("PICKING_UP");
        public static final TransportStatus IN_TRANSIT = new TransportStatus("IN_TRANSIT");
        public static final TransportStatus DELIVERING = new TransportStatus("DELIVERING");
        public static final TransportStatus DELIVERED = new TransportStatus("DELIVERED");
        private final String value; private TransportStatus(String v){this.value=v;}
        public static TransportStatus of(String v){
            if (v == null) {
                throw new IllegalArgumentException("运输状态不能为空");
            }
            return switch(v.toUpperCase()){
                case "PICKING_UP" -> PICKING_UP;
                case "IN_TRANSIT" -> IN_TRANSIT;
                case "DELIVERING" -> DELIVERING;
                case "DELIVERED" -> DELIVERED;
                default -> throw new IllegalArgumentException("无效的运输状态: " + v);
            };
        }
    }
}
