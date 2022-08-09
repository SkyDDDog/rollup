package com.lyd.controller;

import com.lyd.common.Result;
import com.lyd.controller.VO.VideoVO;
import com.lyd.entity.Video;
import com.lyd.handler.NonStaticResourceHttpRequestHandler;
import com.lyd.mapper.UserMapper;
import com.lyd.service.UserService;
import com.lyd.service.VideoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author 天狗
 * @desc 视频接口
 * @date 2022/7/17
 */

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/video")
@CrossOrigin(origins ="*")
@Api("视频流展示接口")
public class VideoController {

    //引入返回视频流的组件
    @Autowired
//    private final NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;
    private NonStaticResourceHttpRequestHandler nonStaticResourceHttpRequestHandler;

    @Autowired
    private VideoService videoService;
    @Autowired
    private UserService userService;

    @Resource
    private UserMapper userMapper;



    /**
     * @desc 传给前端视频流
     * @param id    视频id
     * @param request
     * @param response
     * @throws Exception
     */
    @ApiOperation("根据视频id查询视频流")
    @GetMapping("/view/{id}")
    public void videoPreview(@PathVariable Long id,@RequestParam(required = false) Long userId,HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("访问了/video/"+id+"接口");
        if (userId!=null && userMapper.selectById(userId)!=null) {
            log.info("增加视频历史记录");
            userService.addHistory(userId,id,(short)3);
        }

        String videoName = videoService.getVideoById(id).getVideoName();
        String suffix = videoName.substring(videoName.lastIndexOf('.'));

        String videoPath = "video/"+id+suffix;
        //保存视频磁盘路径
        Path filePath = Paths.get(videoPath);
        //Files.exists：用来测试路径文件是否存在
        if (Files.exists(filePath)) {
            //获取视频的类型，比如是MP4这样
            String mimeType = Files.probeContentType(filePath);
            if (StringUtils.hasText(mimeType)) {
                //判断类型，根据不同的类型文件来处理对应的数据
                response.setContentType(mimeType);
            }
            //转换视频流部分
            request.setAttribute(NonStaticResourceHttpRequestHandler.ATTR_FILE, filePath);
            nonStaticResourceHttpRequestHandler.handleRequest(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        }
    }

    @ApiOperation("查询视频信息")
    @GetMapping("/{id}")
    public Result getVideoInfo(@PathVariable Long id) {
        VideoVO videoById = videoService.getVideoById(id);
        return Result.success(videoById);
    }

    @ApiOperation("上传视频")
    @PostMapping("/upload/{userId}")
    public Result uploadVideo(@PathVariable Long userId, @RequestParam MultipartFile videoFile,@RequestParam(required = false) String kind) {
        log.info("访问了/video/upload/{}接口",userId);
        videoService.newVideo(userId, kind, videoFile);

        return Result.success();
    }


}
