package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.entity.CommentComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author 天狗
 * @date 2022/7/20
 */
@Mapper
public interface CommentCommentsMapper extends BaseMapper<CommentComments> {

    @Update("UPDATE `comment_comments` SET is_deleted=0 WHERE user_id = #{userId}")
    public void unbanCcByUser(Long userId);

    @Update("UPDATE `comment_comments` SET is_deleted=0 WHERE id = #{id}")
    public void unbanCcById(Long id);

}
