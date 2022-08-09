package com.lyd.controller;

import com.lyd.common.Constants;
import com.lyd.common.Result;
import com.lyd.controller.VO.TodoVO;
import com.lyd.entity.Todo;
import com.lyd.entity.User;
import com.lyd.mapper.TodoMapper;
import com.lyd.mapper.UserMapper;
import com.lyd.service.TodoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@ResponseBody
@RestController
@RequestMapping("/todo")
@CrossOrigin(origins ="*")
@Api("TodoList接口")
public class TodoController {

    @Autowired
    private TodoService todoService;
    @Resource
    private TodoMapper todoMapper;
    @Resource
    private UserMapper userMapper;

    @ApiOperation("获取某人的TodoList(sort:0未完成|1已完成)")
    @GetMapping("/get/{userId}")
    public Result getTodosByUserId(@PathVariable Long userId,@RequestParam(required = false) Short sort) {
        log.info("访问了/todo/get/"+userId+"接口");
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"该用户不存在");
        }
        if (sort==null) {
            List<TodoVO> todos = todoService.getTodosByUserId(userId,sort);
            return Result.success(todos);
        }
        if (sort==0 || sort==1) {
            List<TodoVO> todos = todoService.getTodosByUserId(userId,sort);
            return Result.success(todos);
        } else {
            return Result.error(Constants.CODE_400,"sort:0未完成|1已完成");
        }

    }

    @ApiOperation("完成某项todo")
    @GetMapping("/do/{id}")
    public Result doTodo(@PathVariable Long id) {
        log.info("访问了/todo/do/"+id+"接口");
        if (todoMapper.selectById(id)==null) {
            return Result.error(Constants.CODE_400,"不存在该Todo");
        }
        todoService.doTodo(id);
        return Result.success();
    }

    @ApiOperation("取消完成某项todo")
    @GetMapping("/undo/{id}")
    public Result undoTodo(@PathVariable Long id) {
        log.info("访问了/todo/undo/"+id+"接口");
        if (todoMapper.selectById(id)==null) {
            return Result.error(Constants.CODE_400,"不存在该Todo");
        }
        todoService.undoTodo(id);
        return Result.success();
    }

    @ApiOperation("添加todo")
    @GetMapping("/add/{userId}/{content}")
    public Result addTodo(@PathVariable Long userId,@PathVariable String content) {
        log.info("访问了/todo/add/"+userId+"/"+content+"接口");
        if (userMapper.selectById(userId)==null) {
            return Result.error(Constants.CODE_400,"该用户不存在");
        }
        todoService.addTodo(userId,content);
        return Result.success();
    }

    @ApiOperation("删除todo")
    @DeleteMapping("/{id}")
    public Result delTodo(@PathVariable Long id) {
        log.info("访问了/todo/"+id+"接口");
        if (todoMapper.selectById(id)==null) {
            return Result.error(Constants.CODE_400,"不存在该Todo");
        }
        todoService.delTodo(id);
        return Result.success();
    }

    @ApiOperation("修改Todo")
    @GetMapping("/upd/{id}/{content}")
    public Result updTodo(@PathVariable Long id,@PathVariable String content) {
        log.info("访问了/todo/upd/{}/{}接口",id,content);
        if (todoMapper.selectById(id)==null) {
            return Result.error(Constants.CODE_400,"不存在该Todo");
        }
        todoService.updTodo(id,content);
        return Result.success();
    }




}
