package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.MsgVO;
import com.lyd.entity.*;
import com.lyd.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserReportMapper userReportMapper;
    @Resource
    private PostsMapper postsMapper;
    @Resource
    private PostCommentsMapper postCommentsMapper;
    @Resource
    private CommentCommentsMapper commentCommentsMapper;
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private VideoMapper videoMapper;


    /**
     * @desc    msg转VO
     * @param src   List<msc>
     * @return
     */
    public List<MsgVO> msg2Vo(List<Message> src) {
        ArrayList<MsgVO> res = new ArrayList<>();
        for (Message message : src) {
            MsgVO msgVO = new MsgVO();
            msgVO.setFromId(message.getFrom_id().toString());
            if (message.getFrom_id()!=0L) {
                UserInfo from = userInfoMapper.selectById(message.getFrom_id());
                msgVO.setFromHead(from.getHead());
                msgVO.setFromName(from.getNickname());
            }
            msgVO.setToId(message.getTo_id().toString());
            UserInfo to = userInfoMapper.selectById(message.getTo_id());
            msgVO.setToHead(to.getHead());
            msgVO.setToName(to.getNickname());
            msgVO.setMsg(message.getMsg());
            msgVO.setTitle(message.getTitle());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
            msgVO.setDate(sdf.format(message.getGmt_modified()));

            res.add(msgVO);
        }
        return res;
    }


    /**
     * @desc    发送消息
     * @param fromId    发送者id
     * @param toId      接收者id
     * @param msg       发送的消息
     * @param title     发送的标题
     */
    public void sendMsg(Long fromId,Long toId,String msg,String title) {
        Message message = new Message();
        message.setFrom_id(fromId);
        message.setTo_id(toId);
        message.setMsg(msg);
        messageMapper.insert(message);
    }


    /**
     * @desc    发送举报信息
     * @param toId  被举报人id
     * @param reportId  举报id
     */
    public void sendReportMsg(Long toId,Long reportId) {
        Message msg = new Message();
        msg.setFrom_id(0L);
        msg.setTo_id(toId);
        Short sort = 0;
        UserReport userReport = null;
        if (reportId!=null) {
            userReport = userReportMapper.selectById(reportId);
            sort = userReport.getSort();
        }
        // sort  0用户|1帖子|2回答|3评论|4文档|5视频
        if (sort==0) {
            msg.setTitle("您的账号已被封禁");
            msg.setMsg("违规次数过多，被管理员封禁");
        } else if (sort==1) {
            Posts post = postsMapper.selectById(userReport.getTarget_id());
            msg.setTitle("您的帖子已被删除");
            msg.setMsg("帖子【"+post.getTitle()+"】被管理员删除，" +
                    "被举报原因:"+userReport.getReason());
        } else if (sort==2) {
            PostComments comment = postCommentsMapper.selectById(userReport.getTarget_id());
            msg.setTitle("您的回答已被删除");
            msg.setMsg("回答【"+comment.getContent().substring(0,5)+"...】被管理员删除，" +
                    "被举报原因:"+userReport.getReason());
        } else if (sort==3) {
            CommentComments cc = commentCommentsMapper.selectById(userReport.getTarget_id());
            msg.setTitle("您的评论已被删除");
            msg.setMsg("评论【"+cc.getContent().substring(0,5)+"...】被管理员删除，" +
                    "被举报原因:"+userReport.getReason());
        } else if (sort==4) {
            Document document = documentMapper.selectById(userReport.getTarget_id());
            msg.setTitle("您的文档已被删除");
            msg.setMsg("文档【"+document.getName()+"】被管理员删除，" +
                    "被举报原因:"+userReport.getReason());
        } else if (sort==5) {
            Video video = videoMapper.selectById(userReport.getTarget_id());
            msg.setTitle("您的视频已被删除");
            msg.setMsg("视频【"+video.getName()+"】被管理员删除，" +
                    "被举报原因:"+userReport.getReason());
        }

        messageMapper.insert(msg);
    }

    /**
     * @desc    获取消息
     * @param userId    用户id
     * @param from_id   发送消息者id
     * @return
     */
    public List<MsgVO> getMsg(Long userId,Long from_id) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",userId);
        wrapper.eq("from_id",from_id);
        wrapper.orderByDesc("gmt_modified");
        List<Message> messages = messageMapper.selectList(wrapper);
        return msg2Vo(messages);
    }

    /**
     * @desc    获取消息个数
     * @param userId    用户id
     * @param from_id   发送消息者id
     * @return
     */
    public Long getMsgCount(Long userId,Long from_id) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",userId);
        wrapper.eq("from_id",from_id);
        return messageMapper.selectCount(wrapper);
    }

    /**
     * @desc    获取几条历史信息
     * @param toId  用户id
     * @param fromId    发送者id
     * @param num   获取数量
     * @return
     */
    public List<MsgVO> getHistoryMsg(Long toId,Long fromId,Integer num) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",toId)
                .eq("from_id",fromId)
                .orderByDesc("gmt_modified")
                .last(" limit "+num);
        List<Message> messages = messageMapper.selectList(wrapper);
        return msg2Vo(messages);
    }


}
