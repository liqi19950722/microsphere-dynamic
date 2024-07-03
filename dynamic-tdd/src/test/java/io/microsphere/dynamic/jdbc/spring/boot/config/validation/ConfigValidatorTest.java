package io.microsphere.dynamic.jdbc.spring.boot.config.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import io.microsphere.dynamic.jdbc.spring.boot.datasource.validation.DataSourcePropertiesModuleValidator;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ConfigValidatorTest {
    @Nested
    class AbstractConfigValidatorTest {
        @Test
        @DisplayName("should invoke Aware interface by AbstractConfigValidator")
        void shouldInvokeAwareInterfaceByAbstractConfigValidator() {
            DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
            GenericApplicationContext applicationContext = new GenericApplicationContext(beanFactory);

            ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
            applicationContext.setEnvironment(environment);
            ClassLoader classLoader = mock(ClassLoader.class);
            applicationContext.setClassLoader(classLoader);

            applicationContext.registerBean(AbstractConfigValidatorBean.class);

            applicationContext.refresh();

            AbstractConfigValidatorBean bean = applicationContext.getBean(AbstractConfigValidatorBean.class);
            assertNotNull(bean);
            assertSame(applicationContext, bean.getApplicationContext());
            assertSame(beanFactory, bean.getBeanFactory());
            assertSame(classLoader, bean.getBeanClassLoader());
        }
    }

    private static class AbstractConfigValidatorBean extends AbstractConfigValidator {
        @Override
        public void validate(DynamicJdbcConfig dynamicJdbcConfig, String dynamicJdbcConfigPropertyName, ValidationErrors validationErrors) {

        }

        public BeanFactory getBeanFactory() {
            return beanFactory;
        }

        public ClassLoader getBeanClassLoader() {
            return classLoader;
        }

        public ConfigurableApplicationContext getApplicationContext() {
            return context;
        }
    }


    @Nested
    class DynamicJdbcConfigValidatorTest {

        ConfigValidator configValidator = new DynamicJdbcConfigValidator();

        @Test
        void shouldBeNotValidWhenNameIsNull() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/no-name-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertFalse(validationErrors.isValid());
            assertThat(validationErrors.toString())
                    .contains("must contain 'name' attribute");
        }

        @Test
        void shouldBeNotValidWhenNoModule() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/no-module-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertFalse(validationErrors.isValid());
            assertThat(validationErrors.toString())
                    .contains("must contain one of modules");
        }

        @Test
        void shouldBeValidWhenNameIsNotNullAndAtLeastOneModule() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/valid-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertTrue(validationErrors.isValid());
        }
    }

    @Nested
    class DataSourcePropertiesModuleValidatorTest {

        ConfigValidator configValidator = new DataSourcePropertiesModuleValidator();

        @Test
        void shouldBeValidWhenHasDataSourceModule() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/valid-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertTrue(validationErrors.isValid());
        }

        @Test
        void shouldBeValidWhenHasHighAvailabilityDataSourceModuleWithTwoDatasouceAndDefaultZoneConfig() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/valid-ha-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertTrue(validationErrors.isValid());
        }

        @Test
        void shouldBeNotValidWhenHasNeitherDataSourceAndHighAvailabilityNorDataSourceModule() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/no-datasource-ha-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertFalse(validationErrors.isValid());
            assertThat(validationErrors.toString())
                    .contains("module must be present");
        }

        @Test
        void shouldBeNotValidWhenHasBothDataSourceAndHighAvailabilityDataSourceModule() throws Exception {
            DynamicJdbcConfig dynamicJdbcConfig = readConfig("dynamic/jdbc/both-datasource-ha-test-jdbc-config.json");
            ValidationErrors validationErrors = new ValidationErrors("test");
            configValidator.validate(dynamicJdbcConfig, "test", validationErrors);
            assertFalse(validationErrors.isValid());
            assertThat(validationErrors.toString())
                    .contains("module must not be present at the same time");
        }
    }

    private DynamicJdbcConfig readConfig(String configFile) throws Exception {

        URL resource = this.getClass().getClassLoader().getResource(configFile);
        String content = IOUtils.toString(resource, StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(content, DynamicJdbcConfig.class);
    }
}
