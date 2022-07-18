package com.lxm.danmu.controller;


import com.lxm.danmu.config.RequireAuthenticate;
import com.lxm.danmu.dto.LoginDto;
import com.lxm.danmu.dto.RegisterDto;
import com.lxm.danmu.service.UserService;
import com.lxm.danmu.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public RespBean login(@RequestBody LoginDto loginDto, HttpServletRequest req, HttpServletResponse resp) {
        return userService.doLogin(loginDto,req,resp);
    }

    @GetMapping("/logout")
    @RequireAuthenticate
    public RespBean logout(HttpServletRequest req, HttpServletResponse resp) {
        userService.deleteUser(req, resp);
        return RespBean.success();
    }

    @PostMapping("/register")
    public RespBean register(@RequestBody RegisterDto registerDto) {
        return userService.register(registerDto.getPassword(), registerDto.getNickName(), registerDto.getUsername());
    }

}
