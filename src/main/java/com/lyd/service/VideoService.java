package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.controller.VO.VideoVO;
import com.lyd.entity.Document;
import com.lyd.entity.Video;
import com.lyd.mapper.VideoMapper;
import com.lyd.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class VideoService {

    @Resource
    private VideoMapper videoMapper;
    @Autowired
    private UploadService uploadService;
    @Autowired
    private OssPath ossPath;

    public void newVideo(Long userId,String kind,MultipartFile videoFile) {
        Video video = new Video();
        String photo = null;
        Long id = IdWorker.getId(video);
        String name = videoFile.getOriginalFilename();
        log.info(name);
        // 定义各种路径和文件名
        String suffix = name.substring(name.lastIndexOf('.'));
        String videoFullName = id+suffix;
        String videoPath = "video/";
        String fullPath = videoPath+videoFullName;
        log.info("将文件{}保存至本地{}",videoFullName,videoPath);
        FileUtils.approvalFile(videoFile,videoFullName,videoPath);

        try {
            MultipartFile multipartFile = FileUtils.grabberVideoFramer(videoFullName);
            photo = uploadService.uploadPic(multipartFile, ossPath.getVideoPhotoPath(), id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        video.setId(id);
        video.setUser_id(userId);
        video.setName(name);
        video.setKind(kind);
        video.setPhoto(photo);

        videoMapper.insert(video);
    }

    public VideoVO getVideoById (Long videoId) {
        Video video = videoMapper.selectById(videoId);
        VideoVO videoVO = new VideoVO();
        videoVO.setVideoId(videoId.toString());
        videoVO.setVideoKind(video.getKind());
        videoVO.setVideoName(video.getName());
        videoVO.setVideoPhoto(video.getPhoto());
        videoVO.setUserId(video.getUser_id().toString());

        return videoVO;
    }

    public List<VideoVO> search (String content,Integer pageNum,Integer pageSize) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Video> videos = videoMapper.selectList(wrapper);
        ArrayList<VideoVO> res = new ArrayList<>();
        for (Video video : videos) {
            VideoVO videoVO = new VideoVO();
            videoVO.setVideoId(video.getId().toString());
            videoVO.setUserId(video.getUser_id().toString());
            videoVO.setVideoName(video.getName());
            videoVO.setVideoPhoto(video.getPhoto());
            videoVO.setVideoKind(video.getKind());

            res.add(videoVO);
        }
        return res;
    }

    public Long getSearchCount (String content) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        return videoMapper.selectCount(wrapper);
    }



}
