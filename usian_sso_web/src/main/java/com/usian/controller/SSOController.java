package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.SSOServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbUser;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/frontend/sso")
public class SSOController {

    @Autowired
    private SSOServiceFeign ssoServiceFeign;

    @Autowired
    private CartServiceFeign cartServiceFeign;

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;

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
    public Result userLogin(String username, String password, HttpServletRequest request, HttpServletResponse response){
        Map map = ssoServiceFeign.userLogin(username,password);
        if (map != null && map.size()>0){
            String cookieValue = CookieUtils.getCookieValue(request, "CART_COOKIE_KEY", true);
            if (StringUtils.isNotBlank(cookieValue)){
                //获取cookie中的商品
                Map<String,TbItem> cart = JsonUtils.jsonToMap(cookieValue, TbItem.class);
                //获取已登录用户的id
                String userId = (String) map.get("userid");
                //根据登录用户的id，获取redis中的商品
                Map<String, TbItem> redisCart = cartServiceFeign.selectCartByUserId(userId);
                //遍历cookie的key
                Set<String> keySet = cart.keySet();
                for (String s : keySet) {
                    //都有相同的商品
                    TbItem cookieTbItem = cart.get(s);
                    TbItem redisTbItem = redisCart.get(s);
                    if (redisTbItem==null){ //等于空代表没有相同商品
                        redisCart.put(s,cookieTbItem);
                    }else {     //有相同商品，数量相加
                        redisTbItem.setNum(cookieTbItem.getNum()+redisTbItem.getNum());
                        redisCart.put(s,redisTbItem);
                    }
                    //重新赋值
                    cartServiceFeign.insertCart(userId,redisCart);
                }
                //把cookie中的数据删除，避免下次重复同步
                CookieUtils.deleteCookie(request,response,"CART_COOKIE_KEY");
            }
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
