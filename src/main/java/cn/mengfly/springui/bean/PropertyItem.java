package cn.mengfly.springui.bean;

/**
 * @author Mengfly
 * @date 2021/7/29 16:03
 */
public class PropertyItem {

    private String showName;
    private String propertyName;
    private Object value;

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        SpringUiCfgProperty.putProperty(propertyName, String.valueOf(value));
    }


}
