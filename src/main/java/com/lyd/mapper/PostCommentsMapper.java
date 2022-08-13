package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.entity.PostComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author 天狗
 * @date 2022/7/20
 */
@Mapper
public interface PostCommentsMapper extends BaseMapper<PostComments> {

    @Update("UPDATE `post_comments` SET is_deleted=0 WHERE user_id = #{userId}")
    public void unbanPcByUser(Long userId);

    @Update("UPDATE `post_comments` SET is_deleted=0 WHERE user_id = #{userId}")
    public void unbanPcById(Long userId);

}
