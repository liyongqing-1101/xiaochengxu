package com.wxjiaozi.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.dto.QuestionOptionDTO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 专门处理 List<QuestionOptionDTO> 的类型处理器
 * 用于 MySQL JSON 字段与 Java List 对象之间的转换
 */
public class QuestionOptionListTypeHandler extends BaseTypeHandler<List<QuestionOptionDTO>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<QuestionOptionDTO>> TYPE_REFERENCE =
            new TypeReference<List<QuestionOptionDTO>>() {};

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
            List<QuestionOptionDTO> parameter, JdbcType jdbcType) throws SQLException {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (Exception e) {
            throw new SQLException("Error converting List<QuestionOptionDTO> to JSON", e);
        }
    }

    @Override
    public List<QuestionOptionDTO> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }

    @Override
    public List<QuestionOptionDTO> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return parseJson(json);
    }

    @Override
    public List<QuestionOptionDTO> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return parseJson(json);
    }

    private List<QuestionOptionDTO> parseJson(String json) throws SQLException {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, TYPE_REFERENCE);
        } catch (Exception e) {
            throw new SQLException("Error converting JSON to List<QuestionOptionDTO>: " + json, e);
        }
    }
}
