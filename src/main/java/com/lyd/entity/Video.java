package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc video表实体类
 * @date 2022/8/4
 */

@Data
@TableName("video")
public class Video {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Long user_id;
    private String kind;
    private String photo;
    private Integer download;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
