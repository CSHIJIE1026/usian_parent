package com.usian.proxy.dynamicProxy;

import java.lang.reflect.Proxy;

public class Client {
    public static void main(String[] args) {
        RealStar realStar = new RealStar();
        ProxyStart proxyStart = new ProxyStart(realStar);
        //Start：要生哪个接口的代理类
        //handler：代理类要做的事情
        Star proxy = (Star) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Star.class}, proxyStart);
        proxy.sing();
    }

}
