package com.ownerkaka.springmybatis.support;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author akun
 * @since 2019-07-14
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class})
})
public class MyPlugin implements Interceptor {

    public Properties getProperties() {
        return properties;
    }

    private Properties properties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        Object wrap = Plugin.wrap(target, this);
        return wrap;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}