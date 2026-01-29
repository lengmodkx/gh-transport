package com.ghtransport.customer.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 客户聚合根测试
 */
class CustomerTest {

    @Test
    @DisplayName("创建客户 - 成功")
    void createCustomer_Success() {
        // Given & When
        Customer customer = Customer.create(
            "某某公司",
            "张三",
            "13812345678",
            "zhang@example.com",
            "北京市朝阳区xxx",
            Customer.CustomerType.ENTERPRISE
        );

        // Then
        assertNotNull(customer.getId());
        assertEquals("某某公司", customer.getName());
        assertEquals("张三", customer.getContactPerson());
        assertEquals(Customer.CustomerType.ENTERPRISE, customer.getType());
        assertEquals(Customer.CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("创建客户 - 个人客户")
    void createCustomer_Individual() {
        // Given & When
        Customer customer = Customer.create(
            "李四",
            "李四",
            "13887654321",
            null,
            "上海市浦东新区xxx",
            Customer.CustomerType.INDIVIDUAL
        );

        // Then
        assertEquals(Customer.CustomerType.INDIVIDUAL, customer.getType());
    }

    @Test
    @DisplayName("更新客户信息")
    void updateInfo() {
        // Given
        Customer customer = Customer.create(
            "某某公司",
            "张三",
            "13812345678",
            "old@example.com",
            "旧地址",
            Customer.CustomerType.ENTERPRISE
        );

        // When
        customer.updateInfo(
            "某某科技公司",
            "李经理",
            "13912345678",
            "new@example.com",
            "新地址"
        );

        // Then
        assertEquals("某某科技公司", customer.getName());
        assertEquals("李经理", customer.getContactPerson());
        assertEquals("13912345678", customer.getPhone());
        assertEquals("new@example.com", customer.getEmail());
        assertEquals("新地址", customer.getAddress());
    }

    @Test
    @DisplayName("禁用客户")
    void disableCustomer() {
        // Given
        Customer customer = Customer.create("公司", "联系人", "13800000000", null, "地址", Customer.CustomerType.ENTERPRISE);

        // When
        customer.disable();

        // Then
        assertEquals(Customer.CustomerStatus.INACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("启用客户")
    void enableCustomer() {
        // Given
        Customer customer = Customer.create("公司", "联系人", "13800000000", null, "地址", Customer.CustomerType.ENTERPRISE);
        customer.disable();

        // When
        customer.enable();

        // Then
        assertEquals(Customer.CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("客户类型转换")
    void customerTypeConversion() {
        assertEquals(Customer.CustomerType.ENTERPRISE, Customer.CustomerType.of("ENTERPRISE"));
        assertEquals(Customer.CustomerType.ENTERPRISE, Customer.CustomerType.of("enterprise"));
        assertEquals(Customer.CustomerType.INDIVIDUAL, Customer.CustomerType.of("INDIVIDUAL"));
    }

    @Test
    @DisplayName("客户状态转换")
    void customerStatusConversion() {
        assertEquals(Customer.CustomerStatus.ACTIVE, Customer.CustomerStatus.of("ACTIVE"));
        assertEquals(Customer.CustomerStatus.ACTIVE, Customer.CustomerStatus.of("active"));
        assertEquals(Customer.CustomerStatus.INACTIVE, Customer.CustomerStatus.of("INACTIVE"));
    }
}
