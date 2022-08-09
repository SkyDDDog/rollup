package com.lyd.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author 天狗
 * @desc document
 * @date 2022/7/26
 */

@Data
@TableName("document")
public class Document {

    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Long publisher_id;
    private String kind;
    private Integer downloads;
    private Integer page_num;
    private String doc_path;
    private String photo_path;

    @TableLogic
    private boolean is_deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date gmt_created;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmt_modified;

}
