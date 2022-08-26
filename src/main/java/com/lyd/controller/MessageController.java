package com.lyd.controller;

import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.MsgVO;
import com.lyd.mapper.UserMapper;
import com.lyd.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@ResponseBody
@RestController
@Controller
@RequestMapping("/msg")
@CrossOrigin(origins ="*")
@Api("消息接口")
public class MessageController {

    final Long SYSID = 0L;

    @Autowired
    private MessageService messageService;

    @Resource
    private UserMapper userMapper;

    @ApiOperation("给某用户推送websocket消息")
    @GetMapping("/push/{toUserId}")
    public Result pushToWeb(String message, @PathVariable String toUserId) throws IOException {
        WebSocketServer.sendInfo(message,toUserId);
        return Result.success();
    }

    @ApiOperation("发送系统消息")
    @PostMapping("/sendSysMsg/{userId}/{msg}")
    public Result sendSysMsg(@PathVariable Long userId, @PathVariable String msg,@RequestParam(required = false) String title) {
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        messageService.sendMsg(SYSID,userId,msg,title);
        return Result.success();
    }

    @ApiOperation("发送私聊消息")
    @PostMapping("/sendMsg/{fromId}/{toId}/{msg}")
    public Result sendPrivateMsg(@PathVariable Long fromId,@PathVariable Long toId,@PathVariable String msg) {
        if (userMapper.selectById(fromId)==null || userMapper.selectById(toId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        messageService.sendMsg(fromId,toId,msg,null);
        return Result.success();
    }

    @ApiOperation("获取系统消息")
    @GetMapping("/getSysMsg/{userId}")
    public Result getSysMsg(@PathVariable Long userId) {
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        List<MsgVO> msg = messageService.getMsg(userId, SYSID);
        return Result.success(msg,messageService.getMsgCount(userId, SYSID));
    }

    @ApiOperation("获取私聊消息")
    @GetMapping("/getMsg/{fromId}/{toId}")
    public Result getPrivateMsg(@PathVariable Long fromId,@PathVariable Long toId) {
        if (userMapper.selectById(fromId)==null || userMapper.selectById(toId)==null) {
            return Result.error(Constants.CODE_400,"不存在该用户");
        }
        List<MsgVO> msg = messageService.getMsg(toId, fromId);
        return Result.success(msg,messageService.getMsgCount(toId,fromId));
    }


}
