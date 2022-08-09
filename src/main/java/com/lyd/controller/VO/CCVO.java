package com.lyd.controller.VO;

import lombok.Data;

/**
 * @desc 回答的评论
 * @date 2022/7/21
 */
@Data
public class CCVO {

    private String ccId;
    private String commentId;
    private String content;

    private String fromId;
    private String toId;
    //TODO
    // 待补充用户相关信息
    private String username;
    private String userhead;


    private Integer likes;

    //TODO
    // 待补充日期信息
    private String date;

}
