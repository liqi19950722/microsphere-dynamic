package io.microsphere.dynamic.jdbc.spring.boot.config.validation;

import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
}
