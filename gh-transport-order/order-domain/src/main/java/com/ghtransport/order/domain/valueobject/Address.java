package com.ghtransport.order.domain.valueobject;

import com.ghtransport.common.core.ddd.ValueObject;
import lombok.Getter;

/**
 * 地址值对象（不可变）
 */
@Getter
@ValueObject
public class Address {

    /**
     * 省份
     */
    private final String province;

    /**
     * 城市
     */
    private final String city;

    /**
     * 区县
     */
    private final String district;

    /**
     * 详细地址
     */
    private final String detail;

    /**
     * 收货人姓名
     */
    private final String receiverName;

    /**
     * 收货人电话
     */
    private final String receiverPhone;

    /**
     * 邮编
     */
    private final String postalCode;

    public Address(String province, String city, String district, String detail,
                   String receiverName, String receiverPhone, String postalCode) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.postalCode = postalCode;
    }

    public static Address of(String province, String city, String district, String detail,
                             String receiverName, String receiverPhone, String postalCode) {
        return new Address(province, city, district, detail, receiverName, receiverPhone, postalCode);
    }

    /**
     * 获取完整地址字符串
     */
    public String getFullAddress() {
        return province + city + (district != null ? district : "") + detail;
    }
}
