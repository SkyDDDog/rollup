package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc user_info表实体类
 * @date 2022/7/26
 */

@Data
@TableName("user_info")
public class UserInfo {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String nickname;
    private String head;
    private String signature;
    // false男 true女 null不详
    private Boolean gender;
    private String school;
    private String academic;
    private String profession;
    private Integer grade;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
