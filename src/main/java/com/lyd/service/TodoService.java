package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.TodoVO;
import com.lyd.entity.Todo;
import com.lyd.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 天狗
 * @date 2022/7/21
 */
@Service
public class TodoService {

    @Resource
    private TodoMapper todoMapper;

    /**
     * @desc    获取某用户todoList
     * @param userId
     * @param sort
     * @return
     */
    public List<TodoVO> getTodosByUserId(Long userId,Short sort) {
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        if (sort==null) {
            ;
        } else if (sort==1) {
            wrapper.eq("is_done",1);
        } else if (sort==0) {
            wrapper.eq("is_done",0);
        } else {
            wrapper.orderByDesc("is_done");
        }
        List<Todo> todos = todoMapper.selectList(wrapper);
        ArrayList<TodoVO> res = new ArrayList<>();
        for (Todo todo : todos) {
            TodoVO todoVO = new TodoVO();
            todoVO.setId(todo.getId().toString());
            todoVO.setContent(todo.getContent());
            todoVO.setDone(todo.is_done());

            res.add(todoVO);
        }
        return res;
    }

    /**
     * @desc 完成todo事项
     * @param id    todoId
     */
    public void doTodo(Long id) {
        Todo todo = todoMapper.selectById(id);
        todo.set_done(true);

        todoMapper.updateById(todo);
    }

    /**
     * @desc    取消完成todo事项
     * @param id    todoId
     */
    public void undoTodo(Long id) {
        Todo todo = todoMapper.selectById(id);
        todo.set_done(false);

        todoMapper.updateById(todo);
    }

    /**
     * @desc    添加todo事项
     * @param userId    用户id
     * @param content   todo事项内容
     */
    public void addTodo(Long userId,String content) {
        Todo todo = new Todo();
        todo.setUser_id(userId);
        todo.setContent(content);

        todoMapper.insert(todo);
    }

    /**
     * @desc    通过内容获取事项
     * @param name
     * @return
     */
    public List<Todo> getByName(String name) {
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("content",name);
        return todoMapper.selectList(wrapper);
    }

    /**
     * @desc    逻辑删除todo事项
     * @param id    todoId
     */
    public void delTodo(Long id) {
        todoMapper.deleteById(id);
    }

    /**
     * @desc    更新todo事项内容
     * @param id    todoId
     * @param content   内容
     */
    public void updTodo(Long id,String content) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setContent(content);
        todoMapper.updateById(todo);
    }

    /**
     * @desc    暂停专注
     * @param todoId    todoId
     * @param time  专注时间
     */
    public void stopFocus(Long todoId,Integer time) {
        Todo todo = todoMapper.selectById(todoId);
        todo.setTime(todo.getTime()+time);
        todoMapper.updateById(todo);
    }

    /**
     * @desc 获取专注(按本周/月分类)
     * @param userId    用户id
     * @param sort      1本周|2本月
     * @return
     */
    public List<TodoVO> getFocus(Long userId,Short sort) {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime;
        LocalDate endTime;
        if (sort==1) {
            startTime = localDate.with(DayOfWeek.MONDAY);
            endTime = localDate.with(DayOfWeek.SUNDAY);
        } else {
            startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
            endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.orderByDesc("time");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = df.format(startTime)+" 00:00:00";
        String end = df.format(endTime)+" 23:59:59";
        wrapper.between("gmt_modified",start,end);
        List<Todo> todos = todoMapper.selectList(wrapper);
        ArrayList<TodoVO> res = new ArrayList<>();
        int rank = 1;
        for (Todo todo : todos) {
            TodoVO todoVO = new TodoVO();
            todoVO.setRank(rank++);
            todoVO.setId(todo.getId().toString());
            todoVO.setContent(todo.getContent());
            todoVO.setTime(todo.getTime().toString());
            res.add(todoVO);
        }
        return res;
    }

    /**
     * @desc    获取专注个数
     * @param userId    用户id
     * @param sort      1本周|2本月
     * @return
     */
    public Long getFocusCount(Long userId,Short sort) {
        LocalDate localDate = LocalDate.now();
        LocalDate startTime;
        LocalDate endTime;
        if (sort==1) {
            startTime = localDate.with(DayOfWeek.MONDAY);
            endTime = localDate.with(DayOfWeek.SUNDAY);
        } else {
            startTime = localDate.with(TemporalAdjusters.firstDayOfMonth());
            endTime = localDate.with(TemporalAdjusters.lastDayOfMonth());
        }
        QueryWrapper<Todo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = df.format(startTime)+" 00:00:00";
        String end = df.format(endTime)+" 23:59:59";
        wrapper.between("gmt_modified",start,end);
        return todoMapper.selectCount(wrapper);
    }

}
