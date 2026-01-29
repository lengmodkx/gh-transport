package com.ghtransport.auth.domain.aggregate;

import com.ghtransport.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户聚合根测试
 */
class UserTest {

    @Test
    @DisplayName("创建用户 - 成功")
    void createUser_Success() {
        // Given
        User.Username username = User.Username.of("testuser");
        User.Password password = User.Password.of("password123");
        User.Mobile mobile = User.Mobile.of("13812345678");
        User.Email email = User.Email.of("test@example.com");
        User.Nickname nickname = User.Nickname.of("测试用户");

        // When
        User user = User.create(username, password, mobile, email, nickname);

        // Then
        assertNotNull(user.getId());
        assertEquals("testuser", user.getUsername().getValue());
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("创建用户 - 用户名格式错误")
    void createUser_UsernameFormatError() {
        assertThrows(BusinessException.class, () ->
            User.Username.of("123user") // 不能以数字开头
        );
    }

    @Test
    @DisplayName("创建用户 - 用户名长度不足")
    void createUser_UsernameTooShort() {
        assertThrows(BusinessException.class, () ->
            User.Username.of("ab") // 少于4位
        );
    }

    @Test
    @DisplayName("创建用户 - 手机号格式错误")
    void createUser_MobileFormatError() {
        assertThrows(BusinessException.class, () ->
            User.Mobile.of("1381234567") // 11位但格式不对
        );
    }

    @Test
    @DisplayName("创建用户 - 手机号格式正确")
    void createUser_MobileValid() {
        User.Mobile mobile = User.Mobile.of("13812345678");
        assertEquals("13812345678", mobile.getValue());
    }

    @Test
    @DisplayName("创建用户 - 邮箱可选")
    void createUser_EmailOptional() {
        // Given
        User.Username username = User.Username.of("testuser");
        User.Password password = User.Password.of("password123");
        User.Mobile mobile = User.Mobile.of("13812345678");
        User.Nickname nickname = User.Nickname.of("测试用户");

        User user = User.create(username, password, mobile, null, nickname);

        assertNull(user.getEmail());
    }

    @Test
    @DisplayName("验证密码 - 成功")
    void verifyPassword_Success() {
        // Given
        User user = User.create(
            User.Username.of("testuser"),
            User.Password.of("password123"),
            User.Mobile.of("13812345678"),
            null,
            User.Nickname.of("测试用户")
        );

        // When & Then
        assertTrue(user.verifyPassword("password123"));
        assertFalse(user.verifyPassword("wrongpassword"));
    }

    @Test
    @DisplayName("更新密码 - 原密码错误")
    void updatePassword_WrongOriginal() {
        // Given
        User user = User.create(
            User.Username.of("testuser"),
            User.Password.of("password123"),
            User.Mobile.of("13812345678"),
            null,
            User.Nickname.of("测试用户")
        );

        // When & Then
        assertThrows(BusinessException.class, () ->
            user.updatePassword("wrongpassword", "newpassword123")
        );
    }

    @Test
    @DisplayName("禁用/启用用户")
    void disableAndEnableUser() {
        // Given
        User user = User.create(
            User.Username.of("testuser"),
            User.Password.of("password123"),
            User.Mobile.of("13812345678"),
            null,
            User.Nickname.of("测试用户")
        );

        // When
        user.disable();

        // Then
        assertEquals(User.UserStatus.DISABLED, user.getStatus());

        // When
        user.enable();

        // Then
        assertEquals(User.UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    @DisplayName("分配角色")
    void assignRoles() {
        // Given
        User user = User.create(
            User.Username.of("testuser"),
            User.Password.of("password123"),
            User.Mobile.of("13812345678"),
            null,
            User.Nickname.of("测试用户")
        );

        // When
        user.assignRoles(java.util.List.of(
            User.RoleId.of("role-001"),
            User.RoleId.of("role-002")
        ));

        // Then
        assertEquals(2, user.getRoles().size());
    }
}
