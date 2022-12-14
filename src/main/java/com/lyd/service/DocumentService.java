package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.DocVO;
import com.lyd.entity.Document;
import com.lyd.entity.UserInfo;
import com.lyd.mapper.DocumentMapper;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.annotation.Resource;
import javax.print.Doc;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private UserService userService;


    /**
     * @desc    查出的DocList统一封装为VO
     * @param src   DocList
     * @return
     */
    public List<DocVO> doc2Vo(List<Document> src,Long userId) {
        ArrayList<DocVO> res = new ArrayList<>();
        int i = 1;
        for (Document document : src) {
            DocVO docVO = new DocVO();
            docVO.setId(document.getId().toString());
            docVO.setName(document.getName());
            docVO.setKind(document.getKind());
            docVO.setPageNum(document.getPage_num());
            docVO.setDownloads(document.getDownloads());
            docVO.setRank(i++);

            UserInfo userInfo = userInfoMapper.selectById(document.getPublisher_id());
            if (userInfo!=null) {
                docVO.setUserName(userInfo.getNickname());
            } else {
                docVO.setUserName("该用户已被封禁");
            }

            docVO.setDocPath(document.getDoc_path());
            docVO.setPhotoPath(document.getPhoto_path());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = sdf.format(document.getGmt_created());
            docVO.setUploadDate(formattedDate);

            if (userId!=null) {
                docVO.setIsCollected(userService.isCollected(userId,document.getId(), (short) 3));
            }

            res.add(docVO);
        }
        return res;
    }


    /**
     * @desc    获取所有文档信息
     * @return
     */
    public List<DocVO> getAll(Long userId) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("downloads");
        List<Document> documents = documentMapper.selectList(wrapper);
        return doc2Vo(documents,userId);
    }

    /**
     * @desc    根据文档id查询
     * @param id    文档id
     * @return  DocVO
     */
    public DocVO getById(Long id,Long userId) {
        Document document = documentMapper.selectById(id);
        DocVO docVO = new DocVO();
        docVO.setId(id.toString());
        docVO.setName(document.getName());
        docVO.setKind(document.getKind());
        docVO.setPageNum(document.getPage_num());
        docVO.setDownloads(document.getDownloads());
        docVO.setUserId(document.getPublisher_id().toString());
        UserInfo userInfo = userInfoMapper.selectById(document.getPublisher_id());
        if (userInfo!=null) {
            docVO.setUserName(userInfo.getNickname());
        } else {
            docVO.setUserName("该用户已被封禁");
        }

        docVO.setDocPath(document.getDoc_path());
        docVO.setPhotoPath(document.getPhoto_path());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = sdf.format(document.getGmt_created());
        docVO.setUploadDate(formattedDate);

        docVO.setIsCollected(userService.isCollected(userId,id,(short)3));
        return docVO;
    }

    /**
     * @desc 分类获取带分页
     * @param kind  0精选|1期末历年卷|2等级考试|3资格证书|4论文模板
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<DocVO> getByKind(Short kind,Long userId,Integer pageNum,Integer pageSize) {
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
        return doc2Vo(documents,userId);
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

    /**
     * @desc    获取热门文档
     * @param userId    用户id(用于判定是否收藏点赞等)
     * @return
     */
    public List<DocVO> getHots(Long userId) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("downloads");
        wrapper.last(" limit 5");
        List<Document> documents = documentMapper.selectList(wrapper);
        return doc2Vo(documents,userId);
    }

    /**
     * @desc 下载文件并更新下载量
     * @param id    文件id
     * @return  文件下载地址
     */
    public Map download(Long id) {
        Map<String, String> map = new HashMap<>();
        Document document = documentMapper.selectById(id);
        document.setDownloads(document.getDownloads()+1);
        documentMapper.updateById(document);
        map.put("downloadUrl",document.getDoc_path());
        map.put("documentName",document.getName());
        return map;
    }

    /**
     * @desc    模糊查询文档名称
     * @param content   查询词
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<DocVO> search(String content,Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Document> documents = documentMapper.selectList(wrapper);
        return doc2Vo(documents,userId);
    }

    /**
     * @desc    获取模糊查询结果个数
     * @param content
     * @return
     */
    public Long getSearchCount(String content) {
        QueryWrapper<Document> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        return documentMapper.selectCount(wrapper);
    }

    /**
     * @desc    逻辑删除文档
     * @param id    文档id
     */
    public void delDoc(Long id) {
        documentMapper.deleteById(id);
    }

    /**
     * @desc    恢复被逻辑删除的文档
     * @param id    文档id
     */
    public void releaseDoc(Long id) {
        documentMapper.unbanDocById(id);
    }


}
