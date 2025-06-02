import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;
import model.Event;
import model.Organizer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EventDialog extends Dialog<Event> {

    private TextField titleField = new TextField();
    private TextField locationField = new TextField();
    // 替换原来的timeField为日期时间选择器
    private DatePicker datePicker = new DatePicker();
    private Spinner<Integer> hourSpinner = new Spinner<>(0, 23, 9);
    private Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, 0);
    private TextField organizerField = new TextField();
    private TextField capacityField = new TextField();
    private Organizer currentOrganizer;

    public EventDialog() {
        this(null, null);
    }

    public EventDialog(Event event) {
        this(event, null);
    }

    public EventDialog(Event event, Organizer currentOrganizer) {
        this.currentOrganizer = currentOrganizer;

        setTitle(event == null ? "新增活動" : "編輯活動");
        setHeaderText(event == null ? "請輸入新活動的資訊" : "修改活動資訊");

        // 設定對話框按鈕
        ButtonType saveButtonType = new ButtonType("儲存", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 建立輸入欄位的網格佈局
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 設定輸入欄位
        titleField.setPromptText("請輸入活動標題");
        locationField.setPromptText("請輸入活動地點");
        organizerField.setPromptText("請輸入主辦單位");
        capacityField.setPromptText("請輸入名額數量");

        // 設定日期選擇器
        datePicker.setPromptText("請選擇活動日期");
        datePicker.setValue(LocalDate.now()); // 預設為今天

        // 設定時間選擇器
        setupTimeSpinners();

        // 如果有當前主辦人，自動填入並設為唯讀
        if (currentOrganizer != null) {
            organizerField.setText(currentOrganizer.getName());
            organizerField.setEditable(false);
            organizerField.setStyle("-fx-background-color: #f0f0f0;");
        }

        // 如果是編輯模式，填入現有資料
        if (event != null) {
            titleField.setText(event.getTitle());
            locationField.setText(event.getLocation());

            // 解析時間字符串並設定日期時間選擇器
            parseAndSetDateTime(event.getTime());

            organizerField.setText(event.getOrganizer().getName());
            organizerField.setEditable(false);
            organizerField.setStyle("-fx-background-color: #f0f0f0;");

            capacityField.setText(String.valueOf(event.getCapacity()));
        }

        // 創建時間選擇容器
        HBox timeBox = createTimeSelectionBox();

        // 將欄位加入網格
        grid.add(new Label("標題:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("地點:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("日期:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("時間:"), 0, 3);
        grid.add(timeBox, 1, 3);
        grid.add(new Label("主辦單位:"), 0, 4);
        grid.add(organizerField, 1, 4);
        grid.add(new Label("名額:"), 0, 5);
        grid.add(capacityField, 1, 5);

        // 啟用/停用儲存按鈕
        javafx.scene.Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // 監聽輸入欄位變化
        ChangeListener<Object> formListener = (observable, oldValue, newValue) -> {
            saveButton.setDisable(
                    titleField.getText().trim().isEmpty() ||
                            locationField.getText().trim().isEmpty() ||
                            datePicker.getValue() == null ||
                            capacityField.getText().trim().isEmpty()
            );
        };

        // 加到所有輸入欄位上
        titleField.textProperty().addListener(formListener);
        locationField.textProperty().addListener(formListener);
        datePicker.valueProperty().addListener(formListener);
        capacityField.textProperty().addListener(formListener);

        // 啟動時也先檢查一次
        saveButton.setDisable(
                titleField.getText().trim().isEmpty() ||
                        locationField.getText().trim().isEmpty() ||
                        datePicker.getValue() == null ||
                        capacityField.getText().trim().isEmpty()
        );

        getDialogPane().setContent(grid);

        // 設定焦點
        Platform.runLater(() -> titleField.requestFocus());

        // 設定結果轉換器
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String title = titleField.getText().trim();
                    String location = locationField.getText().trim();
                    LocalDate date = datePicker.getValue();
                    int hour = hourSpinner.getValue();
                    int minute = minuteSpinner.getValue();
                    String organizerName = organizerField.getText().trim();
                    int capacity = Integer.parseInt(capacityField.getText().trim());

                    // 基本驗證
                    if (title.isEmpty() || location.isEmpty() || date == null ||
                            organizerName.isEmpty() || capacity <= 0) {
                        showValidationError("請確認所有欄位都已正確填入，且名額為正整數。");
                        return null;
                    }

                    // 組合日期時間
                    LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(hour, minute));
                    String formattedDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    // 使用當前主辦人或從欄位建立主辦人物件
                    Organizer organizer;
                    if (currentOrganizer != null) {
                        organizer = currentOrganizer;
                    } else {
                        organizer = new Organizer("", organizerName, "");
                    }

                    // 創建活動物件
                    return new Event("", title, location, formattedDateTime, capacity, organizer);

                } catch (NumberFormatException e) {
                    showValidationError("名額必須是有效的正整數。");
                    return null;
                }
            }
            return null;
        });
    }

    private void setupTimeSpinners() {
        // 設定小時選擇器
        hourSpinner.setEditable(true);
        hourSpinner.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 9,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*") && newText.length() <= 2) {
                        try {
                            int value = Integer.parseInt(newText);
                            if (value >= 0 && value <= 23) {
                                return change;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    return null;
                }));

        // 設定分鐘選擇器
        minuteSpinner.setEditable(true);
        minuteSpinner.getEditor().setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*") && newText.length() <= 2) {
                        try {
                            int value = Integer.parseInt(newText);
                            if (value >= 0 && value <= 59) {
                                return change;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                    return null;
                }));

        // 設定spinner寬度
        hourSpinner.setPrefWidth(70);
        minuteSpinner.setPrefWidth(70);
    }

    private HBox createTimeSelectionBox() {
        HBox timeBox = new HBox(5);
        timeBox.getChildren().addAll(
                hourSpinner,
                new Label(":"),
                minuteSpinner
        );
        return timeBox;
    }

    private void parseAndSetDateTime(String timeString) {
        try {
            // 嘗試解析 "yyyy-MM-dd HH:mm" 格式
            LocalDateTime dateTime = LocalDateTime.parse(timeString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            datePicker.setValue(dateTime.toLocalDate());
            hourSpinner.getValueFactory().setValue(dateTime.getHour());
            minuteSpinner.getValueFactory().setValue(dateTime.getMinute());

        } catch (DateTimeParseException e) {
            // 如果解析失敗，使用預設值
            datePicker.setValue(LocalDate.now());
            hourSpinner.getValueFactory().setValue(9);
            minuteSpinner.getValueFactory().setValue(0);

            // 可以選擇顯示警告
            System.err.println("無法解析時間格式: " + timeString + "，使用預設值");
        }
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("輸入錯誤");
        alert.setHeaderText("資料驗證失敗");
        alert.setContentText(message);
        alert.showAndWait();
    }
}