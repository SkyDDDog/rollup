package com.lyd.service;

import com.lyd.entity.SelfUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author 天狗
 * @desc UserDetailsService实现类(Security)
 * @date 2022/7/18
 */
@Component
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || "".equals(username)) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        com.lyd.entity.User u = userService.getUserByEmail(username);
//        TestUser u = testService.getUser(username);
        if (u==null) {
            throw new UsernameNotFoundException("不存在该用户");
        }
        SelfUserDetails user = new SelfUserDetails();
        user.setEmail(u.getEmail());
        user.setId(u.getId().toString());
        user.setPassword(u.getPassword());


        //根据用户名查询用户权限
//        User user =
//        String perm = user.getPerm();
//        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Set authoritiesSet = new HashSet();
        // 模拟从数据库中获取用户角色
//        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        GrantedAuthority authority = new SimpleGrantedAuthority(u.getRole());
        authoritiesSet.add(authority);
        user.setAuthorities(authoritiesSet);
        log.info(user.toString());

        log.info("用户{}验证通过",username);
        return user;
//        声明用户授权
//        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(perm);
//        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("user");
//        grantedAuthorities.add(grantedAuthority);
//        return new User(user.getUsername(), user.getPassword(),
//                true, true, true, true, grantedAuthorities);
//        return new User("test","123456",grantedAuthorities);
    }


}

