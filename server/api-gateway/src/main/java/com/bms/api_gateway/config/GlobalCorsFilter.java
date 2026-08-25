package com.bms.api_gateway.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalCorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");
        if (origin != null && (origin.equals("http://localhost:4200") || origin.startsWith("http://localhost:"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // Handle preflight OPTIONS request directly at Gateway
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Wrap response to prevent downstream services (e.g. LIBRARY-SERVICE) from adding duplicate CORS headers
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if (isCorsHeader(name)) {
                    return;
                }
                super.setHeader(name, value);
            }

            @Override
            public void addHeader(String name, String value) {
                if (isCorsHeader(name)) {
                    return;
                }
                super.addHeader(name, value);
            }

            private boolean isCorsHeader(String name) {
                return name != null && (
                        name.equalsIgnoreCase("Access-Control-Allow-Origin") ||
                        name.equalsIgnoreCase("Access-Control-Allow-Credentials") ||
                        name.equalsIgnoreCase("Access-Control-Allow-Methods") ||
                        name.equalsIgnoreCase("Access-Control-Allow-Headers") ||
                        name.equalsIgnoreCase("Access-Control-Max-Age") ||
                        name.equalsIgnoreCase("Access-Control-Expose-Headers")
                );
            }
        };

        chain.doFilter(request, responseWrapper);
    }
}
