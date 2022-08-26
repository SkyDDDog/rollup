package com.lyd.controller.VO;

import lombok.Data;

/**
 * @desc 收藏
 */
@Data
public class CollectionVO {

    private String id;
    private String targetId;
    private String title;
    private String content;
    private String sort;
    private String date;
    private String discussNum;

}
