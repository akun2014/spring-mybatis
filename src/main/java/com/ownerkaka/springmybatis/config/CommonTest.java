package com.ownerkaka.springmybatis.config;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
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

        DataSource dataSource = applicationContext.getBean(DataSource.class);

        configuration.setDataSourceFactory(new DataSourceFactory() {
            @Override
            public void setProperties(Properties props) {

            }

            @Override
            public DataSource getDataSource() {
                return dataSource;
            }
        });

        ObjectFactory objectFactory = configuration.getObjectFactory();

        DataSource dataSource1 = configuration.getDataSourceFactory().getDataSource();
        Assert.assertSame(dataSource, dataSource1);
    }

    @Test
    public void test() throws SQLException {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");

        Configuration configuration = applicationContext.getBean(Configuration.class);
        configuration.setDataSourceFactory(new DataSourceFactory() {
            @Override
            public void setProperties(Properties props) {

            }

            @Override
            public DataSource getDataSource() {
                return applicationContext.getBean(DataSource.class);
            }
        });

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        Connection connection = sqlSession.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from ownerkaka_user");
        preparedStatement.execute();
        ResultSet resultSet = preparedStatement.getResultSet();
        while (resultSet.next()) {
            String username = resultSet.getString("username");
            System.out.println(username);
        }
    }

    @Test
    public void test2() throws SQLException {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");

        Configuration configuration = applicationContext.getBean(Configuration.class);
        configuration.setDataSourceFactory(new DataSourceFactory() {
            @Override
            public void setProperties(Properties props) {

            }

            @Override
            public DataSource getDataSource() {
                return applicationContext.getBean(DataSource.class);
            }
        });

        Collection<MappedStatement> mappedStatements = configuration.getMapperRegistry().getConfig().getMappedStatements();
        HashSet<MappedStatement> statementHashSet = new HashSet<>(mappedStatements);

        statementHashSet.forEach(configuration::addMappedStatement);

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        OwnerkakaOptions options = sqlSession.selectOne("options.selectOne");

        System.out.println(options);
    }

    @Test
    public void test3() throws SQLException {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");

        Configuration configuration = applicationContext.getBean(Configuration.class);
        configuration.setDataSourceFactory(new DataSourceFactory() {
            @Override
            public void setProperties(Properties props) {

            }

            @Override
            public DataSource getDataSource() {
                return applicationContext.getBean(DataSource.class);
            }
        });


        Configuration tempConfig = configuration.getMapperRegistry().getConfig();
        Collection<MappedStatement> mappedStatements = tempConfig.getMappedStatements();
        HashSet<MappedStatement> statementHashSet = new HashSet<>(mappedStatements);
        statementHashSet.forEach(configuration::addMappedStatement);
        tempConfig.getMapperRegistry().getMappers().forEach(configuration::addMapper);

        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.getById(1);
        System.out.println(user.getUid());
    }

    @Test
    public void test4() throws SQLException {
        GenericXmlApplicationContext applicationContext =
                new GenericXmlApplicationContext("application-customer-xsd.xml");


        SqlSessionFactory sqlSessionFactory = applicationContext.getBean("sqlSessionFactory", SqlSessionFactory.class);
        sqlSessionFactory.getConfiguration().setDataSourceFactory(new DataSourceFactory() {
            @Override
            public void setProperties(Properties props) {

            }

            @Override
            public DataSource getDataSource() {
                return applicationContext.getBean(DataSource.class);
            }
        });
        System.out.println(sqlSessionFactory.getConfiguration().getVariables());
        SqlSession sqlSession = sqlSessionFactory.openSession();

        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.getById(1);
        System.out.println(user.getUid());
    }
}
