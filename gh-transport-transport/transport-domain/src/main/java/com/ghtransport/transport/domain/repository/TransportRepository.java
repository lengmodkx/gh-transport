package com.ghtransport.transport.domain.repository;

import com.ghtransport.transport.domain.aggregate.Transport;
import com.ghtransport.common.core.result.PageResult;

import java.util.Optional;

/**
 * 运输仓储接口
 */
public interface TransportRepository {

    Optional<Transport> findById(Transport.TransportId id);

    Optional<Transport> findByWaybillNo(Transport.WaybillNo waybillNo);

    Optional<Transport> findByDispatchId(String dispatchId);

    PageResult<Transport> findByStatus(Transport.TransportStatus status, int pageNum, int pageSize);

    PageResult<Transport> findByVehicleId(String vehicleId, int pageNum, int pageSize);

    void save(Transport transport);

    void delete(Transport.TransportId id);
}
