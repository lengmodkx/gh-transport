package com.ghtransport.customer.application.service;

import com.ghtransport.customer.domain.aggregate.Customer;
import com.ghtransport.customer.domain.repository.CustomerRepository;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户应用服务
 */
@Slf4j
@Service
public class CustomerApplicationService {

    private final CustomerRepository customerRepository;

    public CustomerApplicationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * 创建客户
     */
    @Transactional(rollbackFor = Exception.class)
    public Customer createCustomer(String name, String contactPerson, String phone,
                                 String email, String address, Customer.CustomerType type) {
        if (customerRepository.existsByPhone(phone)) {
            throw new BusinessException("PHONE_EXISTS", "手机号已存在");
        }
        Customer customer = Customer.create(name, contactPerson, phone, email, address, type);
        customerRepository.save(customer);
        log.info("客户创建成功: {}", customer.getName());
        return customer;
    }

    /**
     * 获取客户详情
     */
    @Transactional(readOnly = true)
    public Customer getCustomer(String customerId) {
        return customerRepository.findById(Customer.CustomerId.of(customerId))
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "客户不存在"));
    }

    /**
     * 更新客户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Customer updateCustomer(String customerId, String name, String contactPerson,
                                 String phone, String email, String address) {
        Customer customer = getCustomer(customerId);
        customer.updateInfo(name, contactPerson, phone, email, address);
        customerRepository.save(customer);
        log.info("客户信息更新: {}", customerId);
        return customer;
    }

    /**
     * 禁用客户
     */
    @Transactional(rollbackFor = Exception.class)
    public Customer disableCustomer(String customerId) {
        Customer customer = getCustomer(customerId);
        customer.disable();
        customerRepository.save(customer);
        return customer;
    }

    /**
     * 启用客户
     */
    @Transactional(rollbackFor = Exception.class)
    public Customer enableCustomer(String customerId) {
        Customer customer = getCustomer(customerId);
        customer.enable();
        customerRepository.save(customer);
        return customer;
    }

    /**
     * 搜索客户
     */
    @Transactional(readOnly = true)
    public PageResult<Customer> searchCustomers(String keyword, int pageNum, int pageSize) {
        return customerRepository.search(keyword, pageNum, pageSize);
    }

    /**
     * 获取客户列表（按类型）
     */
    @Transactional(readOnly = true)
    public PageResult<Customer> getCustomersByType(Customer.CustomerType type, int pageNum, int pageSize) {
        return customerRepository.findByType(type, pageNum, pageSize);
    }
}
