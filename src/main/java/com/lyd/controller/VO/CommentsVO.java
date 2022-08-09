package com.lyd.controller.VO;

import lombok.Data;

import java.util.List;

/**
 * @desc 帖子的回答
 * @date 2022/7/21
 */
@Data
public class CommentsVO {

    private String commentId;
    private String postId;
    private String commentNum;
    private String collectNum;

    private String content;
    private String userId;
    private String username;
    private String userhead;
    private Integer likes;
    private Boolean isLiked;
    private Boolean isCollected;
    private String signature;
    private String date;

}
