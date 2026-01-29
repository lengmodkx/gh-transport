package com.ghtransport.dispatch.domain.repository;

import com.ghtransport.dispatch.domain.aggregate.Dispatch;
import com.ghtransport.common.core.result.PageResult;

/**
 * 调度单仓储接口
 */
public interface DispatchRepository {

    /**
     * 根据ID查询
     */
    java.util.Optional<Dispatch> findById(Dispatch.DispatchId id);

    /**
     * 根据调度单号查询
     */
    java.util.Optional<Dispatch> findByDispatchNo(Dispatch.DispatchNo dispatchNo);

    /**
     * 根据状态查询
     */
    PageResult<Dispatch> findByStatus(Dispatch.DispatchStatus status, int pageNum, int pageSize);

    /**
     * 根据司机ID查询
     */
    PageResult<Dispatch> findByDriverId(String driverId, int pageNum, int pageSize);

    /**
     * 根据车辆ID查询
     */
    PageResult<Dispatch> findByVehicleId(String vehicleId, int pageNum, int pageSize);

    /**
     * 保存
     */
    void save(Dispatch dispatch);

    /**
     * 删除
     */
    void delete(Dispatch.DispatchId id);
}
