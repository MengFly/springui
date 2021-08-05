package io.github.mengfly.springui.ui;

import io.github.mengfly.springui.UiLogFilter;
import io.github.mengfly.springui.bean.PropertyItem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Mengfly
 * @date 2021/7/29 14:40
 */
public class SpringUi extends Application {
    private static final int MAX_LOG_SIZE = 200;

    private Stage primaryStage;
    private TextArea logArea;
    private Queue<Integer> logLength;
    private static SpringUi instance;

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        VBox root = sceneRoot();
        TitledPane settingPane = configPanel();
        VBox.setVgrow(settingPane, Priority.ALWAYS);
        VBox logPane = logAreaLayout();
        HBox statusAndOptionLayout = statusAndOptionLayout();

        root.getChildren().addAll(settingPane, logPane, new Separator(), statusAndOptionLayout);

        primaryStage.setScene(new Scene(root));

        initStage(primaryStage);
        initSystemTrayIcon();
        SpringUiModel.flushLog();
        primaryStage.show();
    }

    /**
     * Ui Frame Root Node
     */
    private VBox sceneRoot() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        return root;
    }

    /**
     * properties table
     */
    private TitledPane configPanel() {
        TableView<PropertyItem> propertyItemTableView = new TableView<>(SpringUiModel.PROPERTIES_PROPERTY);
        initTableCell(propertyItemTableView);
        TitledPane titledPane = new TitledPane("软件配置", propertyItemTableView);
        titledPane.setExpanded(true);
        return titledPane;
    }

    private void initTableCell(TableView<PropertyItem> propertyItemTableView) {
        propertyItemTableView.setEditable(true);
        propertyItemTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Config-Name
        TableColumn<PropertyItem, String> nameColumn = new TableColumn<>("配置项");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("showName"));
        nameColumn.setEditable(false);
        propertyItemTableView.getColumns().add(nameColumn);

        // Config-Value
        TableColumn<PropertyItem, String> valueColumn = new TableColumn<>("配置内容");
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(param -> new TextFieldTableCell<>(new DefaultStringConverter()));
        valueColumn.setEditable(true);
        valueColumn.setOnEditCommit(event -> event.getRowValue().setValue(event.getNewValue()));
        propertyItemTableView.getColumns().add(valueColumn);
    }

    /**
     * Application Log Layout
     */
    private VBox logAreaLayout() {
        VBox logAreaLayout = new VBox(5);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setMinHeight(200);
        logArea.setFont(Font.font(null, FontWeight.NORMAL, FontPosture.REGULAR, 14));
        logArea.setText("在logback配置文件中配置cn.mengfly.springui.UiLogFilter启动日志输出");
        logLength = new ArrayDeque<>();

        Label label = new Label("运行日志:");
        logAreaLayout.getChildren().addAll(label, logArea);
        VBox.setVgrow(logAreaLayout, Priority.ALWAYS);
        return logAreaLayout;
    }

    private HBox statusAndOptionLayout() {
        HBox statusAndOptionLayout = new HBox();
        statusAndOptionLayout.setAlignment(Pos.CENTER_LEFT);

        HBox statusLayout = statusPane();
        HBox memoryLayout = memoryLayout();
        HBox.setMargin(memoryLayout, new Insets(0, 0, 0, 20));
        HBox optionLayout = optionLayout();
        HBox.setHgrow(optionLayout, Priority.ALWAYS);
        statusAndOptionLayout.getChildren().addAll(statusLayout, memoryLayout, optionLayout);
        return statusAndOptionLayout;
    }

    /**
     * Application Running Status Layout
     */
    private HBox statusPane() {
        HBox statusLayout = new HBox(10);
        statusLayout.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label();
        Circle statusCircle = new Circle(10);
        // bind Color
        statusLabel.textFillProperty().bind(SpringUiModel.statusColorProperty());
        statusCircle.fillProperty().bind(SpringUiModel.statusColorProperty());
        // bind text
        statusLabel.textProperty().bind(SpringUiModel.statusProperty());
        statusLayout.getChildren().addAll(statusCircle, statusLabel);
        return statusLayout;
    }

    /**
     * Option Layout
     */
    private HBox optionLayout() {
        HBox optionLayout = UiUtil.ApplicationUi.hbox(10, Pos.CENTER_RIGHT);

        Button startButton = UiUtil.ApplicationUi.button("启动", e -> SpringUiModel.clearLatch());
        startButton.disableProperty().bind(SpringUiModel.startingProperty());
        Button exitButton = UiUtil.ApplicationUi.button("退出", e -> System.exit(0));
        optionLayout.getChildren().addAll(exitButton, startButton);
        UiUtil.addPropertyListenerOnUiWhenTrue(SpringUiModel.runningProperty(), () -> {
            optionLayout.getChildren().remove(startButton);
        });
        return optionLayout;
    }

    /**
     * Show Application Memory of usage And usage percent
     */
    private HBox memoryLayout() {
        HBox hBox = UiUtil.ApplicationUi.hbox(5, Pos.CENTER_LEFT);

        hBox.getChildren().addAll(
                UiUtil.ApplicationUi.label("Memory Usage:", null),
                UiUtil.ApplicationUi.progressBar(SpringUiModel.memoryPercentProperty()),
                UiUtil.ApplicationUi.label("", SpringUiModel.memoryStatusProperty())
        );
        return hBox;
    }

    private void initSystemTrayIcon() {
        if (SpringUiModel.property.getSysTrayEnable()) {
            UiUtil.SystemTrayManagement.getInstance().listen(primaryStage);
        }
    }

    public static boolean addLog(UiLogFilter.UiLog log) {
        if (instance == null) {
            return false;
        }
        instance.appendLog(log);
        return true;
    }

    private void appendLog(UiLogFilter.UiLog log) {
        if (logLength.isEmpty()) {
            logArea.clear();
        }
        while (logLength.size() > MAX_LOG_SIZE) {
            Integer poll = logLength.poll();
            logArea.deleteText(0, poll);
        }
        String content = log.toString();
        logArea.appendText(content);
        logLength.add(content.length());
    }

    private void initStage(Stage primaryStage) {
        Image icon = SpringUiModel.getIcon();
        primaryStage.getIcons().add(icon);
        primaryStage.setOnCloseRequest(event -> {
            if (SpringUiModel.property.getExitOnClose()) {
                Platform.setImplicitExit(true);
                System.exit(0);
            } else {
                SpringUiModel.clearLatch();
            }
        });
        primaryStage.titleProperty().set(SpringUiModel.property.getName());
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
    }

}
