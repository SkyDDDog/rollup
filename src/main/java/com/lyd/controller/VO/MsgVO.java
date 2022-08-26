package com.lyd.controller.VO;

import lombok.Data;

@Data
public class MsgVO {

    private String fromId;
    private String fromName;
    private String fromHead;
    private String toId;
    private String toHead;
    private String toName;
    private String title;
    private String msg;
    private String date;

}
