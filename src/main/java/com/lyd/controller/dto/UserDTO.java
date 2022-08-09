package com.lyd.controller.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("登录模型")
public class UserDTO {

    private String username;
    private String password;

}
