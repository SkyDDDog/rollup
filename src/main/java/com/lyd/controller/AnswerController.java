package com.lyd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.entity.PostComments;
import com.lyd.entity.Posts;
import com.lyd.mapper.PostCommentsMapper;
import com.lyd.mapper.PostsMapper;
import com.lyd.service.CommentsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.python.antlr.ast.Return;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;

/**
 * @author 天狗
 * @date 2022/7/22
 */
@Slf4j
@ResponseBody
@RestController
@RequestMapping("/answer")
@CrossOrigin(origins ="*")
@Api("帖子的回答接口")
public class AnswerController {

    @Autowired
    private CommentsService commentsService;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private PostsMapper postsMapper;


    @ApiOperation("获取某帖子所有回答")
    @GetMapping("/post/{postId}/{pageNum}/{pageSize}")
//    @PermitAll
    public Result getAnswerByPostId(@PathVariable Long postId,@RequestParam(required = false) Long userId,
                                    @PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/answer/post/"+postId+"/"+pageNum+"/"+pageSize+"接口");
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"页码从第1页开始");
        }
        Posts post = postsMapper.selectById(postId);
        if (post==null) {
            return Result.error(Constants.CODE_400,"不存在该帖子");
        }
        List<CommentsVO> comments = commentsService.getCommentsByPostId(userId,postId,pageNum,pageSize);

        return Result.success(comments,commentsService.getCommentsCountByPostId(postId));
    }

    @ApiOperation("获取某问答信息")
    @GetMapping("/{id}")
    public Result getAnswerByCommentId(@PathVariable Long id,@RequestParam(required = false)Long userId) {
        log.info("访问了/answer/"+id+"接口");
        CommentsVO commentById = commentsService.getCommentById(id,userId);
        return Result.success(commentById);
    }

    @ApiOperation("修改回答内容")
    @GetMapping("/upd/{id}/{content}")
    public Result updComment(@PathVariable Long id,@PathVariable String content) {
        PostComments comments = postCommentsMapper.selectById(id);
        if (comments==null) {
            return Result.error(Constants.CODE_400,"不存在该回答");
        }
        CommentsVO commentsVO = commentsService.updComment(id, content);
        return Result.success(commentsVO);
    }

    @ApiOperation("删除某回答")
    @DeleteMapping("/del/{id}")
    public Result delComment(@PathVariable Long id) {
        log.info("访问了/answer/del/"+id+"接口");
        PostComments comments = postCommentsMapper.selectById(id);
        if (comments==null) {
            return Result.error(Constants.CODE_400,"不存在该回答");
        }
        commentsService.delPostComments(id);
        return Result.success();
    }



}
