package com.ghtransport.transport.infrastructure.persistence.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.transport.domain.aggregate.Transport;
import com.ghtransport.transport.domain.aggregate.Transport.TransportId;
import com.ghtransport.transport.domain.aggregate.Transport.TransportStatus;
import com.ghtransport.transport.domain.aggregate.Transport.WaybillNo;
import com.ghtransport.transport.domain.repository.TransportRepository;
import com.ghtransport.transport.infrastructure.persistence.po.TransportPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 运输仓储实现（MongoDB）
 */
@Slf4j
@Repository
public class TransportRepositoryImpl implements TransportRepository {

    private final MongoTemplate mongoTemplate;
    private static final String COLLECTION_NAME = "transport";

    public TransportRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<Transport> findById(Transport.TransportId id) {
        TransportPO po = mongoTemplate.findById(id.getValue(), TransportPO.class);
        return Optional.ofNullable(toAggregate(po));
    }

    @Override
    public Optional<Transport> findByWaybillNo(WaybillNo waybillNo) {
        Query query = Query.query(Criteria.where("waybillNo").is(waybillNo.getValue()));
        TransportPO po = mongoTemplate.findOne(query, TransportPO.class);
        return Optional.ofNullable(toAggregate(po));
    }

    @Override
    public Optional<Transport> findByDispatchId(String dispatchId) {
        Query query = Query.query(Criteria.where("dispatchId").is(dispatchId));
        TransportPO po = mongoTemplate.findOne(query, TransportPO.class);
        return Optional.ofNullable(toAggregate(po));
    }

    @Override
    public PageResult<Transport> findByStatus(Transport.TransportStatus status, int pageNum, int pageSize) {
        Query query = Query.query(Criteria.where("status").is(status.getValue()))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        long total = mongoTemplate.count(query, TransportPO.class);
        query.skip((long) pageSize * (pageNum - 1)).limit(pageSize);
        List<TransportPO> pos = mongoTemplate.find(query, TransportPO.class);
        return new PageResult<>(
                pos.stream().map(this::toAggregate).toList(),
                (int) total,
                pageNum,
                pageSize
        );
    }

    @Override
    public PageResult<Transport> findByVehicleId(String vehicleId, int pageNum, int pageSize) {
        Query query = Query.query(Criteria.where("dispatchId").exists(true))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        long total = mongoTemplate.count(query, TransportPO.class);
        query.skip((long) pageSize * (pageNum - 1)).limit(pageSize);
        List<TransportPO> pos = mongoTemplate.find(query, TransportPO.class);
        return new PageResult<>(
                pos.stream().map(this::toAggregate).toList(),
                (int) total,
                pageNum,
                pageSize
        );
    }

    @Override
    public void save(Transport transport) {
        TransportPO po = toPO(transport);
        boolean exists = mongoTemplate.exists(
                Query.query(Criteria.where("_id").is(po.getId())), TransportPO.class);
        if (exists) {
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(po.getId())),
                    Update.from(po),
                    TransportPO.class
            );
            log.debug("运单更新成功: {}", transport.getId().getValue());
        } else {
            mongoTemplate.save(po, COLLECTION_NAME);
            log.debug("运单保存成功: {}", transport.getId().getValue());
        }
    }

    @Override
    public void delete(Transport.TransportId id) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(id.getValue())), TransportPO.class);
        log.debug("运单删除成功: {}", id.getValue());
    }

    /**
     * 将PO转换为Aggregate
     */
    private Transport toAggregate(TransportPO po) {
        if (po == null) return null;
        Transport transport = new Transport();
        transport.setId(TransportId.of(po.getId()));
        transport.setWaybillNo(WaybillNo.of(po.getWaybillNo()));
        transport.setDispatchId(po.getDispatchId());
        transport.setStatus(TransportStatus.of(po.getStatus()));
        if (po.getCurrentLocation() != null) {
            transport.setCurrentLocation(new Transport.Location(
                    po.getCurrentLocation().getLng(),
                    po.getCurrentLocation().getLat()
            ));
        }
        transport.setSpeed(po.getSpeed());
        transport.setDirection(po.getDirection());
        transport.setEstimatedArrival(po.getEstimatedArrival());
        transport.setCreatedAt(po.getCreatedAt());
        transport.setUpdatedAt(po.getUpdatedAt());
        return transport;
    }

    /**
     * 将Aggregate转换为PO
     */
    private TransportPO toPO(Transport transport) {
        if (transport == null) return null;
        TransportPO po = new TransportPO();
        po.setId(transport.getId().getValue());
        po.setWaybillNo(transport.getWaybillNo().getValue());
        po.setDispatchId(transport.getDispatchId());
        po.setStatus(transport.getStatus().getValue());
        if (transport.getCurrentLocation() != null) {
            TransportPO.LocationPO loc = new TransportPO.LocationPO();
            loc.setLng(transport.getCurrentLocation().getLng());
            loc.setLat(transport.getCurrentLocation().getLat());
            po.setCurrentLocation(loc);
        }
        po.setSpeed(transport.getSpeed());
        po.setDirection(transport.getDirection());
        po.setEstimatedArrival(transport.getEstimatedArrival());
        po.setCreatedAt(transport.getCreatedAt());
        po.setUpdatedAt(transport.getUpdatedAt());
        return po;
    }
}
