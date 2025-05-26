import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import model.Event;
import model.Organizer;

public class EventDialog extends Dialog<Event> {

    private TextField titleField = new TextField();
    private TextField locationField = new TextField();
    private TextField timeField = new TextField();
    private TextField organizerField = new TextField();
    private TextField capacityField = new TextField();
    private Organizer currentOrganizer; // 添加當前主辦人

    public EventDialog() {
        this(null, null);
    }

    public EventDialog(Event event) {
        this(event, null);
    }

    // 新增構造函數，接受當前主辦人參數
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
        timeField.setPromptText("例：2025-06-01 09:00");
        organizerField.setPromptText("請輸入主辦單位");
        capacityField.setPromptText("請輸入名額數量");

        // 如果有當前主辦人，自動填入並設為唯讀
        if (currentOrganizer != null) {
            organizerField.setText(currentOrganizer.getName());
            organizerField.setEditable(false); // 主辦人不可編輯
            organizerField.setStyle("-fx-background-color: #f0f0f0;"); // 灰色背景表示不可編輯
        }

        // 如果是編輯模式，填入現有資料
        if (event != null) {
            titleField.setText(event.getTitle());
            locationField.setText(event.getLocation());
            timeField.setText(event.getTime());

            // 編輯模式下，主辦人欄位顯示原主辦人且不可編輯
            organizerField.setText(event.getOrganizer().getName());
            organizerField.setEditable(false);
            organizerField.setStyle("-fx-background-color: #f0f0f0;");

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
                    String organizerName = organizerField.getText().trim();
                    int capacity = Integer.parseInt(capacityField.getText().trim());

                    // 基本驗證
                    if (title.isEmpty() || location.isEmpty() || time.isEmpty() ||
                            organizerName.isEmpty() || capacity <= 0) {
                        showValidationError("請確認所有欄位都已正確填入，且名額為正整數。");
                        return null;
                    }

                    // 使用當前主辦人或從欄位建立主辦人物件
                    Organizer organizer;
                    if (currentOrganizer != null) {
                        organizer = currentOrganizer;
                    } else {
                        organizer = new Organizer("", organizerName, "");
                    }

                    // 創建活動物件（不包含ID，將在Controller中設定）
                    return new Event("", title, location, time, capacity, organizer);

                } catch (NumberFormatException e) {
                    showValidationError("名額必須是有效的正整數。");
                    return null;
                }
            }
            return null;
        });
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("輸入錯誤");
        alert.setHeaderText("資料驗證失敗");
        alert.setContentText(message);
        alert.showAndWait();
    }
}