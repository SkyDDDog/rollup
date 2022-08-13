package com.lyd.controller.VO;

import lombok.Data;

/**
 * @desc 返回用户详细信息
 */
@Data
public class UserVO {

    private String userId;
    private String username;
    private String userhead;
    private String signature;
    private String gender;
    private String school;
    private String academic;
    private String profession;
    private String grade;
    private String email;

}
