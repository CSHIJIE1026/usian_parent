package com.usian.interceptor;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class UserLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //获取token，对用户的 token 做判断
        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)){
            return false;
        }

        //如果用户 token 不为空，则校验用户在 redis 中是否失效
        TbUser user = ssoServiceFeign.getUserByToken(token);
        if (user == null){
            return false;
        }

        return true;
    }
}
