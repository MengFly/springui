package io.github.mengfly.springui.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mengfly
 * @date 2021/8/2 9:31
 */
public class StringUtil {

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0 || Objects.equals("null", str);
    }

    public static String exceptionStackTrace(Throwable e) {
        AtomicReference<String> msg = new AtomicReference<>(null);
        try (StringWriter writer = new StringWriter()) {
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                e.printStackTrace(printWriter);
            }
            msg.set(writer.toString());
        } catch (Exception ignored) {
        }
        return msg.get();
    }

    public static String nullOrElse(String checkNull, String els) {
        if (checkNull == null) {
            return els;
        }
        return checkNull;
    }

    /**
     * 根据最大长度换行
     */
    public static String reLineString(String content, int maxLength) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < content.length(); index += maxLength) {
            builder.append(content, index, Math.min(content.length(), index + maxLength)).append("\n");
        }
        return builder.toString();
    }
}
