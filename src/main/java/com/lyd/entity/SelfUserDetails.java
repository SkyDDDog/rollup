package com.lyd.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * @author 天狗
 * @desc SpringSecurity配置UserDetails实现类
 * @date 2022/7/16
 */
public class SelfUserDetails implements UserDetails, Serializable {


    private static final long serialVersionUID = 7171722954972237961L;

    private String id;
//    private String username;
    private String email;
    private String password;
    private Set<? extends GrantedAuthority> authorities;
    private String jwtToken;

    public String getEmail() {
        return email;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(Set<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String  getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
