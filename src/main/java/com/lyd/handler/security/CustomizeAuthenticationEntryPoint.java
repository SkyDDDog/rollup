package com.lyd.handler.security;

import com.alibaba.fastjson.JSON;
import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.common.ResultCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 222100209_李炎东
 * @desc 认证入口
 * @date 2022/7/17
 */

@Component
public class CustomizeAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        Result result = Result.error(Constants.CODE_401, ResultCode.USER_NOT_LOGIN);
        httpServletResponse.setContentType("text/json;charset=utf-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(result));

    }
}
