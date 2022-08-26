package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.CCVO;
import com.lyd.controller.VO.CommentsVO;
import com.lyd.entity.*;
import com.lyd.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;
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
public class CommentsService {

    @Resource
    private CommentCommentsMapper commentCommentsMapper;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private PostsMapper postsMapper;
    @Autowired
    private UserService userService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserCollectionMapper userCollectionMapper;



    /**
     * @desc 评论
     * @param postId
     * @param answerId
     * @param commentId
     * @param userId
     * @param content
     */
    public void newComment(Long userId,String content,Long postId,Long answerId,Long commentId) {
        if (commentId==null) {
            if (answerId==null) {
                // 回答帖子
                PostComments postComment = new PostComments();
                postComment.setPost_id(postId);
                postComment.setContent(content);
                postComment.setUser_id(userId);
                postCommentsMapper.insert(postComment);
            } else {
                // 评论回答
                CommentComments cc = new CommentComments();
                cc.setComment_id(answerId);
                cc.setContent(content);
                cc.setFrom_id(userId);
                cc.setTo_id(null);
                commentCommentsMapper.insert(cc);
            }
        } else {
            // 评论别人的评论
            CommentComments cc = new CommentComments();
            cc.setComment_id(commentId);
            cc.setContent(content);
            Long from_id = commentCommentsMapper.selectById(commentId).getFrom_id();
            cc.setFrom_id(from_id);
            cc.setTo_id(userId);
            commentCommentsMapper.insert(cc);
        }

        // 帖子讨论数+1
        Posts post = postsMapper.selectById(postId);
        post.setDiscuss_num(post.getDiscuss_num()+1);
        postsMapper.updateById(post);
    }

    /**
     * @desc 根据回答id删除某个帖子的某个回答
     * @param commentId 回答id
     */
    public void delPostComments(Long commentId) {
        // 帖子讨论数-1
        Long postId = postCommentsMapper.selectById(commentId).getPost_id();
        Posts post = postsMapper.selectById(postId);
        post.setDiscuss_num(post.getDiscuss_num()-1);
        postsMapper.updateById(post);
        // 删除cc记录
        QueryWrapper<CommentComments> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id",commentId);
        commentCommentsMapper.delete(wrapper);
        // 删除pc记录
        postCommentsMapper.deleteById(commentId);
    }

    public void releasePc(Long pcId) {
        postCommentsMapper.unbanPcById(pcId);
    }

    /**
     * @desc 根据问答的评论id来删除某个帖子下某个回答的评论
     * @param ccId 评论id
     */
    public void delCComments(Long ccId) {
        // 帖子讨论数-1
        Long commentId = commentCommentsMapper.selectById(ccId).getComment_id();
        Long postId = postCommentsMapper.selectById(commentId).getPost_id();
        Posts post = postsMapper.selectById(postId);
        post.setDiscuss_num(post.getDiscuss_num()-1);
        postsMapper.updateById(post);
        // 删除cc记录
        commentCommentsMapper.deleteById(ccId);
    }

    public void releaseCc(Long ccId) {
        commentCommentsMapper.unbanCcById(ccId);
    }

    /**
     * @desc 更新帖子的回答
     * @param id    回答id
     * @param content   更新后内容
     */
    public CommentsVO updComment(Long id,String content) {
        PostComments postComment = new PostComments();
        postComment.setId(id);
        postComment.setContent(content);
        postCommentsMapper.updateById(postComment);

        return getCommentById(id,null);
    }

    /**
     * @desc 更新帖子的问答的评论
     * @param id    评论id
     * @param content   更新后内容
     */
    public CCVO updCc(Long id,String content) {
        CommentComments cc = new CommentComments();
        cc.setId(id);
        cc.setContent(content);
        commentCommentsMapper.updateById(cc);

        return getCcById(id);
    }

