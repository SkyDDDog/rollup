package com.lyd.handler.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;

/**
 * @author 222100209_李炎东
 * @date 2022/7/17
 */

@Component
public class CustomizeFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    AntPathMatcher antPathMatcher = new AntPathMatcher();
//    @Autowired
//    SysPermissionService sysPermissionService;
//    @Override
//    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
//        //获取请求地址
//        String requestUrl = ((FilterInvocation) o).getRequestUrl();
//
//        //查询具体某个接口的权限
//        List<SysPermission> permissionList =  sysPermissionService.selectListByPath(requestUrl);
//        if(permissionList == null || permissionList.size() == 0){
//            //请求路径没有配置权限，表明该请求接口可以任意访问
//            return null;
//        }
//        String[] attributes = new String[permissionList.size()];
//        for(int i = 0;i<permissionList.size();i++){
//            attributes[i] = permissionList.get(i).getPermissionCode();
//        }
//        return SecurityConfig.createList(attributes);
//    }


    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}