package com.vaultcore.vbms.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final CustomUserDetailsService details;
    public JwtAuthenticationFilter(JwtService jwt, CustomUserDetailsService details) { this.jwt=jwt; this.details=details; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) { String token=header.substring(7); if(jwt.valid(token)) { String email=jwt.email(token); UserDetails user=details.loadUserByUsername(email); var auth=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities()); auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); SecurityContextHolder.getContext().setAuthentication(auth); } }
        chain.doFilter(request,response);
    }
}
