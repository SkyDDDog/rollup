package com.lyd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyd.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @author 天狗
 * @date 2022/7/26
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    @Update("UPDATE `document` SET is_deleted=0 WHERE publisher_id = #{userId}")
    public void unbanDoc(Long userId);

}
