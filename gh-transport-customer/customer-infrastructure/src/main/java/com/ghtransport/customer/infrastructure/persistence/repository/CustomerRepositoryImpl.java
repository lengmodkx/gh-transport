package com.ghtransport.customer.infrastructure.persistence.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.customer.domain.aggregate.Customer;
import com.ghtransport.customer.domain.repository.CustomerRepository;
import com.ghtransport.customer.infrastructure.persistence.mapper.CustomerMapper;
import com.ghtransport.customer.infrastructure.persistence.po.CustomerPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户仓储实现
 */
@Slf4j
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;

    public CustomerRepositoryImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    public Optional<Customer> findById(Customer.CustomerId id) {
        return customerMapper.findById(id.getValue())
                .map(customerMapper::toAggregate);
    }

    @Override
    public Optional<Customer> findByName(String name) {
        return customerMapper.findByName(name)
                .map(customerMapper::toAggregate);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return customerMapper.findByPhone(phone)
                .map(customerMapper::toAggregate);
    }

    @Override
    public PageResult<Customer> findByType(Customer.CustomerType type, int pageNum, int pageSize) {
        var page = customerMapper.findByType(type.getValue(), pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(customerMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public PageResult<Customer> findByStatus(Customer.CustomerStatus status, int pageNum, int pageSize) {
        var page = customerMapper.findByStatus(status.getValue(), pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(customerMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public PageResult<Customer> search(String keyword, int pageNum, int pageSize) {
        var page = customerMapper.search(keyword, pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(customerMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public void save(Customer customer) {
        CustomerPO po = customerMapper.toPO(customer);
        boolean exists = customerMapper.findById(po.getId()).isPresent();
        if (exists) {
            customerMapper.update(po);
            log.debug("客户更新成功: {}", customer.getId().getValue());
        } else {
            customerMapper.insert(po);
            log.debug("客户保存成功: {}", customer.getId().getValue());
        }
    }

    @Override
    public void delete(Customer.CustomerId id) {
        customerMapper.delete(id.getValue());
        log.debug("客户删除成功: {}", id.getValue());
    }

    @Override
    public boolean existsByName(String name) {
        return customerMapper.existsByName(name);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerMapper.existsByPhone(phone);
    }
}
