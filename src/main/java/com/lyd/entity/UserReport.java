package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc user_report表实体类
 * @date 2022/8/8
 */

@Data
@TableName("user_report")
public class UserReport {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long user_id;
    private Long target_id;
    private Short sort;
    private String reason;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
