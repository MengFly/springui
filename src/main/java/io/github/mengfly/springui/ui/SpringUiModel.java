package io.github.mengfly.springui.ui;

import io.github.mengfly.springui.UiLogFilter;
import io.github.mengfly.springui.bean.PropertyItem;
import io.github.mengfly.springui.bean.SpringUiCfgProperty;
import io.github.mengfly.springui.util.SingletonStartLock;
import io.github.mengfly.springui.util.StringUtil;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Mengfly
 */
public class SpringUiModel {
    private static final long _1M = 1024 * 1024;
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final Log log = LogFactory.getLog(SpringUiModel.class);
    private static ResourceLoader resourceLoader;
    private static CountDownLatch latch = new CountDownLatch(1);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    static SpringUiCfgProperty property;

    // ===========================================================================================
    // Property
    // ===========================================================================================

    private static DoubleProperty memoryPercentProperty;
    private static StringProperty memoryStatusProperty;
    private static ObjectProperty<Paint> statusColorProperty;
    private static StringProperty statusProperty;
    private static final BooleanProperty startingProperty = new SimpleBooleanProperty(false);
    private static final BooleanProperty runningProperty = new SimpleBooleanProperty(false);
    static final ListProperty<PropertyItem> PROPERTIES_PROPERTY
            = new SimpleListProperty<>(FXCollections.observableArrayList());

    static DoubleProperty memoryPercentProperty() {
        if (memoryPercentProperty == null) {
            memoryPercentProperty = new SimpleDoubleProperty(0);
            UiUtil.bindPropertyScheduler(memoryPercentProperty, 2, TimeUnit.SECONDS,
                    () -> (RUNTIME.totalMemory() - RUNTIME.freeMemory()) * 1. / RUNTIME.totalMemory());
        }
        return memoryPercentProperty;
    }

    static StringProperty memoryStatusProperty() {
        if (memoryStatusProperty == null) {
            memoryStatusProperty = new SimpleStringProperty();
            UiUtil.bindPropertyScheduler(memoryStatusProperty, 2, TimeUnit.SECONDS,
                    () -> String.format("(%d of %dM)",
                            (RUNTIME.totalMemory() - RUNTIME.freeMemory()) / _1M, RUNTIME.totalMemory() / _1M));
        }
        return memoryStatusProperty;
    }

    static StringProperty statusProperty() {
        if (statusProperty == null) {
            statusProperty = new SimpleStringProperty("未启动");
        }
        return statusProperty;
    }

    static ObjectProperty<Paint> statusColorProperty() {
        if (statusColorProperty == null) {
            statusColorProperty = new SimpleObjectProperty<>(Color.GRAY);
        }
        return statusColorProperty;
    }

    static BooleanProperty runningProperty() {
        return runningProperty;
    }

    static BooleanProperty startingProperty() {
        return startingProperty;
    }


    // ===========================================================================================
    // Lifecycle callbacks
    // ===========================================================================================

    public static void starting(ConfigurableApplicationContext context) {
        // 读取配置
        readConfiguration(context.getEnvironment());
        if (!checkSingletonStart()) {
            executor.execute(() -> SpringHasBeenStartedUi.launch(SpringHasBeenStartedUi.class));
        } else {
            // 启动界面
            executor.execute(() -> SpringUi.launch(SpringUi.class));
        }
        // 阻塞Spring应用
        await();
        Platform.runLater(() -> {
            statusColorProperty().setValue(Color.ORANGE);
            statusProperty().setValue("启动中");
            startingProperty.set(true);
        });
    }

    private static boolean checkSingletonStart() {
        if (SpringUiModel.property.getSingletonStart()) {
            return new SingletonStartLock().tryLock();
        }
        return true;
    }

    public static void running() {
        Platform.runLater(() -> {
            statusColorProperty().setValue(Color.DARKGREEN);
            statusProperty().setValue("运行中");
            runningProperty.set(true);
        });
        startUrl();
    }

    public static void fail(Throwable exception) {
        Platform.runLater(() -> {
            statusColorProperty().setValue(Color.RED);
            statusProperty().setValue("启动失败");
            UiUtil.ApplicationUi.showErrorAlert("启动失败", exception);
        });
    }

    static final Queue<UiLogFilter.UiLog> LOG_LIST = new ArrayDeque<>();

    public static Map<String, Object> getPropertyMap() {
        return property.cfgMap();
    }

    public static void readConfiguration(ConfigurableEnvironment environment) {
        property = SpringUiCfgProperty.loadFromEnvironment(environment);
        PROPERTIES_PROPERTY.addAll(property.listPropertyItems(environment));
    }

    public static Image getIcon() {
        URL uri = getIconUrl();
        return new Image(uri.toString(), 16, 16, false, true);
    }

    public static URL getIconUrl() {
        try {
            if (StringUtil.isNotNullOrEmpty(property.getIcon())) {
                return resourceLoader.getResource(property.getIcon()).getURL();
            } else {
                return resourceLoader.getResource("META-INF/icon.png").getURL();
            }
        } catch (IOException e) {
            log.error("can't font icon for " + property.getIcon());
        }
        return SpringUiModel.class.getResource("META-INF/icon.png");
    }

    public static void setResourceLoader(ResourceLoader resourceLoader) {
        SpringUiModel.resourceLoader = resourceLoader == null ? new DefaultResourceLoader() : resourceLoader;
    }

    public static void addLog(UiLogFilter.UiLog log) {
        if (SpringUi.addLog(log)) {
            flushLog();
        } else {
            LOG_LIST.add(log);
        }
    }

    static void startUrl() {
        if (StringUtil.isNotNullOrEmpty(property.getOpenUrlOnStared())) {
            try {
                Runtime.getRuntime().exec(String.format("explorer  %s", property.getOpenUrlOnStared()));
            } catch (IOException e) {
                log.error("start Fail for:" + property.getOpenUrlOnStared());
            }
        }
    }

    public static void flushLog() {
        while (!LOG_LIST.isEmpty()) {
            UiLogFilter.UiLog peek = LOG_LIST.peek();
            if (SpringUi.addLog(peek)) {
                LOG_LIST.poll();
            } else {
                return;
            }
        }
    }


    static void await() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    static void clearLatch() {
        while (latch.getCount() > 0) {
            latch.countDown();
        }
        latch = new CountDownLatch(1);
    }
}
