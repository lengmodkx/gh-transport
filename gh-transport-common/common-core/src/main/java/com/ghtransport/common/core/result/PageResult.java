package com.ghtransport.common.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 *
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 数据列表
     */
    private List<T> list;

    public PageResult() {
        this.list = Collections.emptyList();
    }

    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list != null ? list : Collections.emptyList();
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, Collections.emptyList());
    }

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return total == null || total == 0;
    }

    /**
     * 判断是否有数据
     */
    public boolean hasData() {
        return total != null && total > 0;
    }
}
