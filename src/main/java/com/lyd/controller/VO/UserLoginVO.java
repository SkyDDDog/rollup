package com.lyd.controller.VO;

import lombok.Data;

@Data
public class UserLoginVO {

    private String userId;
    private String email;
    private String password;
    private String authority;

}
