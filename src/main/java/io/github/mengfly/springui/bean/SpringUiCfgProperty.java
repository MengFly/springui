package io.github.mengfly.springui.bean;

import io.github.mengfly.springui.util.ConfigFileManager;
import io.github.mengfly.springui.util.StringUtil;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * 软件配置信息
 *
 * @author Mengfly
 * @date 2021/8/4 16:34
 */
public class SpringUiCfgProperty {

    private static final String ITEM_PREFIX = "spring.ui.items[%d]";
    private static final ConfigFileManager CUSTOM_CFG = new ConfigFileManager("spring-ui-custom.cfg");

    private String name = "";
    private String icon = "";
    private Boolean exitOnClose = false;
    private Boolean singletonStart = false;
    private Boolean sysTrayEnable = true;
    private String sysTrayMsg = "";
    private String openUrlOnStared = "";
    private final Map<String, String> items = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public Boolean getExitOnClose() {
        return exitOnClose;
    }

    public Boolean getSingletonStart() {
        return singletonStart;
    }

    public Boolean getSysTrayEnable() {
        return sysTrayEnable;
    }

    public String getSysTrayMsg() {
        return sysTrayMsg;
    }

    public String getOpenUrlOnStared() {
        return openUrlOnStared;
    }

    private void loadItems(Environment environment) {
        for (int i = 0; ; i++) {
            String id = environment.getProperty(String.format(ITEM_PREFIX, i) + ".id");
            if (StringUtil.isNotNullOrEmpty(id)) {
                String name = environment.getProperty(String.format(ITEM_PREFIX, i) + ".name");
                items.put(id, name);
            } else {
                return;
            }
        }
    }

    public List<PropertyItem> listPropertyItems(ConfigurableEnvironment environment) {
        List<PropertyItem> propertyItems = new ArrayList<>();
        for (Map.Entry<String, String> entry : items.entrySet()) {
            PropertyItem itemObj = new PropertyItem();
            itemObj.setShowName(entry.getValue());
            itemObj.setPropertyName(entry.getKey());
            String property = getProperty(entry.getKey());
            if (StringUtil.isNotNullOrEmpty(property)) {
                itemObj.setValue(property);
            } else {
                itemObj.setValue(environment.getProperty(entry.getKey(), ""));
            }
            propertyItems.add(itemObj);
        }
        return propertyItems;
    }

    public static SpringUiCfgProperty loadFromEnvironment(Environment environment) {
        SpringUiCfgProperty property = new SpringUiCfgProperty();
        property.name = environment.getProperty("spring.ui.title", "Spring-Ui");
        property.icon = environment.getProperty("spring.ui.icon", "");
        property.sysTrayEnable = environment.getProperty("spring.ui.tray.enable", Boolean.class, true);
        property.exitOnClose = environment.getProperty("spring.ui.exitOnClose", Boolean.class, false);
        property.sysTrayMsg = environment.getProperty("spring.ui.tray.hiddenMsg", "");
        property.singletonStart = environment.getProperty("spring.ui.singletonStart", Boolean.class, false);
        property.openUrlOnStared = environment.getProperty("spring.ui.openUrlOnStared", "");
        property.loadItems(environment);
        return property;
    }

    public static String getProperty(String property) {
        return CUSTOM_CFG.getItem(property);
    }

    static void putProperty(String id, String property) {
        CUSTOM_CFG.putItem(id, property);
    }

    public Map<String, Object> cfgMap() {
        Map<String, Object> map = new HashMap<>(CUSTOM_CFG.keySet().size());
        for (String key : items.keySet()) {
            String item = getProperty(key);
            if (StringUtil.isNotNullOrEmpty(item)) {
                map.put(key, item);
            }
        }
        return map;
    }

    public static void close() {
        ConfigFileManager.close(CUSTOM_CFG);
    }


}
