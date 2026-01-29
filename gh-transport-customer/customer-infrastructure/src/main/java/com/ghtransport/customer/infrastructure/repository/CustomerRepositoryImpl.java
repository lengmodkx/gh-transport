package com.ghtransport.customer.infrastructure.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.customer.domain.aggregate.Customer;
import com.ghtransport.customer.domain.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 客户仓储实现
 */
@Slf4j
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final List<Customer> customerStore = new ArrayList<>();

    @Override
    public Optional<Customer> findById(Customer.CustomerId id) {
        return customerStore.stream()
                .filter(c -> c.getId().getValue().equals(id.getValue()))
                .findFirst();
    }

    @Override
    public Optional<Customer> findByName(String name) {
        return customerStore.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return customerStore.stream()
                .filter(c -> phone.equals(c.getPhone()))
                .findFirst();
    }

    @Override
    public PageResult<Customer> findByType(Customer.CustomerType type, int pageNum, int pageSize) {
        List<Customer> filtered = customerStore.stream()
                .filter(c -> c.getType() == type)
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Customer> findByStatus(Customer.CustomerStatus status, int pageNum, int pageSize) {
        List<Customer> filtered = customerStore.stream()
                .filter(c -> c.getStatus() == status)
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Customer> search(String keyword, int pageNum, int pageSize) {
        List<Customer> filtered = customerStore.stream()
                .filter(c -> c.getName().contains(keyword) || c.getContactPerson().contains(keyword))
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public void save(Customer customer) {
        Optional<Customer> existing = findById(customer.getId());
        if (existing.isPresent()) {
            int index = customerStore.indexOf(existing.get());
            customerStore.set(index, customer);
        } else {
            customerStore.add(customer);
        }
        log.info("保存客户: {}", customer.getName());
    }

    @Override
    public void delete(Customer.CustomerId id) {
        customerStore.removeIf(c -> c.getId().getValue().equals(id.getValue()));
    }

    @Override
    public boolean existsByName(String name) {
        return customerStore.stream().anyMatch(c -> c.getName().equals(name));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerStore.stream().anyMatch(c -> phone.equals(c.getPhone()));
    }

    private PageResult<Customer> createPageResult(List<Customer> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Customer> pageList = start >= total ? List.of() : list.subList(start, end);
        return new PageResult<>(pageNum, pageSize, (long) total, pageList);
    }
}
