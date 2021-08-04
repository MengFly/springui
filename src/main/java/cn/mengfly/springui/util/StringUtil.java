package cn.mengfly.springui.util;

/**
 * @author Mengfly
 * @date 2021/8/2 9:31
 */
public class StringUtil {

    public static boolean isNotNullOrEmpty(String str) {
        return str != null && str.length() > 0 && !"null".equals(str);

    }
}
