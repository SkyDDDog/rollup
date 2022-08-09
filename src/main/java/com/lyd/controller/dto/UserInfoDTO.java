package com.lyd.controller.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
@ApiModel("用户信息交互模型")
public class UserInfoDTO {
    @ApiModelProperty(value = "用户id",required = true)
    private String userId;
    @ApiModelProperty(value = "用户名",required = false)
    private String nickname;
    @ApiModelProperty(value = "个性签名",required = false)
    private String signature;
    @ApiModelProperty(value = "邮箱",required = false)
    private String email;
    @ApiModelProperty(value = "性别(0男|1女|2不详)",required = false)
    private Short gender;
    @ApiModelProperty(value = "学校",required = false)
    private String school;
    @ApiModelProperty(value = "学院",required = false)
    private String academic;
    @ApiModelProperty(value = "专业",required = false)
    private String profession;
    @ApiModelProperty(value = "入学年份",required = false)
    private Integer grade;
}
