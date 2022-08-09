package com.lyd.controller.VO;

import lombok.Data;

@Data
public class PrePostVO {

    private Integer rank;
    private String postId;
    private String title;
    private String content;
    private String userId;
    private Integer discussNum;
    private String bestAnswer;
    private String bestAnswerId;
    private Integer likes;
    private Boolean bestAnswerIsLiked;
    private Boolean isCollected;

}