    /**
     * @desc 获取某回答下所有评论
     * @param commentId
     * @return
     */
    public List<CCVO> getAllCcByCommentId(Long commentId) {
        QueryWrapper<CommentComments> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id",commentId);
        wrapper.orderByDesc("likes");
        List<CommentComments> ccs = commentCommentsMapper.selectList(wrapper);
        ArrayList<CCVO> res = new ArrayList<>();
        for (CommentComments cc : ccs) {
            CCVO ccvo = new CCVO();
            ccvo.setCcId(cc.getId().toString());
            ccvo.setCommentId(cc.getComment_id().toString());
            ccvo.setContent(cc.getContent());
            ccvo.setFromId(cc.getFrom_id().toString());
            ccvo.setToId(cc.getTo_id().toString());
//            ccvo.setLikes(cc.getLikes());

            res.add(ccvo);
        }
        return res;
    }

    /**
     * @desc 通过回答id获取该回答的所有评论
     * @param commentId 回答id
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return  评论List
     */
    public List<CCVO> getCcByCommentId(Long commentId,Integer pageNum,Integer pageSize) {
        QueryWrapper<CommentComments> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id",commentId);
//        wrapper.orderByDesc("likes");
        wrapper.last(" limit "+(pageNum-1)*pageSize+","+pageSize);
        List<CommentComments> ccs = commentCommentsMapper.selectList(wrapper);
        ArrayList<CCVO> res = new ArrayList<>();
        for (CommentComments cc : ccs) {
            CCVO ccvo = new CCVO();
            ccvo.setCcId(cc.getId().toString());
            ccvo.setCommentId(cc.getComment_id().toString());
            ccvo.setContent(cc.getContent());
            ccvo.setFromId(cc.getFrom_id().toString());
            if (cc.getTo_id()!=null) {
                ccvo.setToId(cc.getTo_id().toString());
            }
            UserInfo userInfo = userInfoMapper.selectById(cc.getFrom_id());
            ccvo.setUserhead(userInfo.getHead());
            ccvo.setUsername(userInfo.getNickname());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            ccvo.setDate(sdf.format(cc.getGmt_modified()));

//            ccvo.setLikes(cc.getLikes());

            res.add(ccvo);
        }
        return res;
    }

    /**
     * @desc 获取某回答下评论数
     * @param commentId 回答id
     * @return
     */
    public Long getCcCount(Long commentId) {
        QueryWrapper<CommentComments> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id",commentId);
        return commentCommentsMapper.selectCount(wrapper);
    }

