package io.github.mengfly.springui.util;

/**
 * 只允许程序启动一个实例
 *
 * @author Mengfly
 * @date 2021/8/4 9:35
 */
public class SingletonStartLock {

    public static void tryLock() {


        throw new RuntimeException("程序已经在运行中");
    }


}
