package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.admin.AdminLoginResultDTO;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.entity.AdminUser;
import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamChapter;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ExamTag;
import com.wxjiaozi.mapper.AdminUserMapper;
import com.wxjiaozi.mapper.ExamCategoryMapper;
import com.wxjiaozi.mapper.ExamChapterMapper;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.mapper.ExamTagMapper;
import com.wxjiaozi.security.JwtUtil;
import com.wxjiaozi.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final AdminUserMapper adminUserMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamCategoryMapper examCategoryMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final ExamChapterMapper examChapterMapper;
    private final ExamTagMapper examTagMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public AdminServiceImpl(AdminUserMapper adminUserMapper,
                            ExamQuestionMapper examQuestionMapper,
                            ExamCategoryMapper examCategoryMapper,
                            ExamSubjectMapper examSubjectMapper,
                            ExamChapterMapper examChapterMapper,
                            ExamTagMapper examTagMapper,
                            JwtUtil jwtUtil,
                            ObjectMapper objectMapper) {
        this.adminUserMapper = adminUserMapper;
        this.examQuestionMapper = examQuestionMapper;
        this.examCategoryMapper = examCategoryMapper;
        this.examSubjectMapper = examSubjectMapper;
        this.examChapterMapper = examChapterMapper;
        this.examTagMapper = examTagMapper;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminLoginResultDTO login(String username, String password) {
        AdminUser adminUser = adminUserMapper.selectByUsername(username);
        if (adminUser == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (adminUser.getStatus() != null && adminUser.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        if (!BCrypt.checkpw(password, adminUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(adminUser.getId(), null, "ADMIN");

        AdminLoginResultDTO result = new AdminLoginResultDTO();
        result.setToken(token);
        result.setNickname(adminUser.getNickname());
        result.setRole(adminUser.getRole() != null ? adminUser.getRole() : "admin");
        return result;
    }

    @Override
    public PageResult<ExamQuestion> queryQuestions(Long categoryId, Long subjectId, Long chapterId, Long tagId,
                                                    Integer type, Integer difficulty, Integer status, String keyword,
                                                    int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ExamQuestion> records = examQuestionMapper.selectPageWithFilters(
                offset, pageSize, subjectId, type, status, keyword
        );
        Long total = examQuestionMapper.countWithFilters(subjectId, type, status, keyword);
        return new PageResult<>(records, total, page, pageSize);
    }

    @Override
    @Transactional
    public ExamQuestion saveQuestion(QuestionSaveDTO dto) {
        ExamQuestion question;
        if (dto.getId() != null) {
            question = examQuestionMapper.selectById(dto.getId());
            if (question == null) {
                throw new BusinessException("题目不存在");
            }
        } else {
            question = new ExamQuestion();
        }

        question.setSubjectId(dto.getSubjectId());
        question.setType(dto.getType());
        question.setStem(dto.getStem());
        question.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        // 答案存储：单选存字母，多选存逗号分隔，判断存true/false
        String answer = dto.getAnswer();
        if (StrUtil.isNotBlank(answer)) {
            String trimmed = answer.trim();
            // 判断题特殊处理
            if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                answer = trimmed.toLowerCase();
            } else if (trimmed.contains(",")) {
                // 多选：多个字母逗号分隔，统一转大写
                String[] parts = trimmed.split(",");
                List<String> partsList = new ArrayList<>();
                for (String part : parts) {
                    String trimmedPart = part.trim().toUpperCase();
                    if (!trimmedPart.isEmpty()) {
                        partsList.add(trimmedPart);
                    }
                }
                answer = String.join(",", partsList);
            } else {
                // 单选：单个字母
                answer = trimmed.toUpperCase();
            }
        }
        question.setAnswer(answer);
        question.setExplanation(dto.getExplanation());
        // optionList 使用新的JSON格式（由DTO处理，此处简化）
        if (dto.getOptionList() != null && !dto.getOptionList().isEmpty()) {
            question.setOptionList(dto.getOptionList());
        }

        if (dto.getId() != null) {
            examQuestionMapper.updateById(question);
        } else {
            examQuestionMapper.insert(question);
        }

        return question;
    }

    @Override
    @Transactional
    public void deleteQuestion(Long id) {
        ExamQuestion question = examQuestionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        examQuestionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        examQuestionMapper.batchDeleteByIds(ids);
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        List<ExamQuestion> questions = examQuestionMapper.selectByIds(ids);
        for (ExamQuestion q : questions) {
            q.setStatus(status);
        }
        for (ExamQuestion q : questions) {
            examQuestionMapper.updateById(q);
        }
    }

    @Override
    @Transactional
    public ExamCategory saveCategory(Long id, Long parentId, String type, String name, String icon,
                                      String description, Integer sortOrder) {
        switch (type) {
            case "category": {
                ExamCategory category;
                if (id != null) {
                    category = examCategoryMapper.selectById(id);
                    if (category == null) {
                        throw new BusinessException("分类不存在");
                    }
                } else {
                    category = new ExamCategory();
                }
                category.setName(name);
                category.setIcon(icon);
                category.setDescription(description);
                category.setSortOrder(sortOrder != null ? sortOrder : 0);
                category.setStatus(1);
                if (id != null) {
                    examCategoryMapper.updateById(category);
                } else {
                    examCategoryMapper.insert(category);
                }
                return category;
            }
            case "subject": {
                ExamSubject subject;
                if (id != null) {
                    subject = examSubjectMapper.selectById(id);
                    if (subject == null) {
                        throw new BusinessException("科目不存在");
                    }
                } else {
                    subject = new ExamSubject();
                }
                subject.setCategoryId(parentId);
                subject.setName(name);
                subject.setIcon(icon);
                subject.setSortOrder(sortOrder != null ? sortOrder : 0);
                subject.setStatus(1);
                if (id != null) {
                    examSubjectMapper.updateById(subject);
                } else {
                    examSubjectMapper.insert(subject);
                }
                return null;
            }
            case "chapter": {
                ExamChapter chapter;
                if (id != null) {
                    chapter = examChapterMapper.selectById(id);
                    if (chapter == null) {
                        throw new BusinessException("章节不存在");
                    }
                } else {
                    chapter = new ExamChapter();
                }
                chapter.setSubjectId(parentId);
                chapter.setName(name);
                chapter.setSortOrder(sortOrder != null ? sortOrder : 0);
                if (id != null) {
                    examChapterMapper.updateById(chapter);
                } else {
                    examChapterMapper.insert(chapter);
                }
                return null;
            }
            case "tag": {
                ExamTag tag;
                if (id != null) {
                    tag = examTagMapper.selectById(id);
                    if (tag == null) {
                        throw new BusinessException("知识点不存在");
                    }
                } else {
                    tag = new ExamTag();
                }
                tag.setChapterId(parentId);
                tag.setName(name);
                if (id != null) {
                    examTagMapper.updateById(tag);
                } else {
                    examTagMapper.insert(tag);
                }
                return null;
            }
            default:
                throw new BusinessException("不支持的分类类型: " + type);
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String type) {
        switch (type) {
            case "category":
                examCategoryMapper.deleteById(id);
                break;
            case "subject":
                examSubjectMapper.deleteById(id);
                break;
            case "chapter":
                examChapterMapper.deleteById(id);
                break;
            case "tag":
                examTagMapper.deleteById(id);
                break;
            default:
                throw new BusinessException("不支持的分类类型: " + type);
        }
    }

    @Override
    public List<ExamCategory> getFullCategoryTree() {
        List<ExamCategory> categories = examCategoryMapper.selectAll();
        for (ExamCategory category : categories) {
            List<ExamSubject> subjects = examSubjectMapper.selectByCategoryId(category.getId());
            for (ExamSubject subject : subjects) {
                List<ExamChapter> chapters = examChapterMapper.selectBySubjectId(subject.getId());
                for (ExamChapter chapter : chapters) {
                    List<ExamTag> tags = examTagMapper.selectByChapterId(chapter.getId());
                    chapter.setTags(tags);
                }
                subject.setChapters(chapters);
            }
            category.setSubjects(subjects);
        }
        return categories;
    }
}
