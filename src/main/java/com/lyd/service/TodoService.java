package com.lyd.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyd.controller.VO.TodoVO;
import com.lyd.entity.Todo;
import com.lyd.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public void doTodo(Long id) {
        Todo todo = todoMapper.selectById(id);
        todo.set_done(true);

        todoMapper.updateById(todo);
    }

    public void undoTodo(Long id) {
        Todo todo = todoMapper.selectById(id);
        todo.set_done(false);

        todoMapper.updateById(todo);
    }

    public void addTodo(Long userId,String content) {
        Todo todo = new Todo();
        todo.setUser_id(userId);
        todo.setContent(content);

        todoMapper.insert(todo);
    }

    public void delTodo(Long id) {
        todoMapper.deleteById(id);
    }

    public void updTodo(Long id,String content) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setContent(content);
        todoMapper.updateById(todo);
    }

}
