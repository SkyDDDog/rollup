package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc post_comments表实体类
 * @date 2022/7/20
 */

@Data
@TableName("post_comments")
public class PostComments {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long post_id;
    private String content;
    private Long user_id;
    private Integer likes;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;
}
