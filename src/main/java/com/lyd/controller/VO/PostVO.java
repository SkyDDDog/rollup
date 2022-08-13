package com.lyd.controller.VO;

import lombok.Data;

@Data
public class PostVO {

    private Integer rank;
    private String postId;
    private String title;
    private String content;
    private String userId;
    private String userHead;
    private String userName;
    private String discussNum;
    private String collectNum;
    private Boolean isCollected;
    private String date;


}
