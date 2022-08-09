package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc user_like表实体类
 * @date 2022/7/27
 */

@Data
@TableName("user_like")
public class UserLike {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long user_id;
    private Long answer_id;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
