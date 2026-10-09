package com.projectmanagement.seller.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class LoginUser {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class UserContext {
        private Long userId;
        private String username;
        private String email;
    }

    private static final ThreadLocal<UserContext> CURRENT_USER = ThreadLocal.withInitial(UserContext::new);

    public Long getUserId() {
        return CURRENT_USER.get().getUserId();
    }

    public void setUserId(Long userId) {
        CURRENT_USER.get().setUserId(userId);
    }

    public String getUsername() {
        return CURRENT_USER.get().getUsername();
    }

    public void setUsername(String username) {
        CURRENT_USER.get().setUsername(username);
    }

    public String getName() {
        return getUsername();
    }

    public void setName(String name) {
        setUsername(name);
    }

    public String getEmail() {
        return CURRENT_USER.get().getEmail();
    }

    public void setEmail(String email) {
        CURRENT_USER.get().setEmail(email);
    }

    public void clear() {
        CURRENT_USER.remove();
    }
}
