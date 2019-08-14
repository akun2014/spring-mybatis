package com.ownerkaka.springmybatis.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.CacheRefResolver;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author akun
 * @since 2019-08-14
 */
@Slf4j
public class MapperBeanDefinitionParser implements BeanDefinitionParser {

    private static final String CACHE = "cache";
    private static final String CACHE_REF = "cache-ref";
    private static final String PARAMETER_MAP = "parameterMap";
    private static final String RESULT_MAP = "resultMap";
    private static final String SQL = "sql";
    private static final String SELECT = "select";
    private static final String INSERT = "insert";
    private static final String UPDATE = "update";
    private static final String DELETE = "delete";

    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String PACKAGE = "package";
    private static final String TYPE_ATTRIBUTE = "type";
    private static final String ALIAS = "alias";
    private static final String URL_ATTRIBUTE = "url";
    private static final String RESOURCE_ATTRIBUTE = "resource";
    private static final String ID_ATTRIBUTE = "id";
    private static final String EVICTION_ATTRIBUTE = "eviction";
    private static final String FLUSH_INTERVAL_ATTRIBUTE = "flushInterval";
    private static final String SIZE_ATTRIBUTE = "size";
    private static final String READONLY_ATTRIBUTE = "readOnly";
    private static final String BLOCKING_ATTRIBUTE = "blocking";
    private static final String NAMESPACE_ATTRIBUTE = "namespace";

    MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(new Configuration(), "");
    Configuration configuration = builderAssistant.getConfiguration();

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String namespace = element.getAttribute(NAMESPACE_ATTRIBUTE);
        log.info("tagName:{}", element.getTagName());
        List<Element> childElts = DomUtils.getChildElements(element);
        for (Element elt : childElts) {
            String localName = parserContext.getDelegate().getLocalName(elt);
            if (CACHE.equals(localName)) {
                String type = elt.getAttribute(TYPE_ATTRIBUTE);
                Class<? extends Cache> typeClass = TypeAliasRegistry.resolveAlias(type);
                String eviction = elt.getAttribute(EVICTION_ATTRIBUTE);
                Class<? extends Cache> evictionClass = TypeAliasRegistry.resolveAlias(eviction);
                Long flushInterval = Long.parseLong(elt.getAttribute(FLUSH_INTERVAL_ATTRIBUTE));
                Integer size = Integer.parseInt(elt.getAttribute(SIZE_ATTRIBUTE));
                boolean readWrite = !Boolean.parseBoolean(elt.getAttribute(READONLY_ATTRIBUTE));
                boolean blocking = Boolean.parseBoolean(elt.getAttribute(BLOCKING_ATTRIBUTE));
                //todo propertis
                builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, null);
            } else if (CACHE_REF.equals(localName)) {
                String referencedNamespace = elt.getAttribute(NAMESPACE_ATTRIBUTE);
                configuration.addCacheRef(namespace, referencedNamespace);
                CacheRefResolver cacheRefResolver = new CacheRefResolver(builderAssistant, referencedNamespace);
                try {
                    cacheRefResolver.resolveCacheRef();
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteCacheRef(cacheRefResolver);
                }
            } else if (PARAMETER_MAP.equals(localName)) {
                String id = elt.getAttribute(ID_ATTRIBUTE);
                String type = elt.getAttribute(TYPE_ATTRIBUTE);
                Class<?> parameterClass = TypeAliasRegistry.resolveAlias(type);
                List<Element> childElements = DomUtils.getChildElements(elt);
                List<ParameterMapping> parameterMappings = new ArrayList<>();
                for (Element parameterNode : childElements) {
                    String property = parameterNode.getAttribute("property");
                    String javaType = parameterNode.getAttribute("javaType");
                    String jdbcType = parameterNode.getAttribute("jdbcType");
                    String resultMap = parameterNode.getAttribute("resultMap");
                    String mode = parameterNode.getAttribute("mode");
                    String typeHandler = parameterNode.getAttribute("typeHandler");
                    Integer numericScale = Integer.parseInt(parameterNode.getAttribute("numericScale"));

                    ParameterMode modeEnum = ParameterMode.parse(mode);
                    Class<?> javaTypeClass = TypeAliasRegistry.resolveAlias(javaType);
                    JdbcType jdbcTypeEnum = JdbcType.parse(jdbcType);
                    Class<? extends TypeHandler<?>> typeHandlerClass = TypeAliasRegistry.resolveAlias(typeHandler);
                    ParameterMapping parameterMapping =
                            builderAssistant.buildParameterMapping(parameterClass, property,
                                    javaTypeClass, jdbcTypeEnum, resultMap,
                                    modeEnum, typeHandlerClass, numericScale);
                    parameterMappings.add(parameterMapping);
                }
                builderAssistant.addParameterMap(id, parameterClass, parameterMappings);

            } else if (RESULT_MAP.equals(localName)) {

            } else if (SQL.equals(localName)) {
                System.out.println(localName);
                String id = elt.getAttribute(ID_ATTRIBUTE);
                String databaseId = elt.getAttribute("databaseId");

            } else if (SELECT.equals(localName)) {

            } else if (INSERT.equals(localName)) {

            } else if (UPDATE.equals(localName)) {

            } else if (DELETE.equals(localName)) {

            }
        }
        return null;
    }

    public void parseParameterMap(Element elt, ParserContext parserContext) {


    }

    public void parseStatementNode(Element elt) {
        String id = elt.getAttribute(ID_ATTRIBUTE);
        String nodeName = elt.getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        Boolean flushCache;
        if (elt.hasAttribute("flushCache")) {
            flushCache = Boolean.valueOf(elt.getAttribute("flushCache"));
        } else {
            flushCache = !isSelect;
        }
        Boolean useCache = Boolean.valueOf(elt.getAttribute("useCache"));
        Boolean resultOrdered = Boolean.valueOf(elt.getAttribute("resultOrdered"));

        String parameterType = elt.getAttribute("parameterType");
        Class<?> parameterTypeClass = TypeAliasRegistry.resolveAlias(parameterType);

        String lang = elt.getAttribute("lang");
        Class<? extends LanguageDriver> langClass = TypeAliasRegistry.resolveAlias(lang);
        LanguageDriver langDriver = configuration.getLanguageDriver(langClass);

        // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
        KeyGenerator keyGenerator;
        String keyStatementId = id + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        keyStatementId = builderAssistant.applyCurrentNamespace(keyStatementId, true);
        if (configuration.hasKeyGenerator(keyStatementId)) {
            keyGenerator = configuration.getKeyGenerator(keyStatementId);
        } else {
            boolean useGeneratedKeys;
            if (elt.hasAttribute("useGeneratedKeys")) {
                useGeneratedKeys = Boolean.valueOf(elt.getAttribute("useGeneratedKeys"));
            } else {
                useGeneratedKeys = configuration.isUseGeneratedKeys() && SqlCommandType.INSERT.equals(sqlCommandType);
            }
            keyGenerator = useGeneratedKeys ? Jdbc3KeyGenerator.INSTANCE : NoKeyGenerator.INSTANCE;
        }

        //todo
        SqlSource sqlSource = null;

        StatementType statementType = StatementType.valueOf(getAttribute(elt, "statementType", StatementType.PREPARED.toString()));
        Integer fetchSize = getIntAttribute(elt, "fetchSize", null);
        Integer timeout = getIntAttribute(elt, "timeout", null);
        String parameterMap = elt.getAttribute("parameterMap");
        String resultType = elt.getAttribute("resultType");
        Class<?> resultTypeClass = TypeAliasRegistry.resolveAlias(resultType);
        String resultMap = elt.getAttribute("resultMap");
        String resultSetType = elt.getAttribute("resultSetType");
        ResultSetType resultSetTypeEnum = ResultSetType.parse(resultSetType);
        String keyProperty = elt.getAttribute("keyProperty");
        String keyColumn = elt.getAttribute("keyColumn");
        String resultSets = elt.getAttribute("resultSets");

        builderAssistant.addMappedStatement(id, sqlSource, statementType, sqlCommandType,
                fetchSize, timeout, parameterMap, parameterTypeClass, resultMap, resultTypeClass,
                resultSetTypeEnum, flushCache, useCache, resultOrdered,
                keyGenerator, keyProperty, keyColumn, "", langDriver, resultSets);
    }

    private String getAttribute(Element elt, String name, String defaultValue) {
        if (elt.hasAttribute(name)) {
            return elt.getAttribute(name);
        }
        return defaultValue;
    }

    private Integer getIntAttribute(Element elt, String name, Integer defaultValue) {
        if (elt.hasAttribute(name)) {
            return Integer.parseInt(elt.getAttribute(name));
        }
        return defaultValue;
    }
}