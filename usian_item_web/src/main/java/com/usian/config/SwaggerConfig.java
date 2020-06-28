package com.usian.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration  //<beans>
@EnableSwagger2 //扫描swagger的注解
public class SwaggerConfig {

    /**
     * Docket:生成接口文档
     * @return
     */
    @Bean
    public Docket getDocket(){
        return new Docket(DocumentationType.SWAGGER_2) //生成文档的类别为swagger2版本
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.usian.controller")) //生成哪些controller （扫描哪个包生成接口文档）
                .paths(PathSelectors.any()) //包里的任何路径
                .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("优思安商城后台管理系统")  //Api文档标题
                .description("商品管理模块接口文档") //Api 文档描述
                .version("1.0")  //版本
                .build(); //用户体验不太好，调一下build
    }

}
