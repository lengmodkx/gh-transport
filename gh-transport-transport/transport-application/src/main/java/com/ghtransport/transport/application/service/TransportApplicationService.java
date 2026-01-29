package com.ghtransport.transport.application.service;

import com.ghtransport.transport.domain.aggregate.Transport;
import com.ghtransport.transport.domain.repository.TransportRepository;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 运输应用服务
 */
@Slf4j
@Service
public class TransportApplicationService {

    private final TransportRepository transportRepository;

    public TransportApplicationService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    /**
     * 创建运单
     */
    @Transactional(rollbackFor = Exception.class)
    public Transport createTransport(String dispatchId, String origin, String destination) {
        Transport transport = Transport.create(dispatchId, origin, destination);
        transportRepository.save(transport);
        log.info("运单创建成功: {}", transport.getWaybillNo().getValue());
        return transport;
    }

    /**
     * 获取运单详情
     */
    @Transactional(readOnly = true)
    public Transport getTransport(String transportId) {
        return transportRepository.findById(Transport.TransportId.of(transportId))
                .orElseThrow(() -> new BusinessException("TRANSPORT_NOT_FOUND", "运单不存在"));
    }

    /**
     * 更新位置
     */
    @Transactional(rollbackFor = Exception.class)
    public Transport updateLocation(String transportId, Double lng, Double lat) {
        Transport transport = getTransport(transportId);
        transport.updateLocation(lng, lat);
        transportRepository.save(transport);
        return transport;
    }

    /**
     * 提货
     */
    @Transactional(rollbackFor = Exception.class)
    public Transport pickup(String transportId) {
        Transport transport = getTransport(transportId);
        transport.setStatus(Transport.TransportStatus.PICKING_UP);
        transportRepository.save(transport);
        log.info("运单开始提货: {}", transportId);
        return transport;
    }

    /**
     * 开始配送
     */
    @Transactional(rollbackFor = Exception.class)
    public Transport startDelivery(String transportId) {
        Transport transport = getTransport(transportId);
        transport.setStatus(Transport.TransportStatus.DELIVERING);
        transportRepository.save(transport);
        log.info("运单开始配送: {}", transportId);
        return transport;
    }

    /**
     * 完成配送
     */
    @Transactional(rollbackFor = Exception.class)
    public Transport completeDelivery(String transportId) {
        Transport transport = getTransport(transportId);
        transport.setStatus(Transport.TransportStatus.DELIVERED);
        transportRepository.save(transport);
        log.info("运单完成配送: {}", transportId);
        return transport;
    }

    /**
     * 根据调度ID获取运单
     */
    @Transactional(readOnly = true)
    public Transport getByDispatchId(String dispatchId) {
        return transportRepository.findByDispatchId(dispatchId)
                .orElseThrow(() -> new BusinessException("TRANSPORT_NOT_FOUND", "运单不存在"));
    }

    /**
     * 获取运单列表（按状态）
     */
    @Transactional(readOnly = true)
    public PageResult<Transport> getByStatus(Transport.TransportStatus status, int pageNum, int pageSize) {
        return transportRepository.findByStatus(status, pageNum, pageSize);
    }
}
