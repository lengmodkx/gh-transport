package com.ghtransport.common.core.ddd;

import lombok.Getter;

import java.util.Objects;

/**
 * 实体基类
 *
 * @param <ID> ID类型
 */
@Getter
public abstract class Entity<ID> {

    /**
     * 实体ID（不可变）
     */
    protected final ID id;

    protected Entity() {
        this.id = null;
    }

    protected Entity(ID id) {
        this.id = id;
    }

    /**
     * 判断是否为新建实体（无ID或ID为空）
     */
    public boolean isNew() {
        return id == null;
    }

    /**
     * 判断是否为已存在实体（有ID）
     */
    public boolean isExisting() {
        return !isNew();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(), id);
    }
}
