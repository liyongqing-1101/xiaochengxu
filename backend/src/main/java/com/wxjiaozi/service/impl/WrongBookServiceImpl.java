package com.wxjiaozi.service.impl;

import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.QuestionDTO;
import com.wxjiaozi.dto.mini.WrongQuestionDTO;
import com.wxjiaozi.dto.mini.WrongQuestionSyncDTO;
import com.wxjiaozi.util.OptionListConverter;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.UserWrongQuestion;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.UserWrongQuestionMapper;
import com.wxjiaozi.service.WrongBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class WrongBookServiceImpl implements WrongBookService {

    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final ExamQuestionMapper examQuestionMapper;

    public WrongBookServiceImpl(UserWrongQuestionMapper userWrongQuestionMapper,
                                ExamQuestionMapper examQuestionMapper) {
        this.userWrongQuestionMapper = userWrongQuestionMapper;
        this.examQuestionMapper = examQuestionMapper;
    }

    @Override
    @Transactional
    public void syncWrongQuestions(Long userId, List<WrongQuestionSyncDTO> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        for (WrongQuestionSyncDTO syncDTO : questions) {
            UserWrongQuestion existing = userWrongQuestionMapper.selectByUserIdAndQuestionId(userId, syncDTO.getQuestionId());
            if (existing == null) {
                UserWrongQuestion wrong = new UserWrongQuestion();
                wrong.setUserId(userId);
                wrong.setQuestionId(syncDTO.getQuestionId());
                wrong.setErrorCount(1);
                wrong.setLastWrongAnswer(syncDTO.getSelectedOptions());
                wrong.setSource(syncDTO.getSource() != null ? syncDTO.getSource() : 0);
                userWrongQuestionMapper.insert(wrong);
            } else {
                existing.setErrorCount(existing.getErrorCount() + 1);
                if (syncDTO.getSelectedOptions() != null) {
                    existing.setLastWrongAnswer(syncDTO.getSelectedOptions());
                }
                if (syncDTO.getSource() != null) {
                    existing.setSource(syncDTO.getSource());
                }
                userWrongQuestionMapper.updateById(existing);
            }
        }
    }

    @Override
    public PageResult<WrongQuestionDTO> getWrongList(Long userId, Long subjectId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<UserWrongQuestion> wrongQuestions = userWrongQuestionMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        Long total = userWrongQuestionMapper.countByUserId(userId);

        List<WrongQuestionDTO> list = new ArrayList<>();
        for (UserWrongQuestion wrong : wrongQuestions) {
            ExamQuestion question = examQuestionMapper.selectById(wrong.getQuestionId());
            if (question == null) {
                continue;
            }

            if (subjectId != null && !subjectId.equals(question.getSubjectId())) {
                continue;
            }

            QuestionDTO questionDTO = convertToDTO(question);
            questionDTO.setAnswer(null);

            WrongQuestionDTO dto = new WrongQuestionDTO();
            dto.setId(wrong.getId());
            dto.setQuestionId(wrong.getQuestionId());
            dto.setQuestion(questionDTO);
            dto.setErrorCount(wrong.getErrorCount());
            dto.setLastErrorTime(wrong.getUpdatedAt());
            dto.setLastWrongAnswer(wrong.getLastWrongAnswer());
            dto.setSource(wrong.getSource());
            list.add(dto);
        }

        return new PageResult<>(list, total, page, pageSize);
    }

    @Override
    @Transactional
    public void removeWrong(Long userId, Long questionId) {
        UserWrongQuestion wrong = userWrongQuestionMapper.selectByUserIdAndQuestionId(userId, questionId);
        if (wrong == null) {
            throw new BusinessException("错题记录不存在");
        }
        userWrongQuestionMapper.deleteById(wrong.getId());
    }

    private QuestionDTO convertToDTO(ExamQuestion q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setSubjectId(q.getSubjectId());
        dto.setType(q.getType());
        dto.setStem(q.getStem());
        dto.setOptionList(OptionListConverter.toDTOList(q.getOptionList()));
        dto.setAnswer(q.getAnswer());
        dto.setExplanation(q.getExplanation());
        dto.setStatus(q.getStatus());
        return dto;
    }
}
