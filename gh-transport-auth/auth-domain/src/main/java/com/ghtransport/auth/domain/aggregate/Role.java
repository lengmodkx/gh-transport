package com.ghtransport.auth.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Role extends AggregateRoot<RoleId> {

    /**
     * 角色编码
     */
    private RoleCode code;

    /**
     * 角色名称
     */
    private RoleName name;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 角色状态
     */
    private RoleStatus status;

    /**
     * 是否系统角色
     */
    private boolean systemRole;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    protected Role() {
        super();
    }

    protected Role(RoleId id) {
        super(id);
    }

    /**
     * 创建角色
     */
    public static Role create(RoleCode code, RoleName name, String description, List<String> permissions) {
        Role role = new Role(RoleId.generate());
        role.code = code;
        role.name = name;
        role.description = description;
        role.permissions = permissions;
        role.status = RoleStatus.ENABLED;
        role.systemRole = false;
        role.createdAt = LocalDateTime.now();
        role.updatedAt = LocalDateTime.now();
        return role;
    }

    /**
     * 更新角色信息
     */
    public void update(RoleName name, String description, List<String> permissions) {
        this.name = name;
        this.description = description;
        this.permissions = permissions;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 启用角色
     */
    public void enable() {
        this.status = RoleStatus.ENABLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 禁用角色
     */
    public void disable() {
        this.status = RoleStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getAggregateType() {
        return "Role";
    }

    /**
     * 角色ID
     */
    @Data
    @ValueObject
    public static class RoleId {
        private final String value;

        public static RoleId of(String value) {
            return new RoleId(value);
        }

        public static RoleId generate() {
            return new RoleId(java.util.UUID.randomUUID().toString());
        }
    }

    /**
     * 角色编码
     */
    @Data
    @ValueObject
    public static class RoleCode {
        private final String value;

        public static RoleCode of(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("角色编码不能为空");
            }
            if (!value.matches("^[A-Z][A-Z0-9_]*$")) {
                throw new IllegalArgumentException("角色编码必须以大写字母开头，只能包含大写字母、数字和下划线");
            }
            return new RoleCode(value.trim().toUpperCase());
        }
    }

    /**
     * 角色名称
     */
    @Data
    @ValueObject
    public static class RoleName {
        private final String value;

        public static RoleName of(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("角色名称不能为空");
            }
            if (value.length() < 2 || value.length() > 32) {
                throw new IllegalArgumentException("角色名称长度必须在2-32位之间");
            }
            return new RoleName(value.trim());
        }
    }

    /**
     * 角色状态
     */
    @Getter
    @ValueObject
    public static class RoleStatus {
        public static final RoleStatus ENABLED = new RoleStatus("ENABLED");
        public static final RoleStatus DISABLED = new RoleStatus("DISABLED");

        private final String value;

        private RoleStatus(String value) {
            this.value = value;
        }

        public static RoleStatus of(String value) {
            return switch (value.toUpperCase()) {
                case "ENABLED" -> ENABLED;
                case "DISABLED" -> DISABLED;
                default -> throw new IllegalArgumentException("无效的角色状态");
            };
        }
    }
}
