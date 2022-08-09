package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc posts表实体类
 * @date 2022/7/20
 */

@Data
@TableName("posts")
public class Posts {
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String title;
    private String content;
    private Long user_id;
    private Integer discuss_num;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;
}
