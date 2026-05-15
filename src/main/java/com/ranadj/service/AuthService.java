package com.ranadj.service;

import com.ranadj.dto.AuthResponse;
import com.ranadj.dto.LoginRequest;
import com.ranadj.dto.RegisterRequest;
import com.ranadj.dto.UserResponse;

/**
 * Service interface outlining authentication and registration operations.
 */
public interface AuthService {

    /**
     * Registers a new user into the database.
     *
     * @param request register payload
     * @return UserResponse containing safe user details
     */
    UserResponse register(RegisterRequest request);

    /**
     * Logs in a user and returns their secure JWT along with profile details.
     *
     * @param request login payload
     * @return AuthResponse containing token and user profile
     */
    AuthResponse login(LoginRequest request);
}
