package com.lyd.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 天狗
 * @desc AliOss配置类
 */
@Slf4j
@Component
public class OssUtils {

    //阿里云API的内或外网域名
    private static String ENDPOINT;
    //阿里云API的密钥Access Key ID
    private static String ACCESS_KEY_ID;
    //阿里云API的密钥Access Key Secret
    private static String ACCESS_KEY_SECRET;
    //阿里云API的bucket名称
    private static String BACKET_NAME;
    //阿里云API的前缀url
    private static String URL_PREFIX;
    //阿里云API的文件夹名称
//    private static String FOLDER;

    @Value("${aliyun.oss.endpoint}")
    public void setENDPOINT(String ENDPOINT) {
        OssUtils.ENDPOINT = ENDPOINT;
    }
    @Value("${aliyun.accessKeyId}")
    public void setAccessKeyId(String accessKeyId) {
        ACCESS_KEY_ID = accessKeyId;
    }
    @Value("${aliyun.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        ACCESS_KEY_SECRET = accessKeySecret;
    }
    @Value("${aliyun.oss.bucketName}")
    public void setBacketName(String backetName) {
        BACKET_NAME = backetName;
    }
    @Value("${aliyun.oss.urlPrefix}")
    public void setUrlPrefix(String urlPrefix) {
        URL_PREFIX = urlPrefix;
    }

    public static OSSClient getOSSClient() {
        String endpoint="https://"+ENDPOINT;
        return new OSSClient(endpoint, ACCESS_KEY_ID, ACCESS_KEY_SECRET);
    }
    private static String getContentType(String fileName) {
        //文件的后缀名
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        if (".bmp".equalsIgnoreCase(fileExtension)) {
            return "image/bmp";
        }
        if (".gif".equalsIgnoreCase(fileExtension)) {
            return "image/gif";
        }
        if (".jpeg".equalsIgnoreCase(fileExtension) || ".jpg".equalsIgnoreCase(fileExtension) || ".png"
                .equalsIgnoreCase(fileExtension)) {
            return "image/jpeg";
        }
        if (".html".equalsIgnoreCase(fileExtension)) {
            return "text/html";
        }
        if (".txt".equalsIgnoreCase(fileExtension)) {
            return "text/plain";
        }
        if (".vsd".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.visio";
        }
        if (".ppt".equalsIgnoreCase(fileExtension) || "pptx".equalsIgnoreCase(fileExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (".doc".equalsIgnoreCase(fileExtension) || "docx".equalsIgnoreCase(fileExtension)) {
            return "application/msword";
        }
        if (".xml".equalsIgnoreCase(fileExtension)) {
            return "text/xml";
        }
        //默认返回类型
        return "image/jpeg";
    }

    /**
     * @author 222100209_李炎东
     * @desc  如果同名文件会覆盖服务器上的
     * @param instream 文件流
     * @param fileName 文件名称 包括后缀名
     * @return 出错返回"" ,唯一MD5数字签名
     */
    private static void uploadFile2OSS(InputStream instream, String fileName,
                                       OSSClient ossClient, String folder) {
        try {
            //创建上传Object的Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(instream.available());
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setHeader("Pragma", "no-cache");
            objectMetadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("inline;filename=" + fileName);
            //上传文件
            ossClient.putObject(BACKET_NAME, folder + fileName, instream, objectMetadata);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (instream != null) {
                    instream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        ossClient.shutdown();
    }

    /**
     * @author 222100209_李炎东
     * @desc 上传一张图片
     * @param multipartFile 图片
     * @param ossClient Oss客户端
     * @param folder 路径
     * @return  回显url
     */
    public static String saveImg(MultipartFile multipartFile,OSSClient ossClient,String fileName,String folder){
        //防止名字冲突覆盖原有图片  使用雪花算法生成的主键作为文件名
//        String extension = multipartFile.getOriginalFilename();
//        extension = extension.substring(extension.lastIndexOf("."));
        String extension = ".jpg";
        fileName = fileName + extension;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            OssUtils.uploadFile2OSS(inputStream, fileName,ossClient,folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String  urlName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
//        return "https//" + BACKET_NAME+"."+ENDPOINT+ File.separator + folder +urlName;
        return URL_PREFIX + '/' + folder +urlName;
    }

    /**
     * @desc    上传文档至oss
     * @param multipartFile 文档
     * @param ossClient oss客户端
     * @param fileName  文件名(雪花算法全局唯一主键)
     * @return  回显url
     */
    public static String saveDoc(MultipartFile multipartFile,OSSClient ossClient,String fileName,String folder) {
//        String folder = "document/";
        String extension = multipartFile.getOriginalFilename();
        if ("".equals(extension)) {
            extension = multipartFile.getName();
        }
        extension = extension.substring(extension.lastIndexOf("."));

        fileName = fileName + extension;
        try {
            InputStream inputStream = multipartFile.getInputStream();
            OssUtils.uploadFile2OSS(inputStream,fileName,ossClient,folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String urlName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());

        return URL_PREFIX + '/' + folder +urlName;
    }


}