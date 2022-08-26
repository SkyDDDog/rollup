package com.lyd.controller;

import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.BanUserVO;
import com.lyd.controller.VO.ReportVO;
import com.lyd.entity.UserReport;
import com.lyd.mapper.UserMapper;
import com.lyd.mapper.UserReportMapper;
import com.lyd.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@ResponseBody
@RestController
@Controller
@RequestMapping("/report")
@CrossOrigin(origins ="*")
@Api("举报和管理接口")
public class ReportController {

    @Autowired
    private UserService userService;
    @Autowired
    private PostsService postsService;
    @Autowired
    private CommentsService commentsService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private MessageService messageService;

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserReportMapper userReportMapper;





    @ApiOperation("举报(sort:0用户|1帖子|2回答|3评论|4文档|5视频)")
    @PostMapping("/{sort}/{userId}/{targetId}")
    public Result report(@PathVariable Short sort, @PathVariable Long userId, @PathVariable Long targetId, @RequestParam(required = false) String reason) {
        if (sort>5 || sort<0) {
            return Result.error(Constants.CODE_400,"sort:0用户|1帖子|2回答|3评论|4文档|5视频");
        }
        if (!userService.getReportBy(userId,targetId).isEmpty()) {
            return Result.error(Constants.CODE_400,"该用户已举报过此项目");
        }
        userService.addReport(userId,targetId,sort,reason);
        return Result.reportSuccess();
    }

    @ApiOperation("解封")
    @GetMapping("/{reportId}")
    public Result unbanByReportId(@PathVariable Long reportId) {
        UserReport report = userReportMapper.selectById(reportId);
        if (report==null) {
            return Result.error(Constants.CODE_400,"不存在该举报记录");
        }
        Short sort = report.getSort();      // 0用户|1帖子|2回答|3评论|4文档|5视频
        Long targetId = report.getTarget_id();
        if (sort==0) {
            userService.unbanUser(targetId);
        } else if (sort==1) {
            postsService.releasePost(targetId);
        } else if (sort==2) {
            commentsService.releasePc(targetId);
        } else if (sort==3) {
            commentsService.releasePc(targetId);
        } else if (sort==4) {
            documentService.releaseDoc(targetId);
        } else if (sort==5) {
            videoService.releaseVideo(targetId);
        }
        return Result.success();
    }

    @ApiOperation("被举报名单")
    @GetMapping("/getList/{pageNum}/{pageSize}")
    public Result getReported(@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"应从第一页开始");
        }
        List<ReportVO> reported = userService.getReported(pageNum, pageSize);
        return Result.success(reported, userService.getReportedNum());
    }




    @ApiOperation("处理举报(result:1删除|2保留)")
    @DeleteMapping("/dealReport/{result}/{reportId}")
    public Result dealReport(@PathVariable Long reportId,@PathVariable Short result) {
        if (result!=1 && result!=2) {
            return Result.error(Constants.CODE_400,"result:1删除|2保留");
        }
        if (userReportMapper.selectById(reportId)==null) {
            return Result.error(Constants.CODE_400,"不存在该举报记录");
        }

        UserReport userReport = userReportMapper.selectById(reportId);
        Short sort = userReport.getSort();      // 0用户|1帖子|2回答|3评论|4文档|5视频
        Long targetId = userReport.getTarget_id();
        userMapper.dealReport(reportId,sort);
        if (result==1 && sort==0) {
            userService.banUser(targetId);
        } else if (result==1 && sort==1) {
            postsService.delPost(targetId);
        } else if (result==1 && sort==2) {
            commentsService.delPostComments(targetId);
        } else if (result==1 && sort==3) {
            commentsService.delCComments(targetId);
        } else if (result==1 && sort==4) {
            documentService.delDoc(targetId);
        } else if (result==1 && sort==5) {
            videoService.delVideo(targetId);
        }


        return Result.success();
    }




    @ApiOperation("解封用户")
    @GetMapping("/unbanUser/{userId}")
    public Result unbanUser(@PathVariable Long userId) {
        if (userMapper.getByID(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        userService.unbanUser(userId);
        return Result.success();
    }

    @ApiOperation("解封帖子")
    @GetMapping("/unbanPost/{postId}")
    public Result unbanPost(@PathVariable Long postId) {
        postsService.releasePost(postId);
        return Result.success();
    }

    @ApiOperation("取消封禁回答")
    @GetMapping("/unbanAnswer/{pcId}")
    public Result unbanPc(@PathVariable Long pcId) {
        commentsService.releasePc(pcId);
        return Result.success();
    }

    @ApiOperation("取消封禁某条评论")
    @GetMapping("/unbanComment/{id}")
    public Result unbanCc(@PathVariable Long id) {
        commentsService.releaseCc(id);
        return Result.success();
    }

    @ApiOperation("取消封禁文档")
    @GetMapping("/unbanDoc/{docId}")
    public Result unbanDoc(@PathVariable Long docId) {
        documentService.releaseDoc(docId);
        return Result.success();
    }

    @ApiOperation("取消封禁视频")
    @GetMapping("/unbanVideo/{videoId}")
    public Result unbanVideo(@PathVariable Long videoId) {
        videoService.releaseVideo(videoId);
        return Result.success();
    }

    @ApiOperation("封禁用户列表")
    @GetMapping("/userList/{pageNum}/{pageSize}")
    public Result getUserList(@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"从第一页开始");
        }
        List<BanUserVO> banUserList = userService.getBanUserList(pageNum, pageSize);

        return Result.success(banUserList,userMapper.getAllCount());
    }

    @ApiOperation("封禁用户")
    @DeleteMapping("/banUser/{userId}")
    public Result banUser(@PathVariable Long userId) {
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"该用户不存在或已封禁");
        }
        userService.banUser(userId);
        messageService.sendReportMsg(userId,null);
        return Result.banUserSuccess();
    }





}
