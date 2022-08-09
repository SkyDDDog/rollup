package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

/**
 * @author 天狗
 * @desc 测试springsecurity用的实体类
 * @date 2022/7/17
 */

@Data
@TableName("testUser")
public class TestUser {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;
    private String email;
    private String password;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
