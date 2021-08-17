package io.github.mengfly.springui;

import io.github.mengfly.springui.bean.SpringUiCfgProperty;
import io.github.mengfly.springui.ui.SpringUiModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * @author Mengfly
 * @date 2021/7/29 13:17
 */
public class SpringUiRunListener implements SpringApplicationRunListener, Ordered {

    private static final Log log = LogFactory.getLog(SpringUiRunListener.class);

    public SpringUiRunListener(SpringApplication application, String[] ignore) {
        SpringUiModel.setResourceLoader(application.getResourceLoader());
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
    }

    @Override
    public void starting() {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        SpringUiModel.starting(context);
        addCustomPropertiesEnvironment(context.getEnvironment());
    }

    private void addCustomPropertiesEnvironment(ConfigurableEnvironment environment) {
        Map<String, Object> propertyMap = SpringUiModel.getPropertyMap();

        MapPropertySource customProperties = new MapPropertySource("springUiProperties", propertyMap);
        environment.getPropertySources().addFirst(customProperties);
        log.info(String.format("use custom properties:\n %s", propertyMap.toString()));
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        SpringUiCfgProperty.close();
        SpringUiModel.running();
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        SpringUiModel.fail(exception);
        SpringUiCfgProperty.close();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
