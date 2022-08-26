package com.lyd.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyuncs.auth.ECSMetadataServiceCredentialsFetcher;
import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.service.MailService;
import com.lyd.utils.RedisUtils;
import com.lyd.utils.VerCodeGenerateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author 222100209_李炎东
 * @desc 通过JavaMailSender发送高级邮件，作为邮箱验证码并存入redis
 */

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/mail")
@CrossOrigin(origins ="*")
@Api("邮件接口")
public class MailController {
    @Autowired
    private JavaMailSenderImpl mailSender;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MailService mailService;

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * @author 222100209_李炎东
     * @param email 用户邮箱
     * @return
     */
    @GetMapping(value = "/send/{email}")
    @ApiOperation("给对应用户发送邮件验证码")
    public Result sendEmail(@PathVariable String email) {
        log.info("访问了/mail/send/"+email+"接口");
        //key 邮箱号  value 验证码
        String code = redisTemplate.opsForValue().get(email);
        //从redis获取验证码，如果获取获取到，返回ok
        if (!StringUtils.isEmpty(code)) {
            System.out.println("redis中已存在验证码");
            return Result.success(code);
        }
        //如果从redis获取不到，生成新的4位验证码
        code = VerCodeGenerateUtil.getVerCode();
        //调用service方法，通过邮箱服务进行发送
        boolean isSend = mailService.sendMail(email, code);
        //生成验证码放到redis里面，设置有效时间
        if (isSend) {
            redisTemplate.opsForValue().set(email, code, 1, TimeUnit.MINUTES);
            return Result.success();
        } else {
            return Result.error(Constants.CODE_600,"发送邮箱验证码失败");
        }
    }

    /**
     * @author 222100209_李炎东
     * @param email 用户邮箱
     * @param VerCode 收到的验证码
     * @return
     */
    @GetMapping("/cmp/{email}/{VerCode}")
    @ApiOperation("验证邮箱验证码")
    public Result compareCode(@PathVariable String email, @PathVariable String VerCode) {
        log.info("访问了/mail/cmp接口");
        String code = (String) redisUtils.get(email);
        if (code.equals(VerCode)) {
            return Result.success();
        } else {
            return Result.error(Constants.CODE_401,"验证码错误");
        }
    }

}

