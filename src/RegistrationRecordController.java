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
import model.Event;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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
    private ObservableList<RegistrationRecord> allRegistrationData; // 存儲所有資料用於搜尋
    private String currentStudentId; // 當前學生ID

    // CSV檔案路徑
    private final String[] registrationCsvPaths = {
            "已報名.csv",
            "src/已報名.csv",
            "src/main/resources/已報名.csv",
            "./已報名.csv",
            "../已報名.csv"
    };

    private final String[] eventCsvPaths = {
            "活動列表.csv",
            "src/活動列表.csv",
            "src/main/resources/活動列表.csv",
            "./活動列表.csv",
            "../活動列表.csv"
    };

    // 用於存儲活動資料的Map
    private Map<String, Event> eventMap = new HashMap<>();

    /**
     * 設定當前學生ID
     */
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        System.out.println("設定當前學生ID: " + studentId);
        loadRegistrationData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格欄位
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        eventDateColumn.setCellValueFactory(new PropertyValueFactory<>("eventDate"));
        eventTimeColumn.setCellValueFactory(new PropertyValueFactory<>("eventTime"));
        organizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizer"));
        registrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 初始化資料
        registrationData = FXCollections.observableArrayList();
        allRegistrationData = FXCollections.observableArrayList();
        registrationTable.setItems(registrationData);

        System.out.println("查詢報名紀錄頁面已載入");
    }

    /**
     * 載入活動資料到Map中
     */
    private void loadEventData() {
        eventMap.clear();
        String usedPath = null;

        // 嘗試多個可能的活動列表CSV檔案路徑
        for (String csvPath : eventCsvPaths) {
            File file = new File(csvPath);
            if (file.exists() && file.canRead()) {
                usedPath = csvPath;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                    String line;
                    boolean firstLine = true;
                    int lineNumber = 0;

                    while ((line = br.readLine()) != null) {
                        lineNumber++;

                        if (firstLine) {
                            firstLine = false;
                            System.out.println("活動列表標題行：" + line);
                            continue;
                        }

                        String[] tokens = line.split(",", -1);

                        if (tokens.length >= 6) {
                            try {
                                String id = tokens[0].trim();
                                String title = tokens[1].trim();
                                String location = tokens[2].trim();
                                String time = tokens[3].trim();
                                String organizerInfo = tokens[4].trim();
                                int capacity = Integer.parseInt(tokens[5].trim());

                                // 創建簡化的Event物件用於顯示
                                Event event = new Event(id, title, location, time, capacity, null);
                                eventMap.put(id, event);

                                System.out.println("載入活動：" + id + " - " + title);

                            } catch (NumberFormatException e) {
                                System.out.println("解析活動資料時發生錯誤 (第" + lineNumber + "行)：" + e.getMessage());
                            }
                        } else {
                            System.out.println("活動資料格式不正確 (第" + lineNumber + "行，欄位數量: " + tokens.length + ")：" + line);
                        }
                    }

                    System.out.println("成功從 " + csvPath + " 載入 " + eventMap.size() + " 個活動");
                    break;

                } catch (IOException e) {
                    System.err.println("讀取活動列表 " + csvPath + " 時發生錯誤：" + e.getMessage());
                }
            }
        }

        if (usedPath == null) {
            System.out.println("找不到活動列表CSV檔案");
        }
    }

    /**
     * 載入報名資料
     */
    private void loadRegistrationData() {
        if (currentStudentId == null || currentStudentId.trim().isEmpty()) {
            System.out.println("警告：當前學生ID為空，無法載入報名資料");
            return;
        }

        // 先載入活動資料
        loadEventData();

        registrationData.clear();
        allRegistrationData.clear();
        String usedPath = null;

        // 嘗試多個可能的報名CSV檔案路徑
        for (String csvPath : registrationCsvPaths) {
            File file = new File(csvPath);
            if (file.exists() && file.canRead()) {
                usedPath = csvPath;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                    String line;
                    boolean firstLine = true;
                    int lineNumber = 0;
                    int loadedCount = 0;

                    System.out.println("開始載入學生 " + currentStudentId + " 的報名資料");

                    while ((line = br.readLine()) != null) {
                        lineNumber++;

                        if (firstLine) {
                            firstLine = false;
                            System.out.println("報名紀錄標題行：" + line);
                            continue;
                        }

                        String[] tokens = line.split(",", -1);

                        if (tokens.length >= 3) {
                            String studentId = tokens[0].trim();
                            String eventId = tokens[1].trim();
                            String registrationTime = tokens[2].trim();

                            System.out.println("處理報名紀錄：學生ID=" + studentId + ", 活動ID=" + eventId + ", 報名時間=" + registrationTime);

                            // 只載入當前學生的報名資料
                            if (studentId.equals(currentStudentId)) {
                                Event event = eventMap.get(eventId);

                                if (event != null) {
                                    // 解析和格式化時間
                                    String formattedRegistrationTime = formatRegistrationTime(registrationTime);
                                    String eventDate = extractEventDate(event.getTime());
                                    String eventTime = extractEventTime(event.getTime());

                                    RegistrationRecord record = new RegistrationRecord(
                                            event.getTitle(),           // 活動名稱
                                            eventDate,                  // 活動日期
                                            eventTime,                  // 活動時間
                                            event.getLocation(),        // 地點
                                            "主辦單位",                   // 主辦單位（暫時固定）
                                            formattedRegistrationTime,  // 報名時間
                                            "已報名"                     // 狀態
                                    );

                                    registrationData.add(record);
                                    allRegistrationData.add(record);
                                    loadedCount++;

                                    System.out.println("✓ 成功載入報名：" + event.getTitle());
                                } else {
                                    System.out.println("✗ 找不到活動ID: " + eventId);
                                }
                            }
                        } else {
                            System.out.println("報名資料格式不正確 (第" + lineNumber + "行，欄位數量: " + tokens.length + ")：" + line);
                        }
                    }

                    System.out.println("成功從 " + csvPath + " 載入學生 " + currentStudentId + " 的 " + loadedCount + " 筆報名資料");
                    break;

                } catch (IOException e) {
                    System.err.println("讀取報名紀錄 " + csvPath + " 時發生錯誤：" + e.getMessage());
                }
            }
        }

        if (usedPath == null) {
            System.out.println("找不到報名紀錄CSV檔案");
        }

        updateTotalLabel();
        messageLabel.setText("已載入 " + registrationData.size() + " 筆報名資料");
    }

    /**
     * 格式化報名時間
     */
    private String formatRegistrationTime(String registrationTime) {
        try {
            // 嘗試解析時間格式：yyyy-MM-dd HH:mm:ss
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            LocalDateTime dateTime = LocalDateTime.parse(registrationTime, inputFormatter);
            return dateTime.format(outputFormatter);
        } catch (DateTimeParseException e) {
            // 如果解析失敗，返回原始字串
            System.out.println("無法解析報名時間格式：" + registrationTime);
            return registrationTime;
        }
    }

    /**
     * 從活動時間字串中提取日期
     */
    private String extractEventDate(String eventTime) {
        try {
            if (eventTime.contains(" ")) {
                return eventTime.split(" ")[0]; // 取空格前的部分作為日期
            }
            return eventTime;
        } catch (Exception e) {
            return eventTime;
        }
    }

    /**
     * 從活動時間字串中提取時間
     */
    private String extractEventTime(String eventTime) {
        try {
            if (eventTime.contains(" ")) {
                String[] parts = eventTime.split(" ");
                if (parts.length > 1) {
                    return parts[1]; // 取空格後的部分作為時間
                }
            }
            return "未指定時間";
        } catch (Exception e) {
            return "未指定時間";
        }
    }

    /**
     * 處理搜尋按鈕點擊事件
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = keywordField.getText().trim();
        LocalDate selectedDate = eventDatePicker.getValue();

        filterRegistrationData(keyword, selectedDate);

        // 顯示搜尋條件
        String searchInfo = "搜尋條件 - ";
        if (selectedDate != null) {
            searchInfo += "日期: " + selectedDate + " ";
        }
        if (!keyword.isEmpty()) {
            searchInfo += "關鍵字: " + keyword;
        }
        messageLabel.setText(searchInfo + " (找到 " + registrationData.size() + " 筆資料)");
    }

    /**
     * 處理顯示全部按鈕點擊事件
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        registrationData.setAll(allRegistrationData);
        updateTotalLabel();
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
        registrationData.setAll(allRegistrationData);
        updateTotalLabel();
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

                    // TODO: 在這裡應該同時更新CSV檔案，移除該筆報名紀錄

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
     * 過濾報名資料
     */
    private void filterRegistrationData(String keyword, LocalDate date) {
        registrationData.clear();

        for (RegistrationRecord record : allRegistrationData) {
            boolean matchKeyword = keyword.isEmpty() ||
                    record.getEventName().toLowerCase().contains(keyword.toLowerCase()) ||
                    record.getLocation().toLowerCase().contains(keyword.toLowerCase()) ||
                    record.getOrganizer().toLowerCase().contains(keyword.toLowerCase());

            boolean matchDate = date == null;
            if (date != null) {
                try {
                    // 嘗試匹配活動日期
                    String eventDate = record.getEventDate();
                    if (eventDate.startsWith(date.toString())) {
                        matchDate = true;
                    }
                } catch (Exception e) {
                    // 日期比較失敗時，跳過這個條件
                }
            }

            if (matchKeyword && (date == null || matchDate)) {
                registrationData.add(record);
            }
        }

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