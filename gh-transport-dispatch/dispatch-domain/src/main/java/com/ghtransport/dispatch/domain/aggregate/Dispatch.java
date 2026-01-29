package com.ghtransport.dispatch.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 调度单聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Dispatch extends AggregateRoot<DispatchId> {

    private DispatchNo dispatchNo;
    private String orderId;
    private String vehicleId;
    private String driverId;
    private DispatchStatus status;
    private LocalDateTime plannedTime;
    private LocalDateTime actualTime;
    private String originAddress;
    private String destinationAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Dispatch() { super(); }

    public static Dispatch create(String orderId, String vehicleId, String driverId,
                                   LocalDateTime plannedTime, String origin, String destination) {
        Dispatch d = new Dispatch(DispatchId.generate());
        d.dispatchNo = DispatchNo.generate();
        d.orderId = orderId;
        d.vehicleId = vehicleId;
        d.driverId = driverId;
        d.status = DispatchStatus.PENDING;
        d.plannedTime = plannedTime;
        d.originAddress = origin;
        d.destinationAddress = destination;
        d.createdAt = LocalDateTime.now();
        d.updatedAt = LocalDateTime.now();
        return d;
    }

    public void assign() {
        this.status = DispatchStatus.ASSIGNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.status = DispatchStatus.IN_TRANSIT;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = DispatchStatus.COMPLETED;
        this.actualTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override public String getAggregateType() { return "Dispatch"; }

    @Data @ValueObject public static class DispatchId { private final String value; public static DispatchId of(String v){return new DispatchId(v);} public static DispatchId generate(){return new DispatchId(IdGenerator.generateUUID());} }
    @Data @ValueObject public static class DispatchNo { private final String value; public static DispatchNo of(String v){return new DispatchNo(v);} public static DispatchNo generate(){return new DispatchNo(IdGenerator.generateDispatchNo());} }
    @Getter @ValueObject public static class DispatchStatus {
        public static final DispatchStatus PENDING = new DispatchStatus("PENDING");
        public static final DispatchStatus ASSIGNED = new DispatchStatus("ASSIGNED");
        public static final DispatchStatus IN_TRANSIT = new DispatchStatus("IN_TRANSIT");
        public static final DispatchStatus COMPLETED = new DispatchStatus("COMPLETED");
        public static final DispatchStatus CANCELLED = new DispatchStatus("CANCELLED");
        private final String value; private DispatchStatus(String v){this.value=v;}
        public static DispatchStatus of(String v){
            if (v == null) {
                throw new IllegalArgumentException("调度状态不能为空");
            }
            return switch(v.toUpperCase()){
                case "PENDING" -> PENDING;
                case "ASSIGNED" -> ASSIGNED;
                case "IN_TRANSIT" -> IN_TRANSIT;
                case "COMPLETED" -> COMPLETED;
                case "CANCELLED" -> CANCELLED;
                default -> throw new IllegalArgumentException("无效的调度状态: " + v);
            };
        }
    }
}
