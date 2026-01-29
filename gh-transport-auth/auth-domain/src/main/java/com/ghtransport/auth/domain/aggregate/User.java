package com.ghtransport.auth.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 用户聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class User extends AggregateRoot<UserId> {

    /**
     * 用户名
     */
    private Username username;

    /**
     * 密码
     */
    private Password password;

    /**
     * 手机号
     */
    private Mobile mobile;

    /**
     * 邮箱
     */
    private Email email;

    /**
     * 昵称
     */
    private Nickname nickname;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 用户角色
     */
    private List<RoleId> roles;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    protected User() {
        super();
    }

    protected User(UserId id) {
        super(id);
    }

    /**
     * 创建用户
     */
    public static User create(Username username, Password password, Mobile mobile, Email email, Nickname nickname) {
        User user = new User(UserId.generate());
        user.username = username;
        user.password = password;
        user.mobile = mobile;
        user.email = email;
        user.nickname = nickname;
        user.status = UserStatus.ACTIVE;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword) {
        return password.verify(rawPassword);
    }

    /**
     * 更新密码
     */
    public void updatePassword(String rawPassword, String newPassword) {
        if (!password.verify(rawPassword)) {
            throw new BusinessException("原密码错误");
        }
        this.password = Password.of(newPassword);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新手机号
     */
    public void updateMobile(Mobile mobile) {
        this.mobile = mobile;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新邮箱
     */
    public void updateEmail(Email email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新昵称
     */
    public void updateNickname(Nickname nickname) {
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 分配角色
     */
    public void assignRoles(List<RoleId> roleIds) {
        this.roles = roleIds;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新最后登录时间
     */
    public void updateLastLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 禁用用户
     */
    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 启用用户
     */
    public void enable() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    /**
     * 用户ID
     */
    @Data
    @ValueObject
    public static class UserId {
        private final String value;

        public static UserId of(String value) {
            return new UserId(value);
        }

        public static UserId generate() {
            return new UserId(java.util.UUID.randomUUID().toString());
        }
    }

    /**
     * 用户名
     */
    @Data
    @ValueObject
    public static class Username {
        private final String value;

        public static Username of(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_IS_NULL, "用户名不能为空");
            }
            if (value.length() < 4 || value.length() > 32) {
                throw new BusinessException("USERNAME_INVALID", "用户名长度必须在4-32位之间");
            }
            if (!value.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
                throw new BusinessException("USERNAME_INVALID", "用户名必须以字母开头，只能包含字母、数字和下划线");
            }
            return new Username(value.trim().toLowerCase());
        }
    }

    /**
     * 密码
     */
    @Data
    @ValueObject
    public static class Password {
        private final String encodedPassword;

        private Password(String encodedPassword) {
            this.encodedPassword = encodedPassword;
        }

        public static Password of(String rawPassword) {
            if (rawPassword == null || rawPassword.length() < 6) {
                throw new BusinessException("PASSWORD_INVALID", "密码长度不能少于6位");
            }
            // 实际项目中应该使用BCrypt或其他加密算法
            String encoded = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            return new Password(encoded);
        }

        public boolean verify(String rawPassword) {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        }
    }

    /**
     * 手机号
     */
    @Data
    @ValueObject
    public static class Mobile {
        private final String value;

        public static Mobile of(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_IS_NULL, "手机号不能为空");
            }
            if (!value.matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException("MOBILE_INVALID", "手机号格式不正确");
            }
            return new Mobile(value);
        }
    }

    /**
     * 邮箱
     */
    @Data
    @ValueObject
    public static class Email {
        private final String value;

        public static Email of(String value) {
            if (value == null || value.trim().isEmpty()) {
                return null; // 邮箱可选
            }
            if (!value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                throw new BusinessException("EMAIL_INVALID", "邮箱格式不正确");
            }
            return new Email(value.trim().toLowerCase());
        }
    }

    /**
     * 昵称
     */
    @Data
    @ValueObject
    public static class Nickname {
        private final String value;

        public static Nickname of(String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_IS_NULL, "昵称不能为空");
            }
            if (value.length() < 2 || value.length() > 32) {
                throw new BusinessException("NICKNAME_INVALID", "昵称长度必须在2-32位之间");
            }
            return new Nickname(value.trim());
        }
    }

    /**
     * 用户状态
     */
    @Getter
    @ValueObject
    public static class UserStatus {
        public static final UserStatus ACTIVE = new UserStatus("ACTIVE");
        public static final UserStatus DISABLED = new UserStatus("DISABLED");
        public static final UserStatus LOCKED = new UserStatus("LOCKED");

        private final String value;

        private UserStatus(String value) {
            this.value = value;
        }

        public static UserStatus of(String value) {
            return switch (value.toUpperCase()) {
                case "ACTIVE" -> ACTIVE;
                case "DISABLED" -> DISABLED;
                case "LOCKED" -> LOCKED;
                default -> throw new BusinessException("USER_STATUS_INVALID", "无效的用户状态");
            };
        }
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
    }
}
