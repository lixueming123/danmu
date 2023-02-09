package com.lxm.danmu.entity;

import java.time.LocalDate;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class Msg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 房间号码
     */
    private Long rid;

    /**
     * 发消息的用户id
     */
    private Long uid;

    /**
     * 发消息的用户的昵称
     */
    private String name;

    /**
     * 弹幕消息的内容
     */
    private String content;

    /**
     * 发送事件
     */
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    /**
     * 弹幕颜色
     */
    private String color;

    /**
     * 弹幕位置，0随机，1底部 2 顶部
     */
    private Integer position;

    /**
     *  是否可用
     */
    private Boolean available;

    /**
     * 字体大小
     */
    private Integer size;

    private Boolean bold;

    private Boolean italic;

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        Msg msg = new Msg(1L,0L,1L,"test_name","测试消息",new Date(),"blue",0,true,24,false,false);
        try {
            System.out.println(objectMapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    //{"id":1,"rid":0,"uid":1,"name":"test_name","content":"测试消息","time":"2023-02-08 15:55:41","color":"blue","position":0,"available":true,"size":24,"bold":false,"italic":false}
}

