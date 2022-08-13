package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.entity.Posts;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author 天狗
 * @date 2022/7/20
 */
@Mapper
public interface PostsMapper extends BaseMapper<Posts> {

    @Update("UPDATE `posts` SET is_deleted=0 WHERE user_id = #{userId}")
    public void unbanPostByUser(Long userId);

    @Update("UPDATE `posts` SET is_deleted=0 WHERE id = #{postId}")
    public void unbanPostById(Long postId);

}
