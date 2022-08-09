package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.DocVO;
import com.lyd.entity.Document;
import com.lyd.mapper.DocumentMapper;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 天狗
 * @date 2022/7/26
 */
@Service
public class DocumentService {

    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * @desc    获取所有文档信息
     * @return
     */
    public List<DocVO> getAll() {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("downloads");
        List<Document> documents = documentMapper.selectList(wrapper);
        ArrayList<DocVO> res = new ArrayList<>();
        for (Document document : documents) {
            DocVO docVO = new DocVO();
            docVO.setId(document.getId().toString());
            docVO.setName(document.getName());
            docVO.setKind(document.getKind());
            docVO.setPageNum(document.getPage_num());
            docVO.setDownloads(document.getDownloads());

            String nickname = userInfoMapper.selectById(document.getId()).getNickname();
            docVO.setPublisherName(nickname);

            docVO.setDocPath(document.getDoc_path());
            docVO.setPhotoPath(document.getPhoto_path());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(document.getGmt_created());
            docVO.setUploadDate(formattedDate);

            res.add(docVO);
        }

        return res;
    }

    /**
     * @desc    根据文档id查询
     * @param id    文档id
     * @return  DocVO
     */
    public DocVO getById(Long id) {
        Document document = documentMapper.selectById(id);
        DocVO docVO = new DocVO();
        docVO.setId(document.getId().toString());
        docVO.setName(document.getName());
        docVO.setKind(document.getKind());
        docVO.setPageNum(document.getPage_num());
        docVO.setDownloads(document.getDownloads());

        String nickname = userInfoMapper.selectById(document.getPublisher_id()).getNickname();
        docVO.setPublisherName(nickname);

        docVO.setDocPath(document.getDoc_path());
        docVO.setPhotoPath(document.getPhoto_path());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(document.getGmt_created());
        docVO.setUploadDate(formattedDate);
        return docVO;
    }

    /**
     * @desc 分类获取带分页
     * @param kind  0精选|1期末历年卷|2等级考试|3资格证书|4论文模板
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<DocVO> getByKind(Short kind,Integer pageNum,Integer pageSize) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        if (kind==1) {
            wrapper.eq("kind","期末历年卷");
        } else if (kind==2) {
            wrapper.eq("kind","等级考试");
        } else if (kind==3) {
            wrapper.eq("kind","资格证书");
        } else if (kind==4) {
            wrapper.eq("kind","论文模板");
        }
        wrapper.orderByDesc("downloads");
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Document> documents = documentMapper.selectList(wrapper);
        ArrayList<DocVO> res = new ArrayList<>();
        for (Document document : documents) {
            DocVO docVO = new DocVO();
            docVO.setId(document.getId().toString());
            docVO.setName(document.getName());
            docVO.setKind(document.getKind());
            docVO.setPageNum(document.getPage_num());
            docVO.setDownloads(document.getDownloads());

            String nickname = userInfoMapper.selectById(document.getPublisher_id()).getNickname();
            docVO.setPublisherName(nickname);

            docVO.setDocPath(document.getDoc_path());
            docVO.setPhotoPath(document.getPhoto_path());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(document.getGmt_created());
            docVO.setUploadDate(formattedDate);

            res.add(docVO);
        }

        return res;
    }

    /**
     * @desc 获取总文档数量
     * @param kind  0精选|1期末历年卷|2等级考试|3资格证书|4论文模板
     * @return  Long
     */
    public Long getCount(Short kind) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        if (kind==null) {

        } else if (kind==1) {
            wrapper.eq("kind","期末历年卷");
        } else if (kind==2) {
            wrapper.eq("kind","等级考试");
        } else if (kind==3) {
            wrapper.eq("kind","资格证书");
        } else if (kind==4) {
            wrapper.eq("kind","论文模板");
        }
        return documentMapper.selectCount(wrapper);
    }

    public List<DocVO> getHots() {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("downloads");
        wrapper.last(" limit 5");
        List<Document> documents = documentMapper.selectList(wrapper);
        ArrayList<DocVO> res = new ArrayList<>();
        int i = 1;
        for (Document document : documents) {
            DocVO docVO = new DocVO();
            docVO.setId(document.getId().toString());
            docVO.setName(document.getName());
            docVO.setKind(document.getKind());
            docVO.setPageNum(document.getPage_num());
            docVO.setRank(i++);
            docVO.setDownloads(document.getDownloads());

            String nickname = userInfoMapper.selectById(document.getPublisher_id()).getNickname();
            docVO.setPublisherName(nickname);

            docVO.setDocPath(document.getDoc_path());
            docVO.setPhotoPath(document.getPhoto_path());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(document.getGmt_created());
            docVO.setUploadDate(formattedDate);

            res.add(docVO);
        }

        return res;
    }

    /**
     * @desc 下载文件并更新下载量
     * @param id    文件id
     * @return  文件下载地址
     */
    public String download(Long id) {
        Document document = documentMapper.selectById(id);
        document.setDownloads(document.getDownloads()+1);
        documentMapper.updateById(document);
        return document.getDoc_path();
    }

    /**
     * @desc    模糊查询文档名称
     * @param content   查询词
     * @param type  排序类型(1热门|2最新)
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<DocVO> search(String content,Short type,Integer pageNum,Integer pageSize) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        if (type==1) {
            wrapper.orderByDesc("downloads");
        } else if (type==2) {
            wrapper.orderByDesc("gmt_created");
        }
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Document> documents = documentMapper.selectList(wrapper);
        ArrayList<DocVO> res = new ArrayList<>();
        for (Document document : documents) {
            DocVO docVO = new DocVO();
            docVO.setId(document.getId().toString());
            docVO.setName(document.getName());
            docVO.setKind(document.getKind());
            docVO.setPageNum(document.getPage_num());
            docVO.setDownloads(document.getDownloads());

            String nickname = userInfoMapper.selectById(document.getPublisher_id()).getNickname();
            docVO.setPublisherName(nickname);

            docVO.setDocPath(document.getDoc_path());
            docVO.setPhotoPath(document.getPhoto_path());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(document.getGmt_created());
            docVO.setUploadDate(formattedDate);

            res.add(docVO);
        }
        return res;

    }

}
