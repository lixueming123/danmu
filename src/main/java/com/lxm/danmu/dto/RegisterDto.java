package com.lxm.danmu.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegisterDto implements Serializable {
    private String username;

    private String nickName;

    private String password;
}
