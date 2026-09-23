package com.vaultcore.vbms.security;
import com.vaultcore.vbms.entity.User;
import com.vaultcore.vbms.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles(user.getRole()).build();
    }
}
