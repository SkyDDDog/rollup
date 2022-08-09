package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc user表实体类
 * @date 2022/7/20
 */

@Data
@TableName("user")
public class User {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String email;
    private String password;
    private String role;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
