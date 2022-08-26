package com.lyd.controller;

import com.lyd.common.Result;
import com.lyd.controller.VO.VideoVO;
import com.lyd.handler.NonStaticResourceHttpRequestHandler;
import com.lyd.mapper.UserMapper;
import com.lyd.mapper.VideoMapper;
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
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


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
    @Resource
    private VideoMapper videoMapper;



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

        String videoName = videoMapper.selectById(id).getName();
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

    @ApiOperation("下载视频")
    @GetMapping("/download/{id}")
    public void downloadVideo(@PathVariable Long id,HttpServletResponse response) {
        String videoName = videoMapper.selectById(id).getName();
        String suffix = videoName.substring(videoName.lastIndexOf('.'));
        String videoPath = "video/"+id+suffix;

        try {
            // path是指想要下载的文件的路径
            File file = new File(videoPath);
            log.info(file.getPath());
            // 获取文件名
            String filename = file.getName();
            // 获取文件后缀名
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            log.info("文件后缀名：" + ext);

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            // 清空response
//            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();

            // 更新下载量
            videoService.updDownload(id);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @ApiOperation("查询视频信息")
    @GetMapping("/{id}")
    public Result getVideoInfo(@PathVariable Long id,@RequestParam(required = false)Long userId) {
        VideoVO videoById = videoService.getVideoById(id,userId);
        return Result.success(videoById);
    }

    @ApiOperation("上传视频")
    @PostMapping("/upload/{userId}")
    public Result uploadVideo(@PathVariable Long userId, @RequestParam MultipartFile videoFile,@RequestParam(required = false) String kind) {
        log.info("访问了/video/upload/{}接口",userId);
        videoService.newVideo(userId, kind, videoFile);

        return Result.success();
    }

    @ApiOperation("分页获取所有视频信息")
    @GetMapping("/all/{pageNum}/{pageSize}")
    public Result getAllVideosByPage(@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/video/all/{}/{}接口",pageNum,pageSize);
        List<VideoVO> allByPage = videoService.getAllByPage(pageNum, pageSize);

        return Result.success(allByPage, videoService.getAllCount());
    }




}
