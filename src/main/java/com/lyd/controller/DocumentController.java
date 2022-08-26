package com.lyd.controller;

import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.DocVO;
import com.lyd.entity.Document;
import com.lyd.mapper.DocumentMapper;
import com.lyd.service.DocumentService;
import com.lyd.service.UploadService;
import com.lyd.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/doc")
@CrossOrigin(origins ="*")
@Api("资料区接口")
public class DocumentController {

    @Autowired
    private UploadService uploadService;
    @Autowired
    private DocumentService documentService;
    @Resource
    private DocumentMapper documentMapper;
    @Autowired
    private UserService userService;

    @ApiOperation("下载接口")
    @GetMapping("/download/{id}")
    public Result download(@PathVariable Long id) {
        log.info("访问了/#/download接口");
        Map download = documentService.download(id);
        return Result.success(download);
    }

    @ApiOperation("根据资料id获取资料信息")
    @GetMapping("/{docId}")
    public Result getById(@PathVariable Long docId,@RequestParam(required = false) Long userId) {
        log.info("访问了/doc/"+docId+"接口");
        if (documentMapper.selectById(docId)==null) {
            return Result.error(Constants.CODE_400,"不存在该文档");
        }
        DocVO byId = documentService.getById(docId,userId);
        if (userId!=null) {
            log.info("产生文档{}的历史记录",docId);
            userService.addHistory(userId,docId,(short)2);
        }

        return Result.success(byId);
    }

    @ApiOperation(("kind:0精选|1期末历年卷|2等级考试|3资格证书|4论文模板"))
    @GetMapping("/{kind}/{pageNum}/{pageSize}")
    public Result getByKind(@PathVariable Short kind,@RequestParam(required = false) Long userId,
                            @PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/doc/"+kind+"/"+pageNum+"/"+pageSize+"接口");
        if (kind!=1 && kind!=2 && kind!=3 && kind!=4 && kind!=0) {
            return Result.error(Constants.CODE_400,"kind:0精选|1期末历年卷|2等级考试|3资格证书|4论文模板");
        }
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"从第一页开始");
        }

        List<DocVO> byKind = documentService.getByKind(kind,userId,pageNum, pageSize);
        return Result.success(byKind,documentService.getCount(kind));
    }

    @ApiOperation("热门资料")
    @GetMapping("/hot")
    public Result getHots(@RequestParam(required = false)Long userId) {
        log.info("访问了/doc/hot接口");
        List<DocVO> hots = documentService.getHots(userId);
        return Result.success(hots);
    }


    @ApiOperation("获取预览用pdf(返回pdf文件流)")
    @GetMapping("/preview/{id}")
    public Result previewPdf(@PathVariable Long id, HttpServletResponse response) {
        if (documentMapper.selectById(id)==null) {
            return Result.error(Constants.CODE_400,"不存在该文件");
        }
        String path = "previewPdf/"+id.toString()+".pdf";
        try {
            FileInputStream in = new FileInputStream(path);
            ServletOutputStream outputStream = null;
//            response.reset();
            response.setContentType("application/pdf");
            outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n=in.read(buffer))!=-1) {
                outputStream.write(buffer,0,n);
            }

            outputStream.close();
            in.close();
            return Result.success();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(Constants.CODE_500,"文件打开错误");
        }

    }

    @ApiOperation("上传文档资料")
    @PostMapping("/upload/{userId}/{kind}")
    public Result uploadDoc(@PathVariable Long userId, @PathVariable(required = false)String kind,
                            @RequestParam MultipartFile multipartFile) {
        Document document = uploadService.uploadDoc(multipartFile, userId, kind);
        uploadService.uploadInfo2DB(document);
        return Result.success();

    }


}
