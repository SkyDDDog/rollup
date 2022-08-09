package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc todo表实体类
 * @date 2022/7/21
 */

@Data
@TableName("todo")
public class Todo {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long user_id;
    private String content;
    private Integer time;
    private boolean is_done;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
