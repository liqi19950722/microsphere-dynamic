package io.microsphere.dynamic.jdbc.spring.boot.datasource;


import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import io.microsphere.dynamic.jdbc.spring.boot.context.DynamicJdbcChildContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.dynamic.jdbc.spring.boot.datasource.constants.DataSourceConstants.DYNAMIC_DATA_SOURCE_CHILD_CONTEXT_CLOSE_DELAY_PROPERTY_NAME;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DynamicDataSourceTest {

    @Nested
    class DelegateDataSourceTest {

        @TestFactory
        @DisplayName("should call actual method when call DataSource method")
        List<DynamicTest> shouldCallActualMethodWhenCallDataSourceMethod() {
            DataSource actual = mock(DataSource.class);
            DynamicDataSource dataSource = spyDynamicDataSource();
            doReturn(actual).when(dataSource).getDelegate();

            return Arrays.asList(
                    dynamicTest("should call delegate when call DataSource.connection()",
                            () -> verifyCallConnection(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.connection(String, String)",
                            () -> verifyCallConnectionWithUsernameAndPassword(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.getLogWriter()",
                            () -> verifyGetLogWriter(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.setLogWriter(PrintWriter)",
                            () -> verifySetLogWriter(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.getLoginTimeout()",
                            () -> verifyGetLoginTimeout(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.setLoginTimeout(int)",
                            () -> verifySetLoginTimeout(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.getParentLogger()",
                            () -> verifyGetParentLogger(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.unwrap(Class)",
                            () -> verifyUnwrap(dataSource, actual)),
                    dynamicTest("should call delegate when call DataSource.isWrapperFor(Class)",
                            () -> verifyIsWrapperFor(dataSource, actual))
            );
        }

        private DynamicDataSource spyDynamicDataSource() {
            ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
            ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
            when(applicationContext.getEnvironment()).thenReturn(environment);
            when(environment.getProperty(anyString(), anyString())).thenReturn("");

            return spy(new DynamicDataSource(mock(DynamicJdbcConfig.class),
                    "anyPropertyName", applicationContext));
        }

        private void verifyIsWrapperFor(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            Class<?> anyClass = any(Class.class);
            dataSource.isWrapperFor(anyClass);
            verify(actual).isWrapperFor(anyClass);
        }

        private void verifyUnwrap(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            Class<?> anyClass = any(Class.class);
            dataSource.unwrap(anyClass);
            verify(actual).unwrap(anyClass);
        }

        private void verifyGetParentLogger(DynamicDataSource dataSource, DataSource actual) throws SQLFeatureNotSupportedException {
            dataSource.getParentLogger();
            verify(actual).getParentLogger();
        }

        private void verifySetLoginTimeout(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            int timeout = any(int.class);
            dataSource.setLoginTimeout(timeout);
            verify(actual).setLoginTimeout(timeout);
        }

        private void verifyGetLoginTimeout(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            dataSource.getLoginTimeout();
            verify(actual).getLoginTimeout();
        }

        private void verifySetLogWriter(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            PrintWriter printWriter = any(PrintWriter.class);
            dataSource.setLogWriter(printWriter);
            verify(actual).setLogWriter(printWriter);
        }

        private void verifyGetLogWriter(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            dataSource.getLogWriter();
            verify(actual).getLogWriter();
        }

        private void verifyCallConnectionWithUsernameAndPassword(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            dataSource.getConnection(anyString(), anyString());
            verify(actual).getConnection();
        }

        private void verifyCallConnection(DynamicDataSource dataSource, DataSource actual) throws SQLException {
            dataSource.getConnection();
            verify(actual).getConnection();
        }
    }

    @Nested
    class DynamicDataSourceCoreFunctionalityTest {
        DynamicJdbcChildContext first;
        DynamicJdbcChildContext second;
        DynamicJdbcConfig dynamicJdbcConfig;
        ConfigurableApplicationContext parent;
        DataSource dataSource1;
        DataSource dataSource2;

        @BeforeEach
        void setup() throws SQLException {
            ConfigurableEnvironment environment = mock(ConfigurableEnvironment.class);
            when(environment.getProperty(eq(DYNAMIC_DATA_SOURCE_CHILD_CONTEXT_CLOSE_DELAY_PROPERTY_NAME), eq(Duration.class), any(Duration.class))).thenReturn(ofSeconds(60));

            this.parent = mock(ConfigurableApplicationContext.class);
            when(parent.getEnvironment()).thenReturn(environment);

            this.dynamicJdbcConfig = mock(DynamicJdbcConfig.class);
            this.first = mock(DynamicJdbcChildContext.class);
            this.second = mock(DynamicJdbcChildContext.class);
            Map<String, DataSource> firstMap = new HashMap<>();
            this.dataSource1 = mock(DataSource.class);
            firstMap.put("dataSource", dataSource1);
            Map<String, DataSource> secondMap = new HashMap<>();
            this.dataSource2 = mock(DataSource.class);
            secondMap.put("another", dataSource2);
            when(first.getBeansOfType(DataSource.class)).thenReturn(firstMap);
            when(second.getBeansOfType(DataSource.class)).thenReturn(secondMap);
            when(dataSource1.getLoginTimeout()).thenReturn(1);
            when(dataSource2.getLoginTimeout()).thenReturn(2);

        }

        @Test
        void should_initialize_a_data_source_when_call_initializeDataSource() {

            try (MockedStatic<DynamicDataSource> dynamicDataSourceMockedStatic = mockStatic(DynamicDataSource.class)) {
                dynamicDataSourceMockedStatic
                        .when(() -> DynamicDataSource.getDynamicJdbcChildContext(anyString(), any(ConfigurableApplicationContext.class), any(DynamicJdbcConfig.class)))
                        .thenReturn(first, second);

                DynamicDataSource dynamicDataSource = new DynamicDataSource(dynamicJdbcConfig, "anyPropertyName", parent);
                dynamicDataSource.initializeDataSource();
                DataSource actualDataSource = dynamicDataSource.getDelegate();

                assertSame(dataSource1, actualDataSource);
                assertEquals(1, actualDataSource.getLoginTimeout());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }

        @Test
        void should_initialize_another_data_source_when_call_initializeDataSource_twice() {

            try (MockedStatic<DynamicDataSource> dynamicDataSourceMockedStatic = mockStatic(DynamicDataSource.class)) {
                dynamicDataSourceMockedStatic
                        .when(() -> DynamicDataSource.getDynamicJdbcChildContext(anyString(), any(ConfigurableApplicationContext.class), any(DynamicJdbcConfig.class)))
                        .thenReturn(first, second);

                DynamicDataSource dynamicDataSource = new DynamicDataSource(dynamicJdbcConfig, "anyPropertyName", parent);
                dynamicDataSource.initializeDataSource();
                DataSource firstDataSource = dynamicDataSource.getDelegate();

                dynamicDataSource.initializeDataSource(dynamicJdbcConfig, "anyPropertyName", parent);
                DataSource newDataSource = dynamicDataSource.getDelegate();
                assertNotSame(firstDataSource, newDataSource);

                assertSame(dataSource1, firstDataSource);
                assertEquals(1, firstDataSource.getLoginTimeout());

                assertSame(dataSource2, newDataSource);
                assertEquals(2, newDataSource.getLoginTimeout());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }


    }

}