    /**
     * @desc    分页获取某帖子所有回答
     * @param postId    帖子id
     * @param pageNum   分页参数
     * @param pageSize  分页参数
     * @return  帖子所有回答
     */
    public List<CommentsVO> getCommentsByPostId(Long userId,Long postId,Integer pageNum,Integer pageSize) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id",postId);
        wrapper.orderByDesc("likes");
        wrapper.last(" limit " + (pageNum-1)*pageSize + "," + pageSize);
        List<PostComments> pcs = postCommentsMapper.selectList(wrapper);
        ArrayList<CommentsVO> res = new ArrayList<>();
        for (PostComments pc : pcs) {
            CommentsVO commentsVO = new CommentsVO();
            commentsVO.setCommentId(pc.getId().toString());
            commentsVO.setPostId(pc.getPost_id().toString());
            QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
            collectionWrapper.eq("target_id",pc.getId());
            collectionWrapper.eq("sort",1);
            String collectNum = userCollectionMapper.selectCount(collectionWrapper).toString();
            commentsVO.setCollectNum(collectNum);
            commentsVO.setContent(pc.getContent());
            commentsVO.setUserId(pc.getUser_id().toString());
            commentsVO.setLikes(pc.getLikes());

            commentsVO.setCommentNum(getCcCountByCommentId(pc.getId()).toString());


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            commentsVO.setDate(sdf.format(pc.getGmt_modified()));
            UserInfo userInfo = userInfoMapper.selectById(pc.getUser_id());
            if (userInfo!=null) {
                commentsVO.setUsername(userInfo.getNickname());
                commentsVO.setUserhead(userInfo.getHead());
                commentsVO.setSignature(userInfo.getSignature());
            }
            if (userId!=null) {
                commentsVO.setIsLiked(userService.isLike(userId, pc.getId()));
                commentsVO.setIsCollected(userService.isCollected(userId,pc.getId(), (short) 2));
            }

            res.add(commentsVO);
        }
        return res;
    }

    /**
     * @desc    获取某帖子最高赞回答
     * @param postId    帖子id
     * @return  CommentsVO
     */
    public CommentsVO getBestCommentByPostId(Long postId) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id",postId);
        wrapper.orderByDesc("likes");
        wrapper.last(" limit 1");
        PostComments pc = postCommentsMapper.selectOne(wrapper);
        if (pc==null) {
            return null;
        }
        CommentsVO res = new CommentsVO();
        res.setCommentId(pc.getId().toString());
        res.setPostId(pc.getPost_id().toString());
        res.setContent(pc.getContent());
        res.setUserId(pc.getUser_id().toString());
        res.setLikes(pc.getLikes());

        return res;
    }


    /**
     * @desc    通过回答id获取回答信息
     * @param commentId 回答id
     * @return  CommentVO
     */
    public CommentsVO getCommentById(Long commentId,Long userId) {
        PostComments postComments = postCommentsMapper.selectById(commentId);
        CommentsVO res = new CommentsVO();
        res.setCommentId(commentId.toString());
        res.setPostId(postComments.getPost_id().toString());
        res.setContent(postComments.getContent());
        res.setUserId(postComments.getUser_id().toString());
        res.setLikes(postComments.getLikes());

        QueryWrapper<UserCollection> collectionWrapper = new QueryWrapper<>();
        collectionWrapper.eq("target_id",commentId);
        collectionWrapper.eq("sort",1);
        String collectNum = userCollectionMapper.selectCount(collectionWrapper).toString();
        res.setCollectNum(collectNum);

        UserInfo userInfo = userInfoMapper.selectById(postComments.getUser_id());
        res.setUsername(userInfo.getNickname());
        res.setUserhead(userInfo.getHead());
        res.setSignature(userInfo.getSignature());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        res.setDate(sdf.format(postComments.getGmt_modified()));

        res.setCommentNum(getCcCountByCommentId(commentId).toString());

        if (userId!=null) {
            res.setIsLiked(userService.isLike(userId, commentId));
            res.setIsCollected(userService.isCollected(userId,commentId, (short) 2));
        }

//        List<CCVO> ccs = getAllCcByCommentId(commentId);
//        res.setCommentComments(ccs);

        return res;
    }

    /**
     * @desc 通过回答的评论的id得到评论信息
     * @param ccId  评论id
     * @return  CCVO
     */
    public CCVO getCcById(Long ccId) {
        CommentComments cc = commentCommentsMapper.selectById(ccId);
        CCVO res = new CCVO();
        res.setCcId(cc.getId().toString());
        res.setCommentId(cc.getComment_id().toString());
        res.setContent(cc.getContent());
        res.setFromId(cc.getFrom_id().toString());
        res.setToId(cc.getTo_id().toString());

        return res;
    }

    /**
     * @desc 为某回答点赞
     * @param commentId 回答id
     */
    public void likeComment(Long commentId) {
        PostComments comment = postCommentsMapper.selectById(commentId);
        comment.setLikes(comment.getLikes()+1);

        postCommentsMapper.updateById(comment);
    }

    /**
     * @desc    获取某帖子下回答数量
     * @param postId    帖子id
     * @return  Long
     */
    public Long getCommentsCountByPostId(Long postId) {
        QueryWrapper<PostComments> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id",postId);
        return postCommentsMapper.selectCount(wrapper);
    }

    /**
     * @desc    获取某回答下评论数量
     * @param commentID    帖子id
     * @return  Long
     */
    public Long getCcCountByCommentId(Long commentID) {
        QueryWrapper<CommentComments> wrapper = new QueryWrapper<>();
        wrapper.eq("comment_id",commentID);
        return commentCommentsMapper.selectCount(wrapper);
    }

}
