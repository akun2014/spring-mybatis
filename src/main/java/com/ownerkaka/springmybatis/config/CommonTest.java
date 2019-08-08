package com.ownerkaka.springmybatis.config;

import org.apache.ibatis.session.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.sql.Connection;
import java.util.Properties;

/**
 * Created by akun on 2019/8/8.
 */
public class CommonTest {


    public static void main(String[] args) throws Exception {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");

        Configuration configuration = applicationContext.getBean(Configuration.class);
        Properties properties = configuration.getVariables();

        System.out.println(properties.toString());

        ExecutorType executorType = configuration.getDefaultExecutorType();
        System.out.println(executorType);

        SqlSessionFactoryBean sessionFactoryBean = applicationContext.getBean(SqlSessionFactoryBean.class);
        SqlSessionFactory sqlSessionFactory = sessionFactoryBean.getObject();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        Connection connection = sqlSession.getConnection();
        boolean closed = connection.isClosed();
        System.out.println(closed);

        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();

    }
}
