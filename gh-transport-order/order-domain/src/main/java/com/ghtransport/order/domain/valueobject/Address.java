package com.ghtransport.order.domain.vo;

import com.ghtransport.common.domain.ValueObject;
import com.ghtransport.common.util.Validate;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Address implements ValueObject {

    private final String province;
    private final String city;
    private final String district;
    private final String detail;
    private final String contactName;
    private final String contactPhone;

    public Address(String province, String city, String district,
                   String detail, String contactName, String contactPhone) {
        Validate.notBlank(province, "省份不能为空");
        Validate.notBlank(city, "城市不能为空");
        Validate.notBlank(detail, "详细地址不能为空");

        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
    }

    public String getFullAddress() {
        return String.format("%s%s%s%s", province, city, district, detail);
    }
}
