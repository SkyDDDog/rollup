package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.lyd.config.OssPath;
import com.lyd.controller.VO.VideoVO;
import com.lyd.entity.Document;
import com.lyd.entity.Video;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
import com.lyd.mapper.VideoMapper;
import com.lyd.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    @Resource
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserService userService;

    /**
     * @desc    video转VO
     * @param src
     * @return
     */
    public List<VideoVO> video2Vo(List<Video> src) {
        ArrayList<VideoVO> res = new ArrayList<>();
        for (Video video : src) {
            VideoVO videoVO = new VideoVO();
            videoVO.setVideoId(video.getId().toString());
            videoVO.setUserId(video.getUser_id().toString());
            videoVO.setVideoName(video.getName());
            videoVO.setVideoPhoto(video.getPhoto());
            videoVO.setVideoKind(video.getKind());
            videoVO.setDownload(video.getDownload().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            videoVO.setUploadTime(sdf.format(video.getGmt_created()));
            videoVO.setUserName(userInfoMapper.selectById(video.getUser_id()).getNickname());

            res.add(videoVO);
        }
        return res;
    }

    /**
     * @desc    上传视频
     * @param userId    用户id
     * @param kind      视频类型
     * @param videoFile 视频文件
     */
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

    /**
     * @desc    通过id获取视频信息
     * @param videoId   视频id
     * @param userId    用户id
     * @return
     */
    public VideoVO getVideoById (Long videoId,Long userId) {
        Video video = videoMapper.selectById(videoId);
        VideoVO videoVO = new VideoVO();
        videoVO.setVideoId(videoId.toString());
        videoVO.setVideoKind(video.getKind());
        videoVO.setVideoName(video.getName());
        videoVO.setVideoPhoto(video.getPhoto());
        videoVO.setUserId(video.getUser_id().toString());
        videoVO.setUserName(userInfoMapper.selectById(video.getUser_id()).getNickname());
        videoVO.setDownload(video.getDownload().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        videoVO.setUploadTime(sdf.format(video.getGmt_created()));
        if (userId!=null) {
            videoVO.setIsCollected(userService.isCollected(userId,videoId,(short)4));
        }

        return videoVO;
    }

    /**
     * @desc    模糊查询视频
     * @param content   模糊查询词
     * @param pageNum   第?页
     * @param pageSize  一页?条
     * @return
     */
    public List<VideoVO> search (String content,Integer pageNum,Integer pageSize) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Video> videos = videoMapper.selectList(wrapper);
        return video2Vo(videos);
    }

    /**
     * @desc    模糊查询结果数
     * @param content   模糊查询词
     * @return
     */
    public Long getSearchCount (String content) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
        wrapper.like("name",content);
        return videoMapper.selectCount(wrapper);
    }

    /**
     * @desc    分页查询所有视频
     * @param pageNum   第?页
     * @param pageSize  一页?条
     * @return
     */
    public List<VideoVO> getAllByPage (Integer pageNum,Integer pageSize) {
        QueryWrapper<Video> wrapper = new QueryWrapper<>();
//        wrapper.groupBy("series_id");
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<Video> videos = videoMapper.selectList(wrapper);
        return video2Vo(videos);
    }

    /**
     * @desc    获取视频总数
     * @return
     */
    public Long getAllCount() {
        return videoMapper.selectCount(null);
    }

    /**
     * @desc    逻辑删除视频
     * @param videoId   视频id
     */
    public void delVideo(Long videoId) {
        videoMapper.deleteById(videoId);
    }

    /**
     * @desc    恢复逻辑删除的视频
     * @param videoId   视频id
     */
    public void releaseVideo(Long videoId) {
        videoMapper.unbanVideoById(videoId);
    }

    /**
     * @desc 更新下载量
     * @param id 视频id
     */
    public void updDownload(Long id) {
        Video video = videoMapper.selectById(id);
        video.setDownload(video.getDownload()+1);
        videoMapper.updateById(video);
    }


}
