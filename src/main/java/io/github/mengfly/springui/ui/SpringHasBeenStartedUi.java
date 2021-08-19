package io.github.mengfly.springui.ui;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * @author Mengfly
 */
public class SpringHasBeenStartedUi extends Application {
    @Override
    public void start(Stage primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("启动失败");
        alert.setTitle("Info");
        alert.setContentText("软件已在运行中, 无需重新启动");
        alert.setResizable(false);
        alert.setOnCloseRequest(event -> System.exit(0));
        alert.show();
    }
}
