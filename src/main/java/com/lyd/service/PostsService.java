package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.controller.VO.PostVO;
import com.lyd.controller.VO.PrePostVO;
import com.lyd.entity.*;
import com.lyd.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 天狗
 * @date 2022/7/20
 */
@Service
public class PostsService {

    @Resource
    private PostsMapper postsMapper;
    @Autowired
    private CommentsService commentsService;
    @Resource
    private UserCollectionMapper userCollectionMapper;
    @Resource
    private UserLikeMapper userLikeMapper;
    @Resource
    private UserReportMapper userReportMapper;
    @Autowired
    private UserService userService;
    @Resource
    private UserInfoMapper userInfoMapper;

    /**
     * @desc 发布帖子
     * @param title 帖子标题
     * @param userId    发布人id
     * @param content   帖子内容(可空)
     */
    public void newPost(String title,Long userId,String content) {
        Posts post = new Posts();
        post.setTitle(title);
        post.setContent(content);
        post.setUser_id(userId);

        postsMapper.insert(post);
    }

    /**
     * @desc 逻辑删除帖子
     * @param postId    帖子id
     */
    public void delPost(Long postId) {
        postsMapper.deleteById(postId);
    }

    /**
     * @desc 取消逻辑删除
     * @param postId    帖子id
     */
    public void releasePost(Long postId) {
        postsMapper.unbanPostById(postId);
    }

    /**
     * @desc    更新帖子
     * @param postId    帖子id
     * @param title     帖子新标题(可空)
     * @param content   帖子内容(可空)
     */
    public void updPost(Long postId,String title,String content) {
        Posts post = postsMapper.selectById(postId);
        post.setTitle(title);
        post.setContent(content);

        postsMapper.updateById(post);
    }

    /**
     * @desc 按讨论数排序分页查询所有帖子
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<PostVO> getAllPosts(Integer pageNum,Integer pageSize) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("discuss_num");
        wrapper.last(" limit " + (pageNum-1)*pageSize + "," + pageSize);
        List<Posts> posts = postsMapper.selectList(wrapper);
        ArrayList<PostVO> res = new ArrayList<>();
        for (Posts post : posts) {
            PostVO postVO = new PostVO();
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            postVO.setDiscussNum(post.getDiscuss_num().toString());

            res.add(postVO);
        }
        return res;
    }

    /**
     * @desc 得到讨论数最高的五篇帖子
     * @return  List
     */
    public List<PostVO> getHots() {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("discuss_num");
        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit 5");
        List<Posts> posts = postsMapper.selectList(wrapper);
        ArrayList<PostVO> res = new ArrayList<>();
        int i = 1;
        for (Posts post : posts) {
            PostVO postVO = new PostVO();
            postVO.setRank(i);
            i++;
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            postVO.setDiscussNum(post.getDiscuss_num().toString());

            res.add(postVO);
        }
        return res;
    }

    /**
     * @Desc
     * @param userId    用户id(用于判断是否已点赞或收藏)
     * @param sort      排序参数    1热门|2最新回复|3最新发帖
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<PrePostVO> getBySort(Long userId,Short sort, Integer pageNum, Integer pageSize) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        if (sort==1) {
            wrapper.orderByDesc("discuss_num");
        } else if (sort==2) {
            wrapper.orderByDesc("gmt_created");
        } else if (sort==3) {
            wrapper.orderByDesc("gmt_modified");
        }

        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<Posts> posts = postsMapper.selectList(wrapper);
        ArrayList<PrePostVO> res = new ArrayList<>();
        int i = 1;
        for (Posts post : posts) {
            PrePostVO postVO = new PrePostVO();
            postVO.setRank(i);
            i++;
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            postVO.setDiscussNum(post.getDiscuss_num());
            CommentsVO answer = commentsService.getBestCommentByPostId(post.getId());
            if (answer!=null) {
                postVO.setBestAnswer(answer.getContent());
                postVO.setBestAnswerId(answer.getCommentId());
                postVO.setLikes(answer.getLikes());

                if (userId!=null) {
                    postVO.setBestAnswerIsLiked(userService.isLike(userId, Long.valueOf(answer.getCommentId())));
                }
            }
            QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
            collectionWrapper.eq("sort",0);
            collectionWrapper.eq("target_id",post.getId());
            if (userId!=null) {
                collectionWrapper.eq("user_id",userId);
                List<UserCollection> userCollections = userCollectionMapper.selectList(collectionWrapper);
                postVO.setIsCollected(!userCollections.isEmpty());
            }

            res.add(postVO);
        }
        return res;
    }



    /**
     * @desc 获取所有帖子总数
     * @return  Long
     */
    public Long getCount() {
        return postsMapper.selectCount(null);
    }

    /**
     * @desc    通过帖子id获取帖子详情
     * @param postId    帖子id
     * @return  PostVO
     */
    public PostVO getById(Long postId,Long userId) {
        Posts post = postsMapper.selectById(postId);
        PostVO postVO = new PostVO();
        postVO.setPostId(post.getId().toString());
        postVO.setTitle(post.getTitle());
        postVO.setContent(post.getContent());
        postVO.setUserId(post.getUser_id().toString());
        postVO.setDiscussNum(post.getDiscuss_num().toString());

        UserInfo userInfo = userInfoMapper.selectById(post.getUser_id());
        postVO.setUserHead(userInfo.getHead());
        postVO.setUserName(userInfo.getNickname());

        QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
        collectionWrapper.eq("sort",0);
        collectionWrapper.eq("target_id",post.getId());
        if (userId!=null) {
            collectionWrapper.eq("user_id",userId);
            List<UserCollection> userCollections = userCollectionMapper.selectList(collectionWrapper);
            postVO.setIsCollected(!userCollections.isEmpty());
        }
        String collectNum = userCollectionMapper.selectCount(collectionWrapper).toString();
        postVO.setCollectNum(collectNum);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(post.getGmt_modified());
        postVO.setDate(format);

        return postVO;
    }

    /**
     * @desc    模糊查询帖子标题
     * @param content   查询词
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return
     */
    public List<PostVO> search(String content,Integer pageNum,Integer pageSize) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.like("content",content);
        wrapper.orderByDesc("discuss_num");
        wrapper.last(" limit "+ (pageNum-1)*pageSize +","+pageSize);
        List<Posts> posts = postsMapper.selectList(wrapper);
        ArrayList<PostVO> res = new ArrayList<>();
        for (Posts post : posts) {
            PostVO postVO = new PostVO();
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());

            res.add(postVO);
        }
        return res;
    }

    public Long getSearchCount(String content) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.like("content",content);
        return postsMapper.selectCount(wrapper);

    }


}
