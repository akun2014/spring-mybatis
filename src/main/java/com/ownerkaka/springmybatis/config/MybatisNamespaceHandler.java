package com.ownerkaka.springmybatis.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by akun on 2019/8/7.
 */
public class MybatisNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("configuration", new MybatisBeanDefinitionParser());
//        registerBeanDefinitionParser("mapper", new MapperBeanDefinitionParser());
    }
}
