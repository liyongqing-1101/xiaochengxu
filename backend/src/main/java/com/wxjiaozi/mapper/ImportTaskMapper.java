package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ImportTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导入任务 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ImportTaskMapper {

    ImportTask selectById(@Param("id") Long id);

    List<ImportTask> selectAll();

    int insert(ImportTask task);

    int updateById(ImportTask task);

    int deleteById(@Param("id") Long id);

    /**
     * 分页查询导入任务
     */
    List<ImportTask> selectWithPagination(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 统计导入任务总数
     */
    Long countAll();
}
