package io.microsphere.dynamic.jdbc.spring.boot.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.dynamic.jdbc.spring.boot.config.AbstractConfigPostProcessor;
import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import io.microsphere.dynamic.jdbc.spring.boot.datasource.config.DataSourcePropertiesConfigPostProcessor;
import io.microsphere.multiple.active.zone.ZoneContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class ConfigPostProcessorTest {

    @Nested
    class AbstractConfigPostProcessorTest {
        @Test
        @DisplayName("should invoke aware interface by AbstractConfigPostProcessor")
        void shouldInvokeAwareInterfaceByAbstractConfigPostProcessor() {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            GenericApplicationContext applicationContext = new GenericApplicationContext(beanFactory);

            ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
            applicationContext.setEnvironment(environment);
            ClassLoader classLoader = mock(ClassLoader.class);
            applicationContext.setClassLoader(classLoader);

            applicationContext.registerBean(AbstractConfigPostProcessorBean.class);

            applicationContext.refresh();

            AbstractConfigPostProcessorBean bean = applicationContext.getBean(AbstractConfigPostProcessorBean.class);
            assertNotNull(bean);
            assertSame(applicationContext, bean.getApplicationContext());
            assertSame(environment, bean.getEnvironment());
            assertSame(classLoader, bean.getBeanClassLoader());

        }
    }

    private static class AbstractConfigPostProcessorBean extends AbstractConfigPostProcessor {

        @Override
        public void postProcess(DynamicJdbcConfig dynamicJdbcConfig, String dynamicJdbcConfigPropertyName) {

        }

        public Environment getEnvironment() {
            return super.environment;
        }

        public ApplicationContext getApplicationContext() {
            return super.context;
        }

        public ClassLoader getBeanClassLoader() {
            return super.classLoader;
        }

    }

    @Nested
    class DataSourcePropertiesConfigPostProcessorTest {
        DataSourcePropertiesConfigPostProcessor dataSourcePropertiesConfigPostProcessor;
        DynamicJdbcConfig dynamicJdbcConfig;

        @TestFactory
        List<DynamicTest> ConfigPostProcessorForDynamicJdbcConfig() {

            Map<String, Executable> setups = new HashMap<>();
            setups.put("Without Zone", this::setupWithoutZone);
            setups.put("With Zone", this::setupWithZone);
            Executable exercise = this::exercise;
            Map<String, Executable> verifies = new HashMap<>();
            verifies.put("should use previous property when property not exist", this::assertShouldUsePreviousPropertyWhenPropertyNotExist);
            verifies.put("should not use previous property when property exist", this::assertShouldNotUsePreviousPropertyWhenPropertyExist);
            List<DynamicTest> list = new ArrayList<>();
            setups.forEach((setupStep, setup) ->
                    verifies.forEach((verifyStep, verify) ->
                            list.add(DynamicTest.dynamicTest(String.format("%s: %s", setupStep, verifyStep),

                                    () -> {
                                        setup.execute();
                                        exercise.execute();
                                        verify.execute();
                                    }))));
            return list;
        }

        private void exercise() {
            dataSourcePropertiesConfigPostProcessor.postProcess(dynamicJdbcConfig, "test");
        }

        private void setupWithZone() throws Exception {
            readConfig("dynamic/jdbc/test-jdbc-config-with-zone.json");
            ZoneContext.get().setZone("zone-1");
        }

        private void setupWithoutZone() throws Exception {
            readConfig("dynamic/jdbc/test-jdbc-config.json");
        }

        private void readConfig(String configFile) throws Exception {
            dataSourcePropertiesConfigPostProcessor = new DataSourcePropertiesConfigPostProcessor();
            dataSourcePropertiesConfigPostProcessor.setEnvironment(new MockEnvironment());
            dataSourcePropertiesConfigPostProcessor.afterPropertiesSet();
            URL resource = this.getClass().getClassLoader().getResource(configFile);
            String content = IOUtils.toString(resource, StandardCharsets.UTF_8);
            dynamicJdbcConfig = new ObjectMapper().readValue(content, DynamicJdbcConfig.class);
        }

        private void assertShouldUsePreviousPropertyWhenPropertyNotExist() {
            List<Map<String, String>> dataSourcePropertiesList = dynamicJdbcConfig.getDataSourcePropertiesList();

            Map<String, String> first = dataSourcePropertiesList.get(0);
            Map<String, String> second = dataSourcePropertiesList.get(1);

            assertEquals("ds", first.get("name"));
            assertEquals("com.zaxxer.hikari.HikariDataSource", first.get("type"));
            assertEquals("com.mysql.cj.jdbc.Driver", first.get("driverClassName"));
            assertEquals("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", first.get("url"));
            assertEquals("root", first.get("username"));
            assertEquals("123456", first.get("password"));

            assertEquals("test-datasource-1", second.get("name"));
            assertEquals("com.zaxxer.hikari.HikariDataSource", second.get("type"));
            assertEquals("com.mysql.cj.jdbc.Driver", second.get("driverClassName"));
            assertEquals("jdbc:mysql://127.0.0.1:3307/demo_ds?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", second.get("url"));
            assertEquals("root", second.get("username"));
            assertEquals("123456", second.get("password"));
        }

        private void assertShouldNotUsePreviousPropertyWhenPropertyExist() {
            List<Map<String, String>> dataSourcePropertiesList = dynamicJdbcConfig.getDataSourcePropertiesList();

            Map<String, String> first = dataSourcePropertiesList.get(0);
            Map<String, String> third = dataSourcePropertiesList.get(2);

            assertEquals(first.get("type"), third.get("type"));
            assertEquals("com.zaxxer.hikari.HikariDataSource", third.get("type"));
            assertEquals(first.get("driverClassName"), third.get("driverClassName"));
            assertEquals("com.mysql.cj.jdbc.Driver", third.get("driverClassName"));
            assertEquals("jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8", third.get("url"));
            assertEquals("root-1", third.get("username"));
            assertEquals("1234567", third.get("password"));
            assertEquals("ds_1", third.get("name"));
        }
    }
}
