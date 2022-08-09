package com.lyd.controller.VO;

import lombok.Data;

@Data
public class DocVO {

    private Integer rank;
    private String id;
    private String name;
    private String kind;
    private Integer downloads;
    private Integer pageNum;
    private String publisherName;
    private String docPath;
    private String photoPath;
    private String uploadDate;


}
