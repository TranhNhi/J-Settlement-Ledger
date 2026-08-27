package com.example.J_Settlement.Ledger.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Tuyệt đối ưu tiên Header từ Frontend gửi lên
        String bankHeader = request.getHeader("X-Bank-Name");
        String jwt = getJwtFromRequest(request);
        String bankToUse = null;

        if (StringUtils.hasText(bankHeader)) {
            bankToUse = bankHeader.toUpperCase();
        } else if (isValidJwt(jwt)) {
            // Chỉ khi không có Header mới nhìn vào Token
            bankToUse = tokenProvider.getBankFromJWT(jwt);
        }

        // 2. Nếu không có gì cả mới về VCB (hoặc Central tùy bạn)
        if (!StringUtils.hasText(bankToUse)) {
            bankToUse = "VCB";
        }

        try {
            // THIẾT LẬP NGAY: Phải set DB trước khi làm bất cứ việc gì khác
            DBContextHolder.setCurrentDb(bankToUse);
            logger.info(">>> [ROUTING] ĐANG ÉP VÀO DATABASE: {}", bankToUse);

            // 3. XÁC THỰC (Sau khi đã chọn đúng DB)
            if (isValidJwt(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromJWT(jwt);
                // Lúc này CustomUserDetailsService sẽ tìm đúng ở DB đã set bên trên
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            // CHẠY TIẾP
            filterChain.doFilter(request, response);

        } finally {
            DBContextHolder.clear();
            logger.info(">>> [ROUTING] ĐÃ DỌN DẸP THREAD (CLEARED)");
        }
    }

    // Hàm bổ trợ để kiểm tra Token có thực sự là JWT hay không (Tránh lỗi 2 dấu chấm)
    private boolean isValidJwt(String jwt) {
        return StringUtils.hasText(jwt)
                && !jwt.equals("undefined")
                && !jwt.equals("null")
                && jwt.split("\\.").length == 3;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}