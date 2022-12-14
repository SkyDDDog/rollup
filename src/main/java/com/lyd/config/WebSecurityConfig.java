package com.lyd.config;

import com.lyd.filter.JwtAuthenticationTokenFilter;
import com.lyd.handler.security.*;
import com.lyd.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author 天狗
 * @desc: 配置SpringSecurity
 * @date 2022/5/16
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //登录成功处理逻辑
    @Autowired
    CustomizeAuthenticationSuccessHandler authenticationSuccessHandler;

    //登录失败处理逻辑
    @Autowired
    CustomizeAuthenticationFailureHandler authenticationFailureHandler;

    //权限拒绝处理逻辑
    @Autowired
    CustomizeAccessDeniedHandler accessDeniedHandler;

    //匿名用户访问无权限资源时的异常
    @Autowired
    CustomizeAuthenticationEntryPoint authenticationEntryPoint;

    //会话失效(账号被挤下线)处理逻辑
    @Autowired
    CustomizeSessionInformationExpiredStrategy sessionInformationExpiredStrategy;

    //登出成功处理逻辑
    @Autowired
    CustomizeLogoutSuccessHandler logoutSuccessHandler;

    //访问决策管理器
    @Autowired
    CustomizeAccessDecisionManager accessDecisionManager;

    //实现权限拦截
    @Autowired
    CustomizeFilterInvocationSecurityMetadataSource securityMetadataSource;

    @Autowired
    private CustomizeAbstractSecurityInterceptor securityInterceptor;


//    @Autowired
//    CustomizeAuthenticationEntryPoint authenticationEntryPoint;//未登陆时返回 JSON 格式的数据给前端（否则为 html）
//
//    @Autowired
//    AuthenticationSuccessHandler authenticationSuccessHandler; //登录成功返回的 JSON 格式数据给前端（否则为 html）
//
//    @Autowired
//    AuthenticationFailureHandler authenticationFailureHandler; //登录失败返回的 JSON 格式数据给前端（否则为 html）
//
//    @Autowired
//    LogoutSuccessHandler logoutSuccessHandler;//注销成功返回的 JSON 格式数据给前端（否则为 登录时的 html）
//
//    @Autowired
//    AccessDeniedHandler accessDeniedHandler;//无权访问返回的 JSON 格式数据给前端（否则为 403 html 页面）

    @Autowired
    UserDetailsServiceImpl userDetailsService; // 自定义user

    @Autowired
    JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**");
    }


//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //配置认证方式等
//        super.configure(auth);
//    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.formLogin()
//                .loginProcessingUrl("/login");
//        //http相关的配置，包括登入登出、异常处理、会话管理等
////        super.configure(http);
//        //放行swagger相关接口
//        http
//                .authorizeRequests()
//                .antMatchers("/swagger-ui.html").permitAll()
//                .antMatchers("/webjars/**").permitAll()
//                .antMatchers("/swagger-resources/**").permitAll()
//                .antMatchers("/v2/*").permitAll()
//                .antMatchers("/csrf").permitAll()
//                .antMatchers("/").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .formLogin();
//        //放行注册接口
//        http.cors().and().csrf().disable();
//        http.authorizeRequests().
//                //antMatchers("/getUser").hasAuthority("query_user").
//                //antMatchers("/**").fullyAuthenticated().
//                        withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
//                    @Override
//                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
////                        AccessDecisionManager accessDecisionManager;
//                        o.setAccessDecisionManager(accessDecisionManager);//决策管理器
//                        o.setSecurityMetadataSource(securityMetadataSource);//安全元数据源
//                        return o;
//                    }
//                }).
//                //登出
//                        and().logout().
//                permitAll().//允许所有用户
//                logoutSuccessHandler(logoutSuccessHandler).//登出成功处理逻辑
//                deleteCookies("JSESSIONID").//登出之后删除cookie
//                //登入
//                        and().formLogin().
//                permitAll().//允许所有用户
//                successHandler(authenticationSuccessHandler).//登录成功处理逻辑
//                failureHandler(authenticationFailureHandler).//登录失败处理逻辑
//                //异常处理(权限拒绝、登录失效等)
//                        and().exceptionHandling().
//                accessDeniedHandler(accessDeniedHandler).//权限拒绝处理逻辑
//                authenticationEntryPoint(authenticationEntryPoint).//匿名用户访问无权限资源时的异常处理
//                //会话管理
//                        and().sessionManagement().
//                maximumSessions(1).//同一账号同时登录最大用户数
//                expiredSessionStrategy(sessionInformationExpiredStrategy);//会话失效(账号被挤下线)处理逻辑
//        http.addFilterBefore(securityInterceptor, FilterSecurityInterceptor.class);
//    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 加入自定义的安全认证
//        auth.authenticationProvider(provider);
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 去掉 CSRF
        http.cors(Customizer.withDefaults())
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用 JWT，关闭token
                .and()

                .httpBasic().authenticationEntryPoint(authenticationEntryPoint)

                .and()
                .authorizeRequests()//定义哪些URL需要被保护、哪些不需要被保护
                // swagger ui
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-ui").permitAll()
                .antMatchers("/swagger-ui/*").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/*").permitAll()
                .antMatchers("/v3/*").permitAll()

                .antMatchers("/csrf").permitAll()
                .antMatchers("/").permitAll()
                .antMatchers("/test/**").permitAll()

                .antMatchers("/posts/**").permitAll()
                .antMatchers("/answer/**").permitAll()
                .antMatchers("/doc/**").permitAll()
                .antMatchers("/mail/**").permitAll()
                .antMatchers("/video/**").permitAll()
                .antMatchers("/user/**").permitAll()
                .antMatchers("/ws/**").permitAll()

//                .anyRequest().authenticated()
                .anyRequest()//任何请求,登录后可以访问
                .access("@rbacauthorityservice.hasPermission(request,authentication)") // RBAC 动态 url 认证

                .and()
                .formLogin()  //开启登录, 定义当需要用户登录时候，转到的登录页面
//                .loginPage("/test/login.html")
                .loginProcessingUrl("/user/login")
//                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler) // 登录成功
                .failureHandler(authenticationFailureHandler) // 登录失败
                .permitAll()

                .and()
                .logout()//默认注销行为为logout
                .logoutUrl("/user/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .permitAll();

                //放行swagger相关接口
//            http.authorizeRequests()
//                .antMatchers("/swagger-ui.html").permitAll()
//                .antMatchers("/webjars/**").permitAll()
//                .antMatchers("/swagger-resources/**").permitAll()
//                .antMatchers("/v2/*").permitAll()
//                .antMatchers("/csrf").permitAll()
//                .antMatchers("/").permitAll()
//                .anyRequest().authenticated();

        // 记住我
        http.rememberMe().rememberMeParameter("remember-me")
                .userDetailsService(userDetailsService).tokenValiditySeconds(1000);

        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler); // 无权访问 JSON 格式的数据
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class); // JWT Filter

    }

    @Override
    @Bean
    public UserDetailsService userDetailsService() {
        //获取用户账号密码及权限信息
        return new UserDetailsServiceImpl();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

