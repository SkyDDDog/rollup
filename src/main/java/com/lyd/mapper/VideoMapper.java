package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    @Update("UPDATE `video` SET is_deleted=0 WHERE user_id = #{userId}")
    public void unbanVideoByUser(Long userId);

    @Update("UPDATE `video` SET is_deleted=0 WHERE id = #{id}")
    public void unbanVideoById(Long id);

}
