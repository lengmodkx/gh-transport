package com.ghtransport.common.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * 实体基类
 */
@Getter
@Setter
public abstract class Entity<ID> {

    protected ID id;

    protected abstract ID generateId();

    public boolean isNew() {
        return id == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return id != null && id.equals(entity.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
