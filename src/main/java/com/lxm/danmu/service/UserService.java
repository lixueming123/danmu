package com.lxm.danmu.service;

import com.lxm.danmu.common.dto.LoginDto;
import com.lxm.danmu.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lxm.danmu.common.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
public interface UserService extends IService<User> {

    RespBean doLogin(LoginDto loginDto, HttpServletRequest req, HttpServletResponse resp);

    User getUserByCookie(String ticket, HttpServletRequest request, HttpServletResponse response);

    boolean loginCheck(String ticket);

    User getUser(String ticket);

    void deleteUser(HttpServletRequest req, HttpServletResponse resp);

    RespBean register(String username, String password, String nickname);
}
