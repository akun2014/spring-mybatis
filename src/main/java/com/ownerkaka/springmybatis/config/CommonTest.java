package com.ownerkaka.springmybatis.config;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
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

    @Test
    public void test() throws SQLException {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");

        Configuration configuration = applicationContext.getBean(Configuration.class);
        DataSource dataSource = applicationContext.getBean(DataSource.class);

        Environment environment = new Environment.Builder("test")
                .dataSource(dataSource)
                .transactionFactory(new JdbcTransactionFactory())
                .build();
        configuration.setEnvironment(environment);
        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        Connection connection = sqlSession.getConnection();
        String sql = connection.nativeSQL("select now()");
        System.out.println(sql);
        boolean closed = connection.isClosed();
        System.out.println(closed);
    }
}
