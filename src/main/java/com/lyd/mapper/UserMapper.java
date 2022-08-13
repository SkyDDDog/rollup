package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.controller.VO.MyPost;
import com.lyd.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author 天狗
 * @date 2022/7/20
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("(SELECT id,`name` title,NULL discussNum,\"文档\" kind,DATE_FORMAT(gmt_modified,'%Y-%m-%d') `date` FROM `document` WHERE publisher_id = #{userId})\n" +
            "UNION" +
            "(SELECT id,`name`,NULL,\"视频\",DATE_FORMAT(gmt_modified,'%Y-%m-%d') FROM video  WHERE user_id = #{userId})" +
            "limit #{pageNum} , #{pageSize}")
    public List<MyPost> getMyPost(Long userId,Integer pageNum,Integer pageSize);

    @Update("UPDATE `user` SET is_deleted=0 WHERE id = #{userId}")
    public void unbanUser(Long userId);

}
