package com.usian.controller;

import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbUser;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/frontend/sso")
public class SSOController {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    /**
     * 注册信息校验
     * @param checkValue
     * @param checkFlag
     * @return
     */
    @RequestMapping("/checkUserInfo/{checkValue}/{checkFlag}")
    public Result checkUserInfo(@PathVariable String checkValue,@PathVariable Integer checkFlag){
            Boolean checkUserInfo = ssoServiceFeign.checkUserInfo(checkValue,checkFlag);
            if (checkUserInfo){
                return Result.ok();
            }
            return Result.error("校验失败");
    }

    /**
     * 用户注册
     * @param tbUser
     * @return
     */
    @RequestMapping("/userRegister")
    public Result userRegister(TbUser tbUser){
        Integer num = ssoServiceFeign.userRegister(tbUser);
        if (num == 1){
            return Result.ok();
        }
        return Result.error("注册失败");
    }

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/userLogin")
    public Result userLogin(String username,String password){
        Map map = ssoServiceFeign.userLogin(username,password);
        if (map != null && map.size()>0){
            return Result.ok(map);
        }
        return Result.error("登录失败");
    }

    /**
     * 通过token查询用户信息
     * @param token
     * @return
     */
    @RequestMapping("/getUserByToken/{token}")
    public Result getUserByToken(@PathVariable String token){
       TbUser tbUser = ssoServiceFeign.getUserByToken(token);
       if (tbUser != null){
           return Result.ok();
       }
       return Result.error("查询失败");
    }

    /**
     * 退出登录
     * @param token
     * @return
     */
    @RequestMapping("/logOut")
    public Result logOut(String token){
        Boolean logout = ssoServiceFeign.logOut(token);
        if (logout){
            return Result.ok();
        }
        return Result.error("退出失败");
    }
}
