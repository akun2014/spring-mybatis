package com.ownerkaka.springmybatis.config;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.ClassUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by akun on 2019/8/7.
 */
public class MybatisBeanDefinitionParser implements BeanDefinitionParser {

    private static final String PROPERTIES = "properties";
    private static final String SETTINGS = "settings";
    private static final String TYPE_ALIASES = "typeAliases";
    private static final String TYPE_HANDLERS = "typeHandlers";
    private static final String OBJECT_FACTORY = "objectFactory";
    private static final String OBJECT_WRAPPER_FACTORY = "objectWrapperFactory";
    private static final String REFLECTOR_FACTORY = "reflectorFactory";
    private static final String PLUGINS = "plugins";
    private static final String ENVIRONMENTS = "environments";
    private static final String DATABASEID_PROVIDER = "databaseIdProvider";
    private static final String MAPPERS = "mappers";

    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String PACKAGE = "package";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String ALIAS = "alias";
    private static final String URL_ATTRIBUTE = "url";
    private static final String RESOURCE_ATTRIBUTE = "resource";
    private static final String ID_ATTRIBUTE = "id";
    private static final String CONFIGURATION = "configuration";


    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DefaultSqlSessionFactory.class);
//        builder.addPropertyReference(CONFIGURATION, CONFIGURATION);
        builder.addPropertyValue(CONFIGURATION, new Configuration());
        parserContext.getRegistry().registerBeanDefinition("sqlSessionFactory", builder.getBeanDefinition());

        List<Element> childElts = DomUtils.getChildElements(element);
        for (Element elt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(elt);
            if (PROPERTIES.equals(localName)) {
                parseProperties(elt, parserContext, builder);
            } else if (SETTINGS.equals(localName)) {
                parseSettings(elt, parserContext, builder);
            } else if (TYPE_ALIASES.equals(localName)) {
                parseTypeAliases(elt, parserContext, builder);
            } else if (TYPE_HANDLERS.equals(localName)) {
                parseTypeHandlerElement(elt, parserContext, builder);
            } else if (OBJECT_FACTORY.equals(localName)) {
                parseObjectFactory(elt, parserContext, builder);
            } else if (OBJECT_WRAPPER_FACTORY.equals(localName)) {
                parseObjectWrapperFactory(elt, parserContext, builder);
            } else if (REFLECTOR_FACTORY.equals(localName)) {
                parseReflectorFactory(elt, parserContext, builder);
            } else if (PLUGINS.equals(localName)) {
                parsePlugins(elt, parserContext, builder);
            } else if (ENVIRONMENTS.equals(localName)) {
                parseEnvironmentsElement(elt, parserContext, builder);
            } else if (DATABASEID_PROVIDER.equals(localName)) {
                parseDatabaseIdProviderElement(elt, parserContext, builder);
            } else if (MAPPERS.equals(localName)) {
                parseMappers(elt, parserContext, builder);
            }
        }
        return null;
    }

    private void parseDatabaseIdProviderElement(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {

        //todo
    }

    private void parseReflectorFactory(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String type = elt.getAttribute(TYPE_ATTRIBUTE);
        Class<?> aClass = null;
        try {
            aClass = ClassUtils.forName(type, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            parserContext.getReaderContext().error("ReflectorFactory加载失败", elt, e);
        }
        ReflectorFactory reflectorFactory = null;
        try {
            reflectorFactory = (ReflectorFactory) aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            parserContext.getReaderContext().error("ReflectorFactory加载失败", elt, e);
        }
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        configuration.setReflectorFactory(reflectorFactory);
    }

    private void parseMappers(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Element> childElts = DomUtils.getChildElements(elt);
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        for (Element childElt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(childElt);
            if (PACKAGE.equals(localName)) {
                String mapperPackage = childElt.getAttribute(NAME);
                configuration.addMappers(mapperPackage);
            } else {
                String resource = childElt.getAttribute("resource");
                String url = childElt.getAttribute("url");
                String mapperClass = childElt.getAttribute("class");
                Set<String> set = new HashSet<>(4);
                if (StringUtils.hasText(resource)) {
                    set.add(resource);
                }
                if (StringUtils.hasText(url)) {
                    set.add(url);
                }
                if (StringUtils.hasText(mapperClass)) {
                    set.add(mapperClass);
                }
                if (set.size() > 1) {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
                if (StringUtils.hasText(resource)) {
                    ClassPathResource classPathResource = new ClassPathResource(resource);
                    try {
                        InputStream inputStream = classPathResource.getInputStream();
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
                                resource, configuration.getSqlFragments());
                        mapperParser.parse();
                    } catch (IOException e) {
                        parserContext.getReaderContext().error("Mappers加载失败,IOException", elt, e);
                    }
                    continue;
                }
                if (StringUtils.hasText(url)) {
                    try {
                        UrlResource urlResource = new UrlResource(url);
                        InputStream inputStream = urlResource.getInputStream();
                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration,
                                url, configuration.getSqlFragments());
                        mapperParser.parse();
                    } catch (MalformedURLException e) {
                        parserContext.getReaderContext().error("Mappers加载失败", elt, e);
                    } catch (IOException e) {
                        parserContext.getReaderContext().error("Mappers加载失败,IOException", elt, e);
                    }
                    continue;
                }
                if (StringUtils.hasText(mapperClass)) {
                    try {
                        Class<?> mapperInterface = ClassUtils.forName(mapperClass, this.getClass().getClassLoader());
                        configuration.addMapper(mapperInterface);
                    } catch (ClassNotFoundException e) {
                        parserContext.getReaderContext().error("Mappers加载失败", elt, e);
                    }
                }
            }
        }
    }

    private void parseObjectWrapperFactory(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String type = elt.getAttribute(TYPE_ATTRIBUTE);
        Class<?> aClass = null;
        try {
            aClass = ClassUtils.forName(type, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            parserContext.getReaderContext().error("ObjectWrapperFactory加载失败", elt, e);
        }
        ObjectWrapperFactory objectWrapperFactory = null;
        try {
            objectWrapperFactory = (ObjectWrapperFactory) aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            parserContext.getReaderContext().error("ObjectWrapperFactory加载失败", elt, e);
        }
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        configuration.setObjectWrapperFactory(objectWrapperFactory);
    }

    private void parsePlugins(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Element> childElts = DomUtils.getChildElements(elt);
        InterceptorChain interceptorChain = new InterceptorChain();
        for (Element childElt : childElts) {
            String interceptor = childElt.getAttribute("interceptor");
            Class<?> aClass = null;
            try {
                aClass = ClassUtils.forName(interceptor, this.getClass().getClassLoader());
            } catch (ClassNotFoundException e) {
                parserContext.getReaderContext().error("interceptor加载失败", elt, e);
            }
            Interceptor interceptorInstance = null;
            try {
                interceptorInstance = (Interceptor) aClass.newInstance();
            } catch (InstantiationException e) {
                parserContext.getReaderContext().error("interceptor加载失败", elt, e);
            } catch (IllegalAccessException e) {
                parserContext.getReaderContext().error("interceptor加载失败", elt, e);
            }
            interceptorChain.addInterceptor(interceptorInstance);
        }
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        configuration.setInterceptorChain(interceptorChain);
    }

    private void parseObjectFactory(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        String type = elt.getAttribute(TYPE_ATTRIBUTE);
        Class<?> aClass = null;
        try {
            aClass = ClassUtils.forName(type, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            parserContext.getReaderContext().error("ObjectFactory加载失败", elt, e);
        }
        ObjectFactory objectFactory = null;
        try {
            objectFactory = (ObjectFactory) aClass.newInstance();
        } catch (InstantiationException e) {
            parserContext.getReaderContext().error("ObjectFactory加载失败", elt, e);
        } catch (IllegalAccessException e) {
            parserContext.getReaderContext().error("ObjectFactory加载失败", elt, e);
        }
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        configuration.setObjectFactory(objectFactory);
    }

    private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();

    private void parseProperties(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Properties properties = new Properties();

        if (elt.hasAttribute(RESOURCE_ATTRIBUTE)) {
            String resource = elt.getAttribute(RESOURCE_ATTRIBUTE);
            ClassPathResource classPathResource = new ClassPathResource(resource);
            try {
                propertiesPersister.load(properties, classPathResource.getInputStream());
            } catch (IOException e) {
                parserContext.getReaderContext().error("resource加载失败", elt, e);
            }
        }
        if (elt.hasAttribute(URL_ATTRIBUTE)) {
            String url = elt.getAttribute(URL_ATTRIBUTE);
            UrlResource urlResource = null;
            try {
                urlResource = new UrlResource(url);
            } catch (MalformedURLException e) {
                parserContext.getReaderContext().error("url错误", elt, e);
            }
            try {
                propertiesPersister.load(properties, urlResource.getInputStream());
            } catch (IOException e) {
                parserContext.getReaderContext().error("url加载失败", elt, e);
            }
        }
        List<Element> childElts = DomUtils.getChildElements(elt);
        for (Element childElt : childElts) {
            properties.setProperty(childElt.getAttribute(NAME), childElt.getAttribute(VALUE));
        }
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        configuration.setVariables(properties);
    }

    private void parseSettings(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Element> childElts = DomUtils.getChildElements(elt);

        Set<String> set = new HashSet<>();
        set.add("logImpl");
        for (Element childElt : childElts) {
            String name = childElt.getAttribute(NAME);
            if (set.contains(name)) {
                continue;
            }
            String value = childElt.getAttribute(VALUE);
//            builder.addPropertyValue(name, value);
        }
    }

    private void parseTypeAliases(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Element> childElts = DomUtils.getChildElements(elt);

        TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        for (Element childElt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(childElt);
            if (PACKAGE.equals(localName)) {
                String typeAliasPackage = childElt.getAttribute(NAME);
                TypeAliasRegistry.registerAliases(typeAliasPackage);
            } else {
                String alias = childElt.getAttribute(ALIAS);
                String type = childElt.getAttribute(TYPE_ATTRIBUTE);
                try {
                    Class<?> clazz = ClassUtils.forName(type, this.getClass().getClassLoader());
                    TypeAliasRegistry.registerAlias(alias, clazz);

                    GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
                    genericBeanDefinition.setBeanClass(clazz);
                    if (!StringUtils.hasText(alias)) {
                        alias = clazz.getSimpleName();
                    }
                    parserContext.getRegistry().registerBeanDefinition(alias, genericBeanDefinition);
                } catch (ClassNotFoundException e) {
                    throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                }
            }
        }
    }

    private void parseTypeHandlerElement(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {
        List<Element> childElts = DomUtils.getChildElements(elt);

        Configuration configuration = (Configuration) builder.getBeanDefinition().getPropertyValues().get(CONFIGURATION);
        for (Element childElt : childElts) {
            if (childElt.hasAttribute(PACKAGE)) {
                String typeHandlerPackage = childElt.getAttribute(NAME);
                TypeHandlerRegistry.register(typeHandlerPackage);
            } else {
                // todo
                String javaTypeName = childElt.getAttribute("javaType");
                String jdbcTypeName = childElt.getAttribute("jdbcType");
                String handlerTypeName = childElt.getAttribute("handler");
                try {
                    Class<?> clazz = ClassUtils.forName(handlerTypeName, this.getClass().getClassLoader());
                    TypeHandlerRegistry.register(clazz);
                } catch (ClassNotFoundException e) {
                    parserContext.getReaderContext().error("TypeHandler加载失败", elt, e);
                }
            }
        }
    }

    private void parseEnvironmentsElement(Element elt, ParserContext parserContext, BeanDefinitionBuilder builder) {

        String aDefault = elt.getAttribute("default");
        List<Element> childElts = DomUtils.getChildElements(elt);

        for (Element childElt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(childElt);


        }
    }
}
