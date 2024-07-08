package io.microsphere.dynamic.jdbc.spring.boot.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static io.microsphere.dynamic.jdbc.spring.boot.util.DynamicJdbcConfigUtils.generateDynamicJdbcConfigBeanName;
import static io.microsphere.dynamic.jdbc.spring.boot.util.DynamicJdbcConfigUtils.generateSynthesizedPropertySourceName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DynamicJdbcContextProcessorTest {
    private DynamicJdbcContextProcessor dynamicJdbcContextProcessor;
    private DynamicJdbcConfig dynamicJdbcConfig;
    private String dynamicJdbcConfigPropertyName;

    @BeforeEach
    void setUp() throws Exception {
        dynamicJdbcContextProcessor = new DynamicJdbcContextProcessor();
        URL resource = this.getClass().getClassLoader().getResource("dynamic/jdbc/valid-test-jdbc-config.json");
        String content = IOUtils.toString(resource, StandardCharsets.UTF_8);
        dynamicJdbcConfig = new ObjectMapper().readValue(content, DynamicJdbcConfig.class);
        dynamicJdbcConfigPropertyName = "test";
    }


    @Nested
    class ProcessDynamicTest {


        @Test
        void shouldRegisterDynamicDataSourceBeanDefinition() {
            ConfigurableApplicationContext context = new GenericApplicationContext();
            dynamicJdbcContextProcessor.process(dynamicJdbcConfig, dynamicJdbcConfigPropertyName, context);

            BeanDefinition dynamicJdbcDynamicDataSource = context.getBeanFactory().getBeanDefinition("DynamicJdbcDynamicDataSource");
            assertNotNull(dynamicJdbcDynamicDataSource);
        }

        @Test
        void shouldRemoveDataSourceConfigs() {
            ConfigurableApplicationContext context = new GenericApplicationContext();
            assertFalse(dynamicJdbcConfig.getDataSource().isEmpty());
            assertNull(dynamicJdbcConfig.getHighAvailabilityDataSource());

            dynamicJdbcContextProcessor.process(dynamicJdbcConfig, dynamicJdbcConfigPropertyName, context);

            assertEquals(0, dynamicJdbcConfig.getDataSource().size());
            assertEquals(0, dynamicJdbcConfig.getHighAvailabilityDataSource().size());
            assertNull(dynamicJdbcConfig.getShardingSphere());
        }
    }

    @Nested
    class ProcessDynamicJdbcConfigurationPropertiesTest {
        @Test
        void shouldAddSynthesizedPropertySource() {
            ConfigurableApplicationContext context = new GenericApplicationContext();

            dynamicJdbcContextProcessor.process(dynamicJdbcConfig, dynamicJdbcConfigPropertyName, context);

            ConfigurableEnvironment environment = context.getEnvironment();
            Assertions.assertTrue(environment.getPropertySources()
                    .contains(generateSynthesizedPropertySourceName(dynamicJdbcConfigPropertyName)));
        }
    }

    @Nested
    class RegisterDynamicJdbcConfigBeanDefinitions {
        @Test
        void shouldRemoveDataSourceConfigs() {
            ConfigurableApplicationContext context = new GenericApplicationContext();

            dynamicJdbcContextProcessor.process(dynamicJdbcConfig, dynamicJdbcConfigPropertyName, context);

            BeanDefinition dynamicJdbcConfigBean = context.getBeanFactory().getBeanDefinition(generateDynamicJdbcConfigBeanName(dynamicJdbcConfig, dynamicJdbcConfigPropertyName));
            assertNotNull(dynamicJdbcConfigBean);

            BeanDefinition mybatisMapperScanConfiguration = context.getBeanFactory().getBeanDefinition("mybatisMapperScanConfiguration");
            assertNotNull(mybatisMapperScanConfiguration);

        }
    }

}
