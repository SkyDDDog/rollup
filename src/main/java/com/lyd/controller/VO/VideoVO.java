package com.lyd.controller.VO;

import lombok.Data;

@Data
public class VideoVO {

    private String videoId;
    private String userId;
    private String userName;
    private String videoName;
    private String videoPhoto;
    private String videoKind;
    private String download;
    private String uploadTime;
    private Boolean isCollected;
//    private String seriesNum;


}
