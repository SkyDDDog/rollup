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
    private String username;
    private String userhead;
    private Integer likes;
    private String date;

}
