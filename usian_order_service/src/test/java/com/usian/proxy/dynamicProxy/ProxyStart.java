package com.usian.proxy.dynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyStart implements InvocationHandler {
	
	Object realObj;
	
	public ProxyStart(Object object) {
		this.realObj = object;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object object = null;
		
		System.out.println("真正的方法执行前！");
		System.out.println("面谈，签合同，预付款，订机票");
		
		object = method.invoke(realObj, args);//通过反射调用真实角色
		
		System.out.println("真正的方法执行后！");
		System.out.println("收尾款");
		return object;
	}
}
