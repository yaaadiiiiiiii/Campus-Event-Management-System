import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RegistrationRecordController implements Initializable {

    @FXML
    private DatePicker eventDatePicker;

    @FXML
    private TextField keywordField;

    @FXML
    private Button searchButton;

    @FXML
    private Button showAllButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button clearSearchButton;

    @FXML
    private TableView<RegistrationRecord> registrationTable;

    @FXML
    private TableColumn<RegistrationRecord, String> eventNameColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> eventDateColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> eventTimeColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> locationColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> organizerColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> registrationDateColumn;

    @FXML
    private TableColumn<RegistrationRecord, String> statusColumn;

    @FXML
    private TableColumn<RegistrationRecord, Button> actionColumn;

    @FXML
    private Label totalLabel;

    @FXML
    private Button cancelSelectedButton;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    private ObservableList<RegistrationRecord> registrationData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格欄位
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        eventDateColumn.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        eventTimeColumn.setCellValueFactory(new PropertyValueFactory<>("eventTime"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        organizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizer"));
        registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 初始化資料
        registrationData = FXCollections.observableArrayList();
        registrationTable.setItems(registrationData);

        // 載入資料
        loadRegistrationData();

        System.out.println("查詢報名紀錄頁面已載入");
    }

    /**
     * 處理搜尋按鈕點擊事件
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = keywordField.getText().trim();
        LocalDate selectedDate = eventDatePicker.getValue();

        // 這裡應該實作搜尋邏輯
        // 暫時顯示搜尋條件
        String searchInfo = "搜尋條件 - ";
        if (selectedDate != null) {
            searchInfo += "日期: " + selectedDate + " ";
        }
        if (!keyword.isEmpty()) {
            searchInfo += "關鍵字: " + keyword;
        }

        messageLabel.setText(searchInfo);

        // TODO: 實作實際的搜尋功能
        filterRegistrationData(keyword, selectedDate);
    }

    /**
     * 處理顯示全部按鈕點擊事件
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        loadRegistrationData();
        messageLabel.setText("顯示所有報名紀錄");
    }

    /**
     * 處理重新整理按鈕點擊事件
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadRegistrationData();
        messageLabel.setText("資料已重新整理");
    }

    /**
     * 處理清除搜尋按鈕點擊事件
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        keywordField.clear();
        eventDatePicker.setValue(null);
        messageLabel.setText("搜尋條件已清除");
    }

    /**
     * 處理取消報名按鈕點擊事件
     */
    @FXML
    private void handleCancelSelected(ActionEvent event) {
        RegistrationRecord selected = registrationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 確認對話框
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("確認取消報名");
            confirmAlert.setHeaderText("取消報名確認");
            confirmAlert.setContentText("您確定要取消報名活動「" + selected.getEventName() + "」嗎？");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // 更新狀態為已取消
                    selected.setStatus("已取消");
                    registrationTable.refresh();
                    showInfoAlert("取消報名", "取消成功", "已成功取消報名活動: " + selected.getEventName());
                    messageLabel.setText("已取消報名: " + selected.getEventName());
                }
            });
        } else {
            showErrorAlert("錯誤", "未選擇活動", "請先選擇要取消報名的活動");
        }
    }

    /**
     * 處理返回按鈕點擊事件
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("學生主畫面.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("學生主畫面");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法返回主畫面", e.getMessage());
        }
    }

    /**
     * 載入報名資料（示例資料）
     */
    private void loadRegistrationData() {
        registrationData.clear();

        // 添加示例資料
        registrationData.add(new RegistrationRecord(
                "程式設計競賽", "2024-03-15", "09:00",
                "電腦教室A", "資訊系", "2024-03-01", "已報名"
        ));
        registrationData.add(new RegistrationRecord(
                "校園音樂會", "2024-03-20", "19:00",
                "演藝廳", "學務處", "2024-03-05", "已報名"
        ));
        registrationData.add(new RegistrationRecord(
                "職涯講座", "2024-03-25", "14:00",
                "國際會議廳", "職涯中心", "2024-03-10", "已報名"
        ));

        updateTotalLabel();
    }

    /**
     * 過濾報名資料
     */
    private void filterRegistrationData(String keyword, LocalDate date) {
        // TODO: 實作過濾邏輯
        // 這裡應該根據關鍵字和日期過濾資料
        updateTotalLabel();
    }

    /**
     * 更新總計標籤
     */
    private void updateTotalLabel() {
        totalLabel.setText("總計活動: " + registrationData.size() + " 項");
    }

    /**
     * 顯示錯誤警告對話框
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 顯示資訊對話框
     */
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 報名紀錄資料類
     */
    public static class RegistrationRecord {
        private String eventName;
        private String eventDate;
        private String eventTime;
        private String location;
        private String organizer;
        private String registrationDate;
        private String status;

        public RegistrationRecord(String eventName, String eventDate, String eventTime,
                                  String location, String organizer, String registrationDate, String status) {
            this.eventName = eventName;
            this.eventDate = eventDate;
            this.eventTime = eventTime;
            this.location = location;
            this.organizer = organizer;
            this.registrationDate = registrationDate;
            this.status = status;
        }

        // Getter 方法
        public String getEventName() { return eventName; }
        public String getEventDate() { return eventDate; }
        public String getEventTime() { return eventTime; }
        public String getLocation() { return location; }
        public String getOrganizer() { return organizer; }
        public String getRegistrationDate() { return registrationDate; }
        public String getStatus() { return status; }

        // Setter 方法
        public void setEventName(String eventName) { this.eventName = eventName; }
        public void setEventDate(String eventDate) { this.eventDate = eventDate; }
        public void setEventTime(String eventTime) { this.eventTime = eventTime; }
        public void setLocation(String location) { this.location = location; }
        public void setOrganizer(String organizer) { this.organizer = organizer; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
        public void setStatus(String status) { this.status = status; }
    }
}