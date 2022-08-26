package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.controller.VO.PostVO;
import com.lyd.controller.VO.PrePostVO;
import com.lyd.entity.*;
import com.lyd.mapper.*;
import com.lyd.utils.RequestUtil;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
     * @desc    post转VO
     * @param src
     * @return
     */
    public List<PostVO> post2Vo(List<Posts> src) {
        ArrayList<PostVO> res = new ArrayList<>();
        int i = 1;
        for (Posts post : src) {
            PostVO postVO = new PostVO();
            postVO.setRank(i);
            i++;
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            UserInfo userInfo = userInfoMapper.selectById(post.getUser_id());
            if (userInfo!=null) {
                postVO.setUserName(userInfo.getNickname());
                postVO.setUserHead(userInfo.getHead());
            } else {
                postVO.setUserName("该用户已被封禁");
            }

            postVO.setDiscussNum(post.getDiscuss_num().toString());

            res.add(postVO);
        }
        return res;
    }

    /**
     * @desc    post转preVO
     * @param src
     * @param userId    用户id
     * @return
     */
    public List<PrePostVO> post2PreVo(List<Posts> src,Long userId) {
        ArrayList<PrePostVO> res = new ArrayList<>();
        int i = 1;
        for (Posts post : src) {
            PrePostVO postVO = new PrePostVO();
            postVO.setRank(i);
            i++;
            postVO.setPostId(post.getId().toString());
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            postVO.setCollectNum(userService.getCollectNum(post.getId(),(short)1).toString());
            UserInfo userInfo = userInfoMapper.selectById(post.getUser_id());
            if (userInfo!=null) {
                postVO.setUserName(userInfo.getNickname());
                postVO.setUserHead(userInfo.getHead());
            } else {
                postVO.setUserName("该用户已被封禁");
            }
            postVO.setDiscussNum(post.getDiscuss_num().toString());
            CommentsVO answer = commentsService.getBestCommentByPostId(post.getId());
            if (answer!=null) {
                postVO.setBestAnswer(answer.getContent());
                postVO.setBestAnswerId(answer.getCommentId());
                postVO.setLikes(answer.getLikes());
                if (userId!=null) {
                    postVO.setBestAnswerIsLiked(userService.isLike(userId, Long.valueOf(answer.getCommentId())));
                }
            } else {
                postVO.setBestAnswer("暂时没有回答");
                postVO.setLikes(0);
                postVO.setBestAnswerIsLiked(false);
            }
            QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
            collectionWrapper.eq("sort",1);
            collectionWrapper.eq("target_id",post.getId());
            if (userId!=null) {
                collectionWrapper.eq("user_id",userId);
                List<UserCollection> userCollections = userCollectionMapper.selectList(collectionWrapper);
                postVO.setIsCollected(!userCollections.isEmpty());
            } else {
                postVO.setBestAnswerIsLiked(null);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(post.getGmt_modified());
            postVO.setDate(format);
            res.add(postVO);
        }
        return res;
    }


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
        return post2Vo(posts);
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
        return post2Vo(posts);
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
        return post2PreVo(posts,userId);
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
        if (userInfo!=null) {
            postVO.setUserName(userInfo.getNickname());
            postVO.setUserHead(userInfo.getHead());
        } else {
            postVO.setUserName("该用户已被封禁");
        }

        postVO.setIsCollected(userService.isCollected(userId,postId,(short)1));

        postVO.setCollectNum(userService.getCollectNum(postId,(short)1).toString());

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
    public List<PrePostVO> search(String content,Long userId,Integer pageNum,Integer pageSize) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.like("title",content);
        wrapper.orderByDesc("discuss_num");
        wrapper.last(" limit "+ (pageNum-1)*pageSize +","+pageSize);
        List<Posts> posts = postsMapper.selectList(wrapper);
        ArrayList<PrePostVO> res = new ArrayList<>();
        return post2PreVo(posts,userId);
    }

    /**
     * @desc    获取模糊查询结果个数
     * @param content   模糊查询词
     * @return
     */
    public Long getSearchCount(String content) {
        QueryWrapper<Posts> wrapper = new QueryWrapper<>();
        wrapper.like("title",content);
        return postsMapper.selectCount(wrapper);

    }

    /**
     * @desc    通过py接口查询推荐帖子
     * @param userId    用户id
     * @param pageNum   第?页
     * @param pageSize  一页?条
     * @return
     * @throws Exception
     */
    public List<PrePostVO> getRecByUrl(Long userId,Integer pageNum,Integer pageSize) throws Exception {
        ArrayList<PrePostVO> res = null;
        List<String> ids = RequestUtil.getRecIdsByRequestUrl(userId);
        log.info(ids.toString());
        res = new ArrayList<>();
//            int i = 1;
        for (int i = (pageNum-1)*pageSize+1; i<= ids.size() && i <= pageNum*pageSize; i++) {
            String id = ids.get(i-1);
            PrePostVO postVO = new PrePostVO();
            postVO.setPostId(id);
            Posts post = postsMapper.selectById(id);
            postVO.setTitle(post.getTitle());
            postVO.setContent(post.getContent());
            postVO.setUserId(post.getUser_id().toString());
            postVO.setCollectNum(userService.getCollectNum(Long.valueOf(id),(short)1).toString());
            UserInfo userInfo = userInfoMapper.selectById(post.getUser_id());
            if (userInfo!=null) {
                postVO.setUserName(userInfo.getNickname());
                postVO.setUserHead(userInfo.getHead());
            } else {
                postVO.setUserName("该用户已被封禁");
            }
            postVO.setDiscussNum(post.getDiscuss_num().toString());
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(post.getGmt_modified());
            postVO.setDate(format);
            res.add(postVO);
        }
        return res;
    }


}
