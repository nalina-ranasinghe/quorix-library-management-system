package com.library.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        boolean isAdmin = hasAuth(authentication, "ROLE_ADMIN");
        boolean isStaff = hasAuth(authentication, "ROLE_STAFF");
        boolean isEndUser = hasAuth(authentication, "ROLE_END_USER");

        if (isAdmin) {
            response.sendRedirect("/admin");
        } else if (isStaff) {
            response.sendRedirect("/staff");
        } else if (isEndUser) {
            response.sendRedirect("/home");
        } else {
            response.sendRedirect("/home");
        }
    }

    private boolean hasAuth(Authentication auth, String role) {
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a.getAuthority().equals(role)) return true;
        }
        return false;
    }
}