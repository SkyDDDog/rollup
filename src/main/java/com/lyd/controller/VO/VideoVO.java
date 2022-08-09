package com.lyd.controller.VO;

import lombok.Data;

@Data
public class VideoVO {

    private String videoId;
    private String userId;
    //TODO
    // 补充用户信息
    private String videoName;
    private String videoPhoto;
    private String videoKind;


}
