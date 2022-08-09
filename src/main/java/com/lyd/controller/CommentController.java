package com.lyd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.CCVO;
import com.lyd.entity.*;
import com.lyd.mapper.CommentCommentsMapper;
import com.lyd.mapper.PostCommentsMapper;
import com.lyd.mapper.PostsMapper;
import com.lyd.mapper.UserMapper;
import com.lyd.service.CommentsService;
import com.lyd.service.PostsService;
import com.lyd.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 天狗
 * @date  2022/7/22
 */
@Slf4j
@ResponseBody
@RestController
@RequestMapping("/comment")
@CrossOrigin(origins ="*")
@Api("帖子回答的评论接口")
public class CommentController {

    @Autowired
    private CommentsService commentsService;
    @Resource
    private CommentCommentsMapper ccMapper;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private PostsMapper postsMapper;
    @Resource
    private UserMapper userMapper;
    @Autowired
    private PostsService postsService;


    @ApiOperation("分页获取某回答下评论")
    @GetMapping("/answer/{answerId}/{pageNum}/{pageSize}")
    public Result getCommentByAnswerId(@PathVariable Long answerId, @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        log.info("访问了/comment/answer/"+answerId+"/"+pageNum+"/"+pageSize+"接口");
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"页码从第1页开始");
        }
        if (postCommentsMapper.selectById(answerId)==null) {
            return Result.error(Constants.CODE_400,"该回答不存在");
        }
        List<CCVO> ccvos = commentsService.getCcByCommentId(answerId,pageNum,pageSize);
        return Result.success(ccvos);
    }

    @ApiOperation("获取某评论信息")
    @GetMapping("/{id}")
    public Result getCommentById(@PathVariable Long id) {
        log.info("访问了/comment/"+id+"接口");
        CCVO ccById = commentsService.getCcById(id);

        return Result.success(ccById);
    }

    @ApiOperation("修改回答的评论")
    @GetMapping("/upd/{id}/{content}")
    public Result updCc(@PathVariable Long id, @PathVariable String content) {
        log.info("访问了/comment/upd/"+id+"/"+content+"接口");
        CCVO ccvo = commentsService.updCc(id, content);
        return Result.success(ccvo);
    }

    @ApiOperation("删除某条评论")
    @DeleteMapping("/del/{id}")
    public Result delCc(@PathVariable Long id) {
        log.info("访问了/comment/del/"+id+"接口");
        commentsService.delCComments(id);
        return Result.success();
    }

    @ApiOperation("回答帖子")
    @PostMapping("/{postId}/{userId}/{content}")
    public Result commentPost(@PathVariable Long postId,@PathVariable Long userId,@PathVariable String content) {
        User user = userMapper.selectById(userId);
        if (user==null) {
            return Result.error(Constants.CODE_400,"该用户不存在");
        }
        Posts post = postsMapper.selectById(postId);
        if (post==null) {
            return Result.error(Constants.CODE_400,"该帖子不存在");
        }

        commentsService.newComment(userId,content,postId,null,null);
        return Result.success();
    }

    @ApiOperation("评论回答")
    @PostMapping("/{postId}/{answerId}/{userId}/{content}")
    public Result commentAnswer(@PathVariable Long postId,@PathVariable Long answerId,
                                @PathVariable Long userId,@PathVariable String content) {
        User user = userMapper.selectById(userId);
        if (user==null) {
            return Result.error(Constants.CODE_400,"该用户不存在");
        }
        Posts post = postsMapper.selectById(postId);
        if (post==null) {
            return Result.error(Constants.CODE_400,"该帖子不存在");
        } else {
            PostComments answer = postCommentsMapper.selectById(answerId);
            if (answer==null) {
                return Result.error(Constants.CODE_400,"该回答不存在");
            }
        }

        commentsService.newComment(userId,content,postId,answerId,null);
        return Result.success();
    }

    @ApiOperation("评论他人评论")
    @PostMapping("/{postId}/{answerId}/{commentId}/{userId}/{content}")
    public Result commentComment(@PathVariable Long postId,@PathVariable Long answerId,@PathVariable Long commentId,
                                 @PathVariable Long userId,@PathVariable String content) {
        User user = userMapper.selectById(userId);
        if (user==null) {
            return Result.error(Constants.CODE_400,"该用户不存在");
        }
        Posts post = postsMapper.selectById(postId);
        if (post==null) {
            return Result.error(Constants.CODE_400,"该帖子不存在");
        } else {
            PostComments answer = postCommentsMapper.selectById(answerId);
            if (answer==null) {
                return Result.error(Constants.CODE_400,"该回答不存在");
            } else {
                CommentComments cc = ccMapper.selectById(commentId);
                if (cc==null) {
                    return Result.error(Constants.CODE_400,"该评论不存在");
                }
            }
        }

        commentsService.newComment(userId,content,postId,answerId,commentId);
        return Result.success();
    }


}
