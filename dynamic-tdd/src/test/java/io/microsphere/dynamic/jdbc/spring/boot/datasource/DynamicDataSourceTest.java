package io.microsphere.dynamic.jdbc.spring.boot.datasource;


import io.microsphere.dynamic.jdbc.spring.boot.config.DynamicJdbcConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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


}
