package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc video_history表
 * @date 2022/7/26
 */

@Data
@TableName("history")
public class History {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private Long user_id;
    private Long target_id;
    // 1帖子|2资料
    private Short sort;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;



}
