package com.wxjiaozi.dto.mini;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSessionDTO {

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    private Integer questionCount;

    /** 随机试卷：单选题数量 */
    private Integer singleCount;

    /** 随机试卷：多选题数量 */
    private Integer multiCount;

    /** 随机试卷：判断题数量 */
    private Integer trueFalseCount;

    private String mode;
}
