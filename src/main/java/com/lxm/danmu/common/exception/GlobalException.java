package com.lxm.danmu.common.exception;

import com.lxm.danmu.common.vo.RespBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException {
    private RespBeanEnum respBeanEnum;
}
