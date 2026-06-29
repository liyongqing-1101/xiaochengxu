package com.wxjiaozi.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchOperationDTO {

    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    private Integer status;
}
