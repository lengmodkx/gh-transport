package com.ghtransport.customer.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 客户聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Customer extends AggregateRoot<CustomerId> {

    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private CustomerType type;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Customer() { super(); }

    public static Customer create(String name, String contactPerson, String phone,
                                   String email, String address, CustomerType type) {
        Customer c = new Customer(CustomerId.generate());
        c.name = name;
        c.contactPerson = contactPerson;
        c.phone = phone;
        c.email = email;
        c.address = address;
        c.type = type;
        c.status = CustomerStatus.ACTIVE;
        c.createdAt = LocalDateTime.now();
        c.updatedAt = LocalDateTime.now();
        return c;
    }

    public void updateInfo(String name, String contactPerson, String phone, String email, String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void disable() { this.status = CustomerStatus.INACTIVE; this.updatedAt = LocalDateTime.now(); }
    public void enable() { this.status = CustomerStatus.ACTIVE; this.updatedAt = LocalDateTime.now(); }

    @Override public String getAggregateType() { return "Customer"; }

    @Data @ValueObject public static class CustomerId { private final String value; public static CustomerId of(String v){return new CustomerId(v);} public static CustomerId generate(){return new CustomerId(IdGenerator.generateUUID());} }
    @Getter @ValueObject public static class CustomerType {
        public static final CustomerType ENTERPRISE = new CustomerType("ENTERPRISE");
        public static final CustomerType INDIVIDUAL = new CustomerType("INDIVIDUAL");
        private final String value; private CustomerType(String v){this.value=v;}
        public static CustomerType of(String v){
            if (v == null) {
                throw new IllegalArgumentException("客户类型不能为空");
            }
            return v.equalsIgnoreCase("ENTERPRISE") ? ENTERPRISE : INDIVIDUAL;
        }
    }
    @Getter @ValueObject public static class CustomerStatus {
        public static final CustomerStatus ACTIVE = new CustomerStatus("ACTIVE");
        public static final CustomerStatus INACTIVE = new CustomerStatus("INACTIVE");
        private final String value; private CustomerStatus(String v){this.value=v;}
        public static CustomerStatus of(String v){
            if (v == null) {
                throw new IllegalArgumentException("客户状态不能为空");
            }
            return v.equalsIgnoreCase("ACTIVE") ? ACTIVE : INACTIVE;
        }
    }
}
