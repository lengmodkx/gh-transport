package com.ghtransport.dispatch.application.service;

import com.ghtransport.dispatch.domain.aggregate.Dispatch;
import com.ghtransport.dispatch.domain.repository.DispatchRepository;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 调度应用服务
 */
@Slf4j
@Service
public class DispatchApplicationService {

    private final DispatchRepository dispatchRepository;

    public DispatchApplicationService(DispatchRepository dispatchRepository) {
        this.dispatchRepository = dispatchRepository;
    }

    /**
     * 创建调度单
     */
    @Transactional(rollbackFor = Exception.class)
    public Dispatch createDispatch(String orderId, String vehicleId, String driverId,
                                  LocalDateTime plannedTime, String origin, String destination) {
        Dispatch dispatch = Dispatch.create(orderId, vehicleId, driverId, plannedTime, origin, destination);
        dispatchRepository.save(dispatch);
        log.info("调度单创建成功: {}", dispatch.getDispatchNo().getValue());
        return dispatch;
    }

    /**
     * 获取调度单详情
     */
    @Transactional(readOnly = true)
    public Dispatch getDispatch(String dispatchId) {
        return dispatchRepository.findById(Dispatch.DispatchId.of(dispatchId))
                .orElseThrow(() -> new BusinessException("DISPATCH_NOT_FOUND", "调度单不存在"));
    }

    /**
     * 分配调度
     */
    @Transactional(rollbackFor = Exception.class)
    public Dispatch assignDispatch(String dispatchId, String vehicleId, String driverId) {
        Dispatch dispatch = getDispatch(dispatchId);
        dispatch.assign();
        dispatchRepository.save(dispatch);
        log.info("调度单分配成功: {}", dispatch.getDispatchNo().getValue());
        return dispatch;
    }

    /**
     * 开始运输
     */
    @Transactional(rollbackFor = Exception.class)
    public Dispatch startTransport(String dispatchId) {
        Dispatch dispatch = getDispatch(dispatchId);
        dispatch.start();
        dispatchRepository.save(dispatch);
        return dispatch;
    }

    /**
     * 完成调度
     */
    @Transactional(rollbackFor = Exception.class)
    public Dispatch completeDispatch(String dispatchId) {
        Dispatch dispatch = getDispatch(dispatchId);
        dispatch.complete();
        dispatchRepository.save(dispatch);
        log.info("调度单完成: {}", dispatch.getDispatchNo().getValue());
        return dispatch;
    }

    /**
     * 获取司机调度列表
     */
    @Transactional(readOnly = true)
    public PageResult<Dispatch> getDriverDispatches(String driverId, int pageNum, int pageSize) {
        return dispatchRepository.findByDriverId(driverId, pageNum, pageSize);
    }

    /**
     * 获取车辆调度列表
     */
    @Transactional(readOnly = true)
    public PageResult<Dispatch> getVehicleDispatches(String vehicleId, int pageNum, int pageSize) {
        return dispatchRepository.findByVehicleId(vehicleId, pageNum, pageSize);
    }
}
