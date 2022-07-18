package com.lxm.danmu.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.lxm.danmu.dto.LoginDto;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.exception.GlobalException;
import com.lxm.danmu.mapper.UserMapper;
import com.lxm.danmu.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lxm.danmu.util.CookieUtil;
import com.lxm.danmu.util.UUIDUtil;
import com.lxm.danmu.vo.RespBean;
import com.lxm.danmu.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lxm
 * @since 2022-04-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Autowired
    UserMapper userMapper;

    /**
     * 登录方法
     * @param loginDto loginDto
     */
    @Override
    public RespBean doLogin(LoginDto loginDto, HttpServletRequest req, HttpServletResponse resp) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();

        // 通过用户名找到该用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username",username));
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 校验密码
        if (!user.getPassword().equals(password)) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        // 生成cookie
        String ticket = UUIDUtil.uuid();

        // 将用户信息存入redis中
        redisTemplate.opsForValue().set("user:" + ticket, user, 3, TimeUnit.DAYS);
        CookieUtil.setCookie(req, resp,"userTicket", ticket, 3 * 24 * 60 * 60);

        Map<Object, Object> map = MapUtil.builder()
                .put("id", user.getUid())
                .put("username", user.getUsername())
                .put("nickname", user.getNickname())
                .build();

        return RespBean.success(map);
    }

    @Override
    public User getUserByCookie(String ticket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(ticket)) {
            return null;
        }

        User user = (User) redisTemplate.opsForValue().get("user:" + ticket);

        if (user != null) {
            redisTemplate.expire("user:" + ticket, 7, TimeUnit.DAYS);
            CookieUtil.setCookie(request, response, "userTicket", ticket, 7 * 24 * 60 * 60);
        }
        return user;
    }

    @Override
    public boolean loginCheck(String ticket) {
        if (StringUtils.isBlank(ticket)) {
            return false;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + ticket);
        return user != null;
    }

    @Override
    public User getUser(String ticket) {
        if (StringUtils.isBlank(ticket)) {
            return null;
        }
        return (User) redisTemplate.opsForValue().get("user:" + ticket);
    }

    @Override
    public void deleteUser(HttpServletRequest req, HttpServletResponse resp) {
        redisTemplate.delete("user:" + CookieUtil.getCookieValue(req,"userTicket"));
        CookieUtil.deleteCookie(req, resp, "userTicket");
    }

    @Override
    public RespBean register(String username, String password, String nickname) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user != null) {
            return RespBean.error(RespBeanEnum.USER_ALREADY_EXIST);
        }

        user = new User();
        user.setNickname(nickname);
        user.setPassword(password);
        save(user);
        return RespBean.success();
    }


}
