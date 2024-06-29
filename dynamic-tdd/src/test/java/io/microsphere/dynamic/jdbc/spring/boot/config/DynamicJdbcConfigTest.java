package io.microsphere.dynamic.jdbc.spring.boot.config;

import com.jayway.jsonpath.JsonPath;
import io.microsphere.dynamic.jdbc.spring.boot.util.DynamicJdbcConfigUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static io.microsphere.dynamic.jdbc.spring.boot.constants.DynamicJdbcConstants.DYNAMIC_JDBC_CONFIGS_PROPERTY_NAME_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DynamicJdbcConfigTest {
    String dynamicJdbcConfigValue;

    @BeforeEach
    void setup() throws Exception {
        URL resource = DynamicJdbcConfigTest.class.getClassLoader().getResource("dynamic/jdbc/test-dynamic-jdbc.json");
        dynamicJdbcConfigValue = IOUtils.toString(resource, StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("should read DynamicJdbcConfig from ConfigurableEnvironment")
    void shouldReadDynamicJdbcConfigFromConfigurableEnvironment() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        String propertyName = DYNAMIC_JDBC_CONFIGS_PROPERTY_NAME_PREFIX + ".test";
        mockEnvironment.setProperty(propertyName, dynamicJdbcConfigValue);
        DynamicJdbcConfig dynamicJdbcConfig = DynamicJdbcConfigUtils.getDynamicJdbcConfig(mockEnvironment, propertyName);
        assertDynamicJdbcConfig(dynamicJdbcConfig);
    }

    @Test
    @DisplayName("should read DynamicJdbcConfig from ConfigurableEnvironment by classpath")
    void shouldReadDynamicJdbcConfigFromConfigurableEnvironmentByClasspath() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        String propertyName = DYNAMIC_JDBC_CONFIGS_PROPERTY_NAME_PREFIX + ".test";
        String dynamicJdbcConfigClasspath = "classpath:dynamic/jdbc/test-dynamic-jdbc.json";
        mockEnvironment.setProperty(propertyName, dynamicJdbcConfigClasspath);
        DynamicJdbcConfig dynamicJdbcConfig = DynamicJdbcConfigUtils.getDynamicJdbcConfig(mockEnvironment, propertyName);
        assertDynamicJdbcConfig(dynamicJdbcConfig);
    }

    @Test
    @DisplayName("should not throw exception when property not in ConfigurableEnvironment")
    @Disabled
    void shouldNotThrowExceptionWhenPropertyNotInConfigurableEnvironment() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        String propertyName = DYNAMIC_JDBC_CONFIGS_PROPERTY_NAME_PREFIX + ".test";
        assertDoesNotThrow(() -> DynamicJdbcConfigUtils.getDynamicJdbcConfig(mockEnvironment, propertyName));
//        assertThrows(IllegalArgumentException.class, () -> DynamicJdbcConfigUtils.getDynamicJdbcConfig(mockEnvironment, propertyName));
    }


    private void assertDynamicJdbcConfig(DynamicJdbcConfig dynamicJdbcConfig) {
        assertNotNull(dynamicJdbcConfig);
        assertEquals(JsonPath.read(dynamicJdbcConfigValue, "$.name"), dynamicJdbcConfig.getName());
        assertEquals(JsonPath.read(dynamicJdbcConfigValue, "$.dynamic"), dynamicJdbcConfig.isDynamic());
        assertEquals(JsonPath.read(dynamicJdbcConfigValue, "$.primary"), dynamicJdbcConfig.isPrimary());
        assertEquals(JsonPath.<Integer>read(dynamicJdbcConfigValue, "$.datasource.length()"), dynamicJdbcConfig.getDataSource().size());
        assertEquals(JsonPath.read(dynamicJdbcConfigValue, "$.datasource[0].username"), dynamicJdbcConfig.getDataSource().get(0).get("username"));
        assertEquals(JsonPath.read(dynamicJdbcConfigValue, "$.datasource[0].password"), dynamicJdbcConfig.getDataSource().get(0).get("password"));
        assertEquals(JsonPath.<Set<String>>read(dynamicJdbcConfigValue, "$.ha-datasource.keys()"), dynamicJdbcConfig.getHighAvailabilityDataSource().keySet());
        assertThat(JsonPath.<List<String>>read(dynamicJdbcConfigValue, "$..name"))
                .contains(dynamicJdbcConfig.getTransaction().getName(), dynamicJdbcConfig.getShardingSphere().getName(),
                        dynamicJdbcConfig.getMybatis().getName(), dynamicJdbcConfig.getMybatisPlus().getName());
    }


}
