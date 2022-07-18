package com.lxm.danmu.entity;

import java.time.LocalDate;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

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


}
