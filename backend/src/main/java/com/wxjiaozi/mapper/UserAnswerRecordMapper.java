package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.UserAnswerRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户答题记录 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserAnswerRecordMapper {

    /**
     * 根据ID查询
     */
    UserAnswerRecord selectById(@Param("id") Long id);

    /**
     * 插入记录
     */
    int insert(UserAnswerRecord record);

    /**
     * 根据会话ID和用户ID查询答题记录
     */
    List<UserAnswerRecord> selectBySessionIdAndUserId(
            @Param("sessionId") String sessionId,
            @Param("userId") Long userId
    );

    /**
     * 根据用户ID查询所有答题记录
     */
    List<UserAnswerRecord> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID分页查询答题记录，按sessionId去重
     */
    List<UserAnswerRecord> selectByUserIdWithPagination(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 根据用户ID统计答题记录数
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据ID更新
     */
    int updateById(UserAnswerRecord record);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
}
