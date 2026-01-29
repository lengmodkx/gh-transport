package com.ghtransport.customer.domain.repository;

import com.ghtransport.customer.domain.aggregate.Customer;
import com.ghtransport.common.core.result.PageResult;

import java.util.Optional;

/**
 * 客户仓储接口
 */
public interface CustomerRepository {

    Optional<Customer> findById(Customer.CustomerId id);

    Optional<Customer> findByName(String name);

    Optional<Customer> findByPhone(String phone);

    PageResult<Customer> findByType(Customer.CustomerType type, int pageNum, int pageSize);

    PageResult<Customer> findByStatus(Customer.CustomerStatus status, int pageNum, int pageSize);

    PageResult<Customer> search(String keyword, int pageNum, int pageSize);

    void save(Customer customer);

    void delete(Customer.CustomerId id);

    boolean existsByName(String name);

    boolean existsByPhone(String phone);
}
