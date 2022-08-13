package com.lyd.controller.VO;

import lombok.Data;

@Data
public class ReportVO {

    private String reportId;
    private String reporterId;
    private String reporterName;
    private String reporterHead;
    private String reportedId;
    // sort:1用户|2帖子|3回答|4评论
    private String sort;
    private String content;
    private String photo;
    private String reason;


}
