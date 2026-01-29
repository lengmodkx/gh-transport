package com.ghtransport.customer.infrastructure.persistence.mapper;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.customer.domain.aggregate.Customer;
import com.ghtransport.customer.domain.aggregate.Customer.CustomerId;
import com.ghtransport.customer.domain.aggregate.Customer.CustomerStatus;
import com.ghtransport.customer.domain.aggregate.Customer.CustomerType;
import com.ghtransport.customer.infrastructure.persistence.po.CustomerPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 客户MyBatis Mapper
 */
@Mapper
public interface CustomerMapper {

    void insert(@Param("po") CustomerPO po);

    void update(@Param("po") CustomerPO po);

    void delete(@Param("id") String id);

    Optional<CustomerPO> findById(@Param("id") String id);

    Optional<CustomerPO> findByName(@Param("name") String name);

    Optional<CustomerPO> findByPhone(@Param("phone") String phone);

    PageResult<CustomerPO> findByType(@Param("type") String type, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    PageResult<CustomerPO> findByStatus(@Param("status") String status, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    PageResult<CustomerPO> search(@Param("keyword") String keyword, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    boolean existsByName(@Param("name") String name);

    boolean existsByPhone(@Param("phone") String phone);

    /**
     * 将PO转换为Aggregate
     */
    default Customer toAggregate(CustomerPO po) {
        if (po == null) return null;
        Customer customer = new Customer();
        customer.setId(CustomerId.of(po.getId()));
        customer.setName(po.getName());
        customer.setContactPerson(po.getContactPerson());
        customer.setPhone(po.getPhone());
        customer.setEmail(po.getEmail());
        customer.setAddress(po.getAddress());
        customer.setType(CustomerType.of(po.getType()));
        customer.setStatus(CustomerStatus.of(po.getStatus()));
        customer.setCreatedAt(po.getCreatedAt());
        customer.setUpdatedAt(po.getUpdatedAt());
        return customer;
    }

    /**
     * 将Aggregate转换为PO
     */
    default CustomerPO toPO(Customer customer) {
        if (customer == null) return null;
        CustomerPO po = new CustomerPO();
        po.setId(customer.getId().getValue());
        po.setName(customer.getName());
        po.setContactPerson(customer.getContactPerson());
        po.setPhone(customer.getPhone());
        po.setEmail(customer.getEmail());
        po.setAddress(customer.getAddress());
        po.setType(customer.getType().getValue());
        po.setStatus(customer.getStatus().getValue());
        po.setCreatedAt(customer.getCreatedAt());
        po.setUpdatedAt(customer.getUpdatedAt());
        return po;
    }
}
