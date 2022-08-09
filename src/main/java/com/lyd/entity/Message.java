package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc message表
 * @date 2022/8/24
 */

@Data
@TableName("message")
public class Message {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long from_id;
    private Long to_id;
    private String msg;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
