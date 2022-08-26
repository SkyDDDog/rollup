package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.controller.VO.BanUserVO;
import com.lyd.controller.VO.CollectionVO;
import com.lyd.controller.VO.MyPost;
import com.lyd.entity.User;
import org.apache.ibatis.annotations.Insert;
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
            "LIMIT #{pageNum} , #{pageSize}")
    public List<MyPost> getMyPost(Long userId,Integer pageNum,Integer pageSize);

    @Select("(SELECT id,`name`,3 sort,DATE_FORMAT(gmt_modified,'%Y-%m-%d') `date` FROM `video`)\n" +
            "UNION\n" +
            "(SELECT id,`name`,4 sort,DATE_FORMAT(gmt_modified,'%Y-%m-%d') `date` FROM `document` WHERE user_id = #{userId})\n" +
            "ORDER BY `date` DESC\n" +
            "LIMIT #{pageNum} , #{pageSize}")
    public List<CollectionVO> getMyColletion(Long userId,Integer pageNum, Integer pageSize);

    @Update("UPDATE `user` SET is_deleted=0 WHERE id = #{userId}")
    public void unbanUser(Long userId);

    @Update("UPDATE `user_info` SET is_deleted=0 WHERE id = #{userId}")
    public void unbanUserInfo(Long userId);

    @Update("UPDATE `user_report` SET is_deleted=1,result=#{result} WHERE id = #{reportId}")
    public void dealReport(Long reportId,Short result);

    @Select("SELECT id userId, \n" +
            "(SELECT COUNT(*) FROM `user_report` WHERE user_id=userId AND is_deleted=1 AND (gmt_modified BETWEEN #{startTime} AND #{endTime})) violationNum \n" +
            "FROM `user`\n" +
            "ORDER BY violationNum DESC\n" +
            "LIMIT #{pageNum},#{pageSize}")
    public List<BanUserVO> getViolationList(String startTime, String endTime,
                                            Integer pageNum, Integer pageSize);

    @Select("select Count(*) from `user`")
    public Long getAllCount();

    @Select("select * from `user` where id = #{userId}")
    public User getByID(Long userId);


}
