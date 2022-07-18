package com.lxm.danmu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxm.danmu.entity.User;
import com.lxm.danmu.service.UserService;
import com.lxm.danmu.util.CookieUtil;
import com.lxm.danmu.vo.RespBean;
import com.lxm.danmu.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireAuthenticate authAnnotation = handlerMethod.getMethodAnnotation(RequireAuthenticate.class);
            if (authAnnotation == null) {
                return true;
            }

            User user = getUser(request,response);
            if (user == null) {
                writeError(response, RespBeanEnum.SESSION_ERROR);
                return false;
            }
            UserContext.setUser(user);
        }
        return true;
    }

    public void writeError(HttpServletResponse response, RespBeanEnum errorEnum) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        RespBean respBean = RespBean.error(errorEnum);
        writer.write(objectMapper.writeValueAsString(respBean));
        writer.flush();
        writer.close();
    }

    public User getUser(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userTicket");
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        return userService.getUserByCookie(ticket, request, response);
    }
}
