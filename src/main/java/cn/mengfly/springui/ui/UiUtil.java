package cn.mengfly.springui.ui;

import cn.mengfly.springui.util.StringUtil;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Mengfly
 * @date 2021/8/3 9:08
 */
class UiUtil {
    private static final ScheduledExecutorService UI_REFRESH_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    static <T> void bindPropertyScheduler(Property<T> property, long delay, TimeUnit timeUnit, Supplier<T> valueSupplier) {
        UI_REFRESH_EXECUTOR.scheduleWithFixedDelay(() -> {
            T value = valueSupplier.get();
            Platform.runLater(() -> {
                property.setValue(value);
            });
        }, 0L, delay, timeUnit);
    }

    static void addPropertyListenerOnUiWhenTrue(Property<Boolean> property, Runnable runnable) {
        property.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(runnable);
            }
        });
    }

    static <T> void addPropertyListenerOnUi(Property<T> property, Consumer<T> newValueListener) {
        property.addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> newValueListener.accept(newValue)));
    }

    private static final Log log = LogFactory.getLog(UiUtil.class);

    static class ApplicationUi {

        static Button button(String text, EventHandler<ActionEvent> eventHandler) {
            Button button = new Button(text);
            button.setPadding(new Insets(10));
            button.setMinWidth(64);
            button.setOnAction(eventHandler);
            return button;
        }

        static ProgressBar progressBar(DoubleProperty property) {
            ProgressBar progressBar = new ProgressBar();
            progressBar.setMinHeight(24);
            if (property != null) {
                progressBar.progressProperty().bind(property);
            }
            return progressBar;
        }

        static Label label(String text, StringProperty property) {
            Label label = new Label(text);
            if (property != null) {
                label.textProperty().bind(property);
            }
            return label;
        }

        static HBox hbox(double spacing, Pos alignment) {
            HBox hBox = new HBox(spacing);
            hBox.setAlignment(alignment);
            return hBox;
        }
    }

    static class SystemTrayManagement {
        private static SystemTrayManagement mInstance;

        private TrayIcon trayIcon;
        private SystemTray systemTray;

        private Stage stage;
        private EventHandler<WindowEvent> stagePreRequest;
        private EventHandler<WindowEvent> stageNowRequest;

        SystemTrayManagement() {
            System.setProperty("java.awt.headless", "false");
            // 检查系统是否支持托盘
            if (!SystemTray.isSupported()) {
                log.warn("System not support tray, can't enable tray");
                return;
            }
            stageNowRequest = event -> hide();
            URL iconUrl = SpringUiModel.getIconUrl();
            Image image = new ImageIcon(iconUrl).getImage();
            trayIcon = new TrayIcon(image, SpringUiModel.property.getName());
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> showStage());
            initTrayMenu();
            systemTray = SystemTray.getSystemTray();
        }

        private void initTrayMenu() {
            PopupMenu popupMenu = new PopupMenu();
            MenuItem showMenuItem = new MenuItem("打开");
            MenuItem exitMenuItem = new MenuItem("退出");
            exitMenuItem.addActionListener(e -> System.exit(0));
            showMenuItem.addActionListener(e -> showStage());
            popupMenu.add(showMenuItem);
            popupMenu.add(exitMenuItem);
            trayIcon.setPopupMenu(popupMenu);
        }


        public void listen(Stage stage) {
            if (systemTray == null || stage == null) {
                return;
            }
            if (this.stage != null) {
                this.stage.setOnCloseRequest(stagePreRequest);
            }
            stagePreRequest = stage.getOnCloseRequest();
            this.stage = stage;
            this.stage.setOnCloseRequest(stageNowRequest);
        }

        private void showStage() {
            if (stage == null) {
                return;
            }
            Platform.runLater(() -> {
                if (stage.isIconified()) {
                    stage.setIconified(false);
                }
                if (!stage.isShowing()) {
                    stage.show();
                }
                stage.toFront();
            });
            systemTray.remove(trayIcon);
        }

        void hide() {
            Platform.runLater(() -> stage.hide());
            try {
                systemTray.add(trayIcon);
                if (StringUtil.isNotNullOrEmpty(SpringUiModel.property.getSysTrayMsg())) {
                    trayIcon.displayMessage(null, SpringUiModel.property.getSysTrayMsg(), TrayIcon.MessageType.NONE);
                }
            } catch (AWTException e) {
                log.error("Add Tray Icon fail:", e);
            }
        }

        public static SystemTrayManagement getInstance() {
            if (mInstance == null) {
                mInstance = new SystemTrayManagement();
            }
            return mInstance;
        }


    }
}
