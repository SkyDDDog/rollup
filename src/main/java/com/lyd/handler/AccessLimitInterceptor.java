package com.lyd.handler;


import com.lyd.utils.AccessLimitAnno;
import com.lyd.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;


/**
 * @author 天狗
 * @desc 接口防止多次访问拦截器
 * @date 2022/7/18
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断请求是否属于方法的请求
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod=(HandlerMethod) handler;

            //获取方法中的注解，看是否有该注解
            AccessLimitAnno accessLimit = handlerMethod.getMethodAnnotation(AccessLimitAnno.class);
            if(accessLimit==null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxcount = accessLimit.maxcount();
            boolean login = accessLimit.needLogin();
            String key = request.getRequestURI();
            //如果需要登录
            if(login){
                //获取登录的session进行判断
                key+=""+"1"; //用户id userId
            }

            //从redis中获取用户访问的次数
//            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count= (Integer)redisTemplate.opsForValue().get(key);
            if(count==null){
                //第一次访问
                redisUtils.set(key,1+"");
//                redisTemplate.opsForValue().set(key,1);
            }else if(count<maxcount){
                //加1
                redisUtils.set(key,((Integer)redisUtils.get(key)+1)+"");
//                redisTemplate.opsForValue().set(key,((Integer)redisTemplate.opsForValue().get(key))+1);
            }else{
                //超出访问次数
                render(response,key+"接口请求次数过多！");
                return false;
            }
        }
        return true;
    }


    private void render(HttpServletResponse response, String message)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        out.write(message.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

}
