package com.lyd.controller;

import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.*;
import com.lyd.entity.Posts;
import com.lyd.mapper.PostsMapper;
import com.lyd.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/posts")
@CrossOrigin(origins ="*")
@Api("帖子接口")
public class PostController {

    @Autowired
    private PostsService postsService;
    @Autowired
    private CommentsService commentsService;
    @Resource
    private PostsMapper postsMapper;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserService userService;
    @Autowired
    private VideoService videoService;

    @ApiOperation("发布帖子")
    @PostMapping("/{userId}/{title}")
    public Result newPost(@PathVariable Long userId,@PathVariable String title,@RequestParam(required = false) String content) {
        postsService.newPost(title,userId,content);
        return Result.success();
    }

    @ApiOperation("封禁帖子")
    @DeleteMapping("/ban/{postId}")
    public Result banPost(@PathVariable Long postId) {
        userService.delReport(postId,(short)2);
        postsService.delPost(postId);
        return Result.success();
    }

    @ApiOperation("解封帖子")
    @GetMapping("/unban/{postId}")
    public Result unbanPost(@PathVariable Long postId) {
        postsService.releasePost(postId);
        return Result.success();
    }


    @ApiOperation("分页查询所有(按讨论数倒序)")
    @GetMapping("/all/{pageNum}/{pageSize}")
    public Result getAllPosts(@PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        if (pageNum < 1 ) {
            return Result.error(Constants.CODE_400,"页码数应为正整数");
        }
        log.info("访问了/posts/all/"+pageNum+"/"+pageSize+"接口");
        List<PostVO> all = postsService.getAllPosts(pageNum, pageSize);

        return Result.success(all, postsService.getCount());
    }

    @ApiOperation("查询最高赞回答")
    @GetMapping("/best/{postId}")
    public Result getBestComment(@PathVariable Long postId) {
        log.info("访问了/posts/best/"+postId+"接口");
        if (postsMapper.selectById(postId)==null) {
            return Result.error(Constants.CODE_400,"不存在该帖子");
        }
        CommentsVO best = commentsService.getBestCommentByPostId(postId);
        return Result.success(best);
    }

    @ApiOperation("近期热门前五篇帖子")
    @GetMapping("/hot")
    public Result getHots() {
        log.info("访问了/posts/hot接口");
        List<PostVO> hots = postsService.getHots();
        return Result.success(hots,5L);
    }

    @ApiOperation("首页(sort:1热门|2最新回复|3最新发帖)")
    @GetMapping("/{sort}/{pageNum}/{pageSize}")
    public Result getBySort(@PathVariable Short sort,@RequestParam(required = false)Long userId,
                               @PathVariable Integer pageNum,@PathVariable Integer pageSize) {
        log.info("访问了/posts/rec/"+pageNum+"/"+pageSize);
        if (sort!=1 && sort!=2 && sort!=3) {
            return Result.error(Constants.CODE_400,"sort:1热门|2最新回复|3最新发帖");
        }
        if (pageNum < 1) {
            return Result.error(Constants.CODE_400,"从第1页开始");
        }
        List<PrePostVO> recommend = postsService.getBySort(userId,sort,pageNum, pageSize);
        return Result.success(recommend,postsService.getCount());
    }


    @ApiOperation("根据帖子id得到帖子信息")
    @GetMapping("/{postId}")
    public Result getById(@PathVariable Long postId,@RequestParam(required = false) Long userId) {
        log.info("访问了/posts/"+postId+"接口");
        PostVO p = postsService.getById(postId,userId);
        if (userId!=null) {
            log.info("产生帖子{}的历史记录",postId);
            userService.addHistory(userId,postId,(short)1);
        }

        return Result.success(p);
    }

    @ApiOperation("搜索(sort:1帖子|2资料|3视频)")
    @GetMapping("/search/{sort}/{pageNum}/{pageSize}/{content}")
    public Result search(@PathVariable String content,
                         @PathVariable @ApiParam(value = "1帖子/2资料",defaultValue = "1") Short sort,
                         @PathVariable Integer pageNum, @PathVariable Integer pageSize) {
        log.info("访问了/posts/search/"+sort+"/"+pageNum+"/"+pageSize+"接口");
        log.info("查询了"+content);
        List<PostVO> searchPost;
        List<DocVO> searchDoc;
        List<VideoVO> searchVideo;
        if (pageNum<1) {
            return Result.error(Constants.CODE_400,"应从第1页开始查询");
        }
        if (sort==1) {
            searchPost = postsService.search(content, pageNum, pageSize);
            return Result.success(searchPost, postsService.getSearchCount(content));
        } else if (sort==2) {
            searchDoc = documentService.search(content,pageNum,pageSize);
            return Result.success(searchDoc,documentService.getSearchCount(content));
        } else if (sort==3) {
            searchVideo = videoService.search(content,pageNum,pageSize);
            return Result.success(searchVideo,videoService.getSearchCount(content));
        } else {
            return Result.error(Constants.CODE_400,"sort:1帖子|2资料|3视频");
        }
    }

    @ApiOperation("修改帖子内容")
    @PostMapping("/updPost/{postId}")
    public Result updatePost(@PathVariable Long postId,@RequestParam String title,@RequestParam String content) {
        log.info("访问了/post/updPost/{}接口",postId);
        if (postsMapper.selectById(postId)==null) {
            return Result.error(Constants.CODE_400,"不存在该帖子");
        }
        postsService.updPost(postId,title,content);
        return Result.success();
    }


}
