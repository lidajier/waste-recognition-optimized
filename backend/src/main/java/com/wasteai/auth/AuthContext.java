package com.wasteai.auth;

import com.wasteai.domain.UserEntity;

public final class AuthContext {

    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
