package com.ghtransport.order.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghtransport.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<OrderPO> {
}
