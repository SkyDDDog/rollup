package com.lyd.controller;

import com.lyd.entity.Document;
import com.lyd.entity.User;
import com.lyd.mapper.UserMapper;
import com.lyd.service.UploadService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author 天狗
 * @desc 测试springsecurity用的接口
 * @date 2022/7/17
 */
@RestController
@RequestMapping("/test")
@Api("测试接口")
public class TestController {

    @Autowired
    BCryptPasswordEncoder encoder;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UploadService uploadService;

    @GetMapping("/newUser")
    public void newUser(@RequestParam String email,@RequestParam String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRole("ROLE_admin");

        userMapper.insert(user);
    }

    @PostMapping("/upload")
    public Document upload(@RequestParam MultipartFile multipartFile, @RequestParam Long userId, @RequestParam String kind) {
        return uploadService.uploadDoc(multipartFile,userId,kind);

    }



}
