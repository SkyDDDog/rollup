package com.lyd.controller.VO;

import lombok.Data;

/**
 * @author 天狗
 * @date 2022/7/21
 */
@Data
public class TodoVO {

    private Integer rank;
    private String id;
    private String content;
    private String time;
    private boolean isDone;

}
