package com.ghtransport.dispatch.infrastructure.persistence.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.dispatch.domain.aggregate.Dispatch;
import com.ghtransport.dispatch.domain.repository.DispatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 调度单仓储实现
 */
@Slf4j
@Repository
public class DispatchRepositoryImpl implements DispatchRepository {

    // 实际项目中应该注入Mapper

    private final List<Dispatch> dispatchStore = new ArrayList<>();

    @Override
    public Optional<Dispatch> findById(Dispatch.DispatchId id) {
        return dispatchStore.stream()
                .filter(d -> d.getId().getValue().equals(id.getValue()))
                .findFirst();
    }

    @Override
    public Optional<Dispatch> findByDispatchNo(Dispatch.DispatchNo dispatchNo) {
        return dispatchStore.stream()
                .filter(d -> d.getDispatchNo().getValue().equals(dispatchNo.getValue()))
                .findFirst();
    }

    @Override
    public PageResult<Dispatch> findByStatus(Dispatch.DispatchStatus status, int pageNum, int pageSize) {
        List<Dispatch> filtered = dispatchStore.stream()
                .filter(d -> d.getStatus() == status)
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Dispatch> findByDriverId(String driverId, int pageNum, int pageSize) {
        List<Dispatch> filtered = dispatchStore.stream()
                .filter(d -> driverId.equals(d.getDriverId()))
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Dispatch> findByVehicleId(String vehicleId, int pageNum, int pageSize) {
        List<Dispatch> filtered = dispatchStore.stream()
                .filter(d -> vehicleId.equals(d.getVehicleId()))
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public void save(Dispatch dispatch) {
        Optional<Dispatch> existing = findById(dispatch.getId());
        if (existing.isPresent()) {
            int index = dispatchStore.indexOf(existing.get());
            dispatchStore.set(index, dispatch);
        } else {
            dispatchStore.add(dispatch);
        }
        log.info("保存调度单: {}", dispatch.getDispatchNo().getValue());
    }

    @Override
    public void delete(Dispatch.DispatchId id) {
        dispatchStore.removeIf(d -> d.getId().getValue().equals(id.getValue()));
    }

    private PageResult<Dispatch> createPageResult(List<Dispatch> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Dispatch> pageList = start >= total ? List.of() : list.subList(start, end);
        return new PageResult<>(pageNum, pageSize, (long) total, pageList);
    }
}
