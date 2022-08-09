package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.MsgVO;
import com.lyd.entity.Message;
import com.lyd.entity.UserInfo;
import com.lyd.mapper.MessageMapper;
import com.lyd.mapper.UserInfoMapper;
import com.lyd.mapper.UserMapper;
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

    public void sendMsg(Long fromId,Long toId,String msg) {
        Message message = new Message();
        message.setFrom_id(fromId);
        message.setTo_id(toId);
        message.setMsg(msg);
        messageMapper.insert(message);
    }

    public List<MsgVO> getMsg(Long userId,Long from_id,Integer pageNum,Integer pageSize) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",userId);
        wrapper.eq("from_id",from_id);
        wrapper.orderByDesc("gmt_modified");
        wrapper.last(" limit "+(pageNum-1)*pageSize+" , "+pageSize);
        List<Message> messages = messageMapper.selectList(wrapper);
        ArrayList<MsgVO> res = new ArrayList<>();
        for (Message message : messages) {
            MsgVO msgVO = new MsgVO();
            msgVO.setFromId(message.getFrom_id().toString());
            if (from_id!=0L) {
                UserInfo from = userInfoMapper.selectById(message.getFrom_id());
                msgVO.setFromHead(from.getHead());
                msgVO.setFromName(from.getNickname());
            }
            msgVO.setToId(message.getTo_id().toString());
            UserInfo to = userInfoMapper.selectById(message.getTo_id());
            msgVO.setToHead(to.getHead());
            msgVO.setToName(to.getNickname());
            msgVO.setMsg(message.getMsg());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
            msgVO.setDate(sdf.format(message.getGmt_modified()));

            res.add(msgVO);
        }
        return res;
    }

    public Long getMsgCount(Long userId,Long from_id) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("to_id",userId);
        wrapper.eq("from_id",from_id);
        return messageMapper.selectCount(wrapper);
    }


}
