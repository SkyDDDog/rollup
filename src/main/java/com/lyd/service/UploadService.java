package com.lyd.service;

import com.aliyun.oss.OSSClient;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.entity.Document;
import com.lyd.mapper.DocumentMapper;
import com.lyd.utils.FileUtils;
import com.lyd.utils.OssUtils;
import com.lyd.utils.PdfUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author 天狗
 * @desc 上传图片
 */
@Slf4j
@Service
public class UploadService {

    @Autowired
    private OSSClient ossClient;
    @Autowired
    private OssPath ossPath;
    @Resource
    private DocumentMapper documentMapper;

    /**
     * @author 222100209_李炎东
     * @param multipartFile 图片
     * @param folder Oss中存储路径
     * @return 图片url
     * @throws FileNotFoundException
     */
    public String uploadPic(MultipartFile multipartFile, String folder,Long id) {
//        InputStream in = multipartFile.getInputStream();
        String fileName = id+"";
        return OssUtils.saveImg(multipartFile,ossClient,fileName,folder);
    }

    public Document uploadDoc(MultipartFile multipartFile,Long userId,String kind) {
        Document document = new Document();
        Long id = IdWorker.getId(document);

        document.setId(id);
        document.setPublisher_id(userId);
        document.setKind(kind);
        document.setDownloads(0);

        String originalFilename = multipartFile.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            originalFilename = multipartFile.getName();
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));

        // 定义各种路径/文件名
        String docFullName = id+suffix;
        String pdfFullName = id+".pdf";
        String tmpPath = "tmp/";
        String tmpFilePath = tmpPath + docFullName;
        String tmpOutPath = tmpPath + pdfFullName;
        String previewPdfPath = "previewPdf/"+pdfFullName;
        document.setName(originalFilename);

        // 将文件临时缓存至本地
        FileUtils.approvalFile(multipartFile,docFullName,tmpPath);

        // 文档上传oss
        String docURL = OssUtils.saveDoc(multipartFile, ossClient, id.toString(), ossPath.getDocumentPath());
        document.setDoc_path(docURL);

        // 水印字样
        final String mark = "卷吧";
        String photoURL = null;
        Integer pdfPageNum = null;
        if (".doc".equals(suffix) || ".docx".equals(suffix)) {
            PdfUtils.word2pdf(tmpFilePath,tmpOutPath);
        } else if (".xls".equals(suffix) || ".xlsx".equals(suffix)) {
            PdfUtils.excel2pdf(tmpFilePath,tmpOutPath);
        } else if (".ppt".equals(suffix) || ".pptx".equals(suffix)) {
            PdfUtils.ppt2pdf(tmpFilePath,tmpOutPath);
        } else if (".pdf".equals(suffix)) {

        } else {
            return null;
        }
        PdfUtils.wateMark(tmpOutPath,previewPdfPath,mark);
        // 获取缩略图
        MultipartFile photo = PdfUtils.PDFFramer(previewPdfPath);
        photoURL = OssUtils.saveImg(photo, ossClient, id.toString(), ossPath.getThumbnailPath());
        // 获取文档页数
        try {
            pdfPageNum = PdfUtils.getPdfPageNum(tmpOutPath);
        } catch (IOException e) {
            e.printStackTrace();
            pdfPageNum = null;
        }

        document.setPhoto_path(photoURL);
        document.setPage_num(pdfPageNum);

        // 删除本地缓存文件
        FileUtils.delFileInDir(tmpPath);

        return document;
    }

    public void uploadInfo2DB(Document document) {
        documentMapper.insert(document);
    }

}
