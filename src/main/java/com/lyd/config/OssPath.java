package com.lyd.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
public class OssPath {

    private String documentPath;
    private String thumbnailPath;
    private String userHeadPath;
    private String videoPhotoPath;

    @Value("${aliyun.oss.userHeadPath}")
    public void setUserHeadPath(String user) {
        userHeadPath = user;
    }
    @Value("${aliyun.oss.thumbnailPath}")
    public void setThumbnailPath(String thum) {
        thumbnailPath = thum;
    }
    @Value("${aliyun.oss.documentPath}")
    public void setDocumentPath(String doc) {
        documentPath = doc;
    }
    @Value("${aliyun.oss.videoPhotoPath}")
    public void setTempPdfPath(String temp) {
        videoPhotoPath = temp;
    }

}
