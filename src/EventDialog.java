import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.util.Optional;

public class EventDialog extends Dialog<Event> {

    private TextField titleField = new TextField();
    private TextField locationField = new TextField();
    private TextField timeField = new TextField();
    private TextField organizerField = new TextField();
    private TextField capacityField = new TextField();

    public EventDialog() {
        this(null);
    }

    public EventDialog(Event event) {
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
        timeField.setPromptText("例：2025-06-01 09:00");
        organizerField.setPromptText("請輸入主辦單位");
        capacityField.setPromptText("請輸入名額數量");

        // 如果是編輯模式，填入現有資料
        if (event != null) {
            titleField.setText(event.getTitle());
            locationField.setText(event.getLocation());
            timeField.setText(event.getTime());
            organizerField.setText(event.getOrganizer());
            capacityField.setText(String.valueOf(event.getCapacity()));
        }

        // 將欄位加入網格
        grid.add(new Label("標題:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("地點:"), 0, 1);
        grid.add(locationField, 1, 1);
        grid.add(new Label("時間:"), 0, 2);
        grid.add(timeField, 1, 2);
        grid.add(new Label("主辦單位:"), 0, 3);
        grid.add(organizerField, 1, 3);
        grid.add(new Label("名額:"), 0, 4);
        grid.add(capacityField, 1, 4);

        // 啟用/停用儲存按鈕
        javafx.scene.Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // 監聽輸入欄位變化
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        getDialogPane().setContent(grid);

        // 設定焦點
        Platform.runLater(() -> titleField.requestFocus());

        // 設定結果轉換器
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String title = titleField.getText().trim();
                    String location = locationField.getText().trim();
                    String time = timeField.getText().trim();
                    String organizer = organizerField.getText().trim();
                    int capacity = Integer.parseInt(capacityField.getText().trim());

                    // 基本驗證
                    if (title.isEmpty() || location.isEmpty() || time.isEmpty() ||
                            organizer.isEmpty() || capacity <= 0) {
                        showValidationError();
                        return null;
                    }

                    return new Event(title, location, time, organizer, capacity);

                } catch (NumberFormatException e) {
                    showValidationError();
                    return null;
                }
            }
            return null;
        });
    }

    private void showValidationError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("輸入錯誤");
        alert.setHeaderText("資料驗證失敗");
        alert.setContentText("請確認所有欄位都已正確填入，且名額為正整數。");
        alert.showAndWait();
    }
}