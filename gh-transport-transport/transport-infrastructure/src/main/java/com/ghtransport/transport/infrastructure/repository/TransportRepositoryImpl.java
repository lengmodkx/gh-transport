package com.ghtransport.transport.infrastructure.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.transport.domain.aggregate.Transport;
import com.ghtransport.transport.domain.repository.TransportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 运输仓储实现
 */
@Slf4j
@Repository
public class TransportRepositoryImpl implements TransportRepository {

    // 实际项目中注入MongoTemplate
    private final List<Transport> transportStore = new ArrayList<>();

    @Override
    public Optional<Transport> findById(Transport.TransportId id) {
        return transportStore.stream()
                .filter(t -> t.getId().getValue().equals(id.getValue()))
                .findFirst();
    }

    @Override
    public Optional<Transport> findByWaybillNo(Transport.WaybillNo waybillNo) {
        return transportStore.stream()
                .filter(t -> t.getWaybillNo().getValue().equals(waybillNo.getValue()))
                .findFirst();
    }

    @Override
    public Optional<Transport> findByDispatchId(String dispatchId) {
        return transportStore.stream()
                .filter(t -> dispatchId.equals(t.getDispatchId()))
                .findFirst();
    }

    @Override
    public PageResult<Transport> findByStatus(Transport.TransportStatus status, int pageNum, int pageSize) {
        List<Transport> filtered = transportStore.stream()
                .filter(t -> t.getStatus() == status)
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Transport> findByVehicleId(String vehicleId, int pageNum, int pageSize) {
        // 实际使用MongoDB查询
        return PageResult.empty(pageNum, pageSize);
    }

    @Override
    public void save(Transport transport) {
        Optional<Transport> existing = findById(transport.getId());
        if (existing.isPresent()) {
            int index = transportStore.indexOf(existing.get());
            transportStore.set(index, transport);
        } else {
            transportStore.add(transport);
        }
        log.info("保存运单: {}", transport.getWaybillNo().getValue());
    }

    @Override
    public void delete(Transport.TransportId id) {
        transportStore.removeIf(t -> t.getId().getValue().equals(id.getValue()));
    }

    private PageResult<Transport> createPageResult(List<Transport> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Transport> pageList = start >= total ? List.of() : list.subList(start, end);
        return new PageResult<>(pageNum, pageSize, (long) total, pageList);
    }
}
