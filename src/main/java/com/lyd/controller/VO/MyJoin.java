package com.lyd.controller.VO;

import lombok.Data;

/**
 * @desc 我参与的
 */
@Data
public class MyJoin {

    private String postId;
    private String commentId;
    private String title;
    private String commentContent;
    private String likes;
    private String date;

}
