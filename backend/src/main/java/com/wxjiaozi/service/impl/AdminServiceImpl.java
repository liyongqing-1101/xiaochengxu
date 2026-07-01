package com.wxjiaozi.service.impl;

import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamChapter;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ExamTag;
import com.wxjiaozi.mapper.ExamCategoryMapper;
import com.wxjiaozi.mapper.ExamChapterMapper;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.mapper.ExamTagMapper;
import com.wxjiaozi.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ExamQuestionMapper examQuestionMapper;
    private final ExamCategoryMapper examCategoryMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final ExamChapterMapper examChapterMapper;
    private final ExamTagMapper examTagMapper;

    // 注意：登录逻辑已移至 AdminAuthController 直接实现
    // 管理后台改用 HttpSession + BCrypt，不再使用 JWT + Redis

    @Override
    public ExamQuestion getQuestionById(Long id) {
        return examQuestionMapper.selectById(id);
    }

    @Override
    public PageResult<ExamQuestion> queryQuestions(Long categoryId, Long subjectId, Long chapterId, Long tagId,
                                                    Integer type, Integer difficulty, Integer status, String keyword,
                                                    int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ExamQuestion> records = examQuestionMapper.selectPageWithFilters(
                offset, pageSize, subjectId, type, status, keyword);
        Long total = examQuestionMapper.countWithFilters(subjectId, type, status, keyword);
        return new PageResult<>(records, total, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamQuestion saveQuestion(QuestionSaveDTO dto) {
        try {
            // 参数校验
            validateQuestionDTO(dto);

            ExamQuestion question;
            Long oldSubjectId = null;

            if (dto.getId() != null) {
                // 编辑模式
                question = examQuestionMapper.selectById(dto.getId());
                if (question == null) {
                    throw new BusinessException("题目不存在");
                }
                oldSubjectId = question.getSubjectId();
            } else {
                // 新增模式
                question = new ExamQuestion();
                question.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            }

            // 设置基本字段
            question.setSubjectId(dto.getSubjectId());
            question.setType(dto.getType());
            question.setStem(dto.getStem());
            question.setAnswer(dto.getAnswer());
            question.setExplanation(dto.getExplanation());

            // 处理选项：判断题optionList设为null
            if (dto.getType() == 3) {
                question.setOptionList(null);
            } else {
                question.setOptionList(dto.getOptionList());
            }

            // 执行保存
            if (dto.getId() != null) {
                examQuestionMapper.updateById(question);
                // 如果科目变更，更新两个科目的计数
                if (oldSubjectId != null && !oldSubjectId.equals(dto.getSubjectId())) {
                    examSubjectMapper.decrementQuestionCount(oldSubjectId);
                    examSubjectMapper.incrementQuestionCount(dto.getSubjectId());
                }
            } else {
                examQuestionMapper.insert(question);
                // 新增题目，科目计数+1
                examSubjectMapper.incrementQuestionCount(dto.getSubjectId());
            }

            return question;
        } catch (BusinessException e) {
            log.warn("保存题目业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("保存题目系统异常", e);
            throw new BusinessException("保存失败: " + e.getMessage());
        }
    }

    /**
     * 校验题目参数
     */
    private void validateQuestionDTO(QuestionSaveDTO dto) {
        if (dto.getSubjectId() == null) {
            throw new BusinessException("请选择所属科目");
        }
        if (dto.getType() == null || dto.getType() < 1 || dto.getType() > 3) {
            throw new BusinessException("题型参数错误，应为1(单选)/2(多选)/3(判断)");
        }
        if (dto.getStem() == null || dto.getStem().trim().isEmpty()) {
            throw new BusinessException("题干不能为空");
        }
        if (dto.getAnswer() == null || dto.getAnswer().trim().isEmpty()) {
            throw new BusinessException("正确答案不能为空");
        }
        // 单选/多选必须有选项
        if (dto.getType() != 3 && (dto.getOptionList() == null || dto.getOptionList().isEmpty())) {
            throw new BusinessException("选择题的选项不能为空");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        try {
            ExamQuestion question = examQuestionMapper.selectById(id);
            if (question == null) {
                throw new BusinessException("题目不存在");
            }
            examQuestionMapper.deleteById(id);
            // 删除题目，科目计数-1
            if (question.getSubjectId() != null) {
                examSubjectMapper.decrementQuestionCount(question.getSubjectId());
            }
        } catch (BusinessException e) {
            log.warn("删除题目业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除题目系统异常", e);
            throw new BusinessException("删除失败: " + e.getMessage());
        }
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
