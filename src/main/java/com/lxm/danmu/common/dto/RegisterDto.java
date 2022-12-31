package com.lxm.danmu.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDto implements Serializable {
    private String username;

    private String nickName;

    private String password;
}
