import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.io.*;
import java.net.URL;
import java.util.*;

public class RegistrationRecordController implements Initializable {

    @FXML private TableView<RegistrationRecord> registrationTable;
    @FXML private TableColumn<RegistrationRecord, String> eventTitleColumn;
    @FXML private TableColumn<RegistrationRecord, String> locationColumn;
    @FXML private TableColumn<RegistrationRecord, String> timeColumn;
    @FXML private TableColumn<RegistrationRecord, String> organizerColumn;
    @FXML private TableColumn<RegistrationRecord, String> registrationTimeColumn;
    @FXML private Label totalLabel;

    @FXML private TextField keywordField;     // 新增搜尋欄位 FXML 綁定
    @FXML private Button searchButton;
    @FXML private Button showAllButton;
    @FXML private Button refreshButton;
    @FXML private Button clearSearchButton;
    @FXML private TableColumn<RegistrationRecord, String> statusColumn;

    private ObservableList<RegistrationRecord> registrationData = FXCollections.observableArrayList();
    private ObservableList<RegistrationRecord> allData = FXCollections.observableArrayList(); // 所有資料（for 顯示全部/搜尋）
    private String currentStudentId = null; // 由主畫面呼叫 setCurrentStudentId 設定

    private static final String EVENT_CSV_PATH = "src/活動列表.csv";
    private static final String REG_CSV_PATH = "src/已報名.csv";

    private Map<String, EventData> eventMap = new HashMap<>();

    @Override
    public void initialize(URL location, java.util.ResourceBundle resources) {
        eventTitleColumn.setCellValueFactory(cell -> cell.getValue().eventTitleProperty());
        locationColumn.setCellValueFactory(cell -> cell.getValue().locationProperty());
        timeColumn.setCellValueFactory(cell -> cell.getValue().timeProperty());
        organizerColumn.setCellValueFactory(cell -> cell.getValue().organizerProperty());
        registrationTimeColumn.setCellValueFactory(cell -> cell.getValue().registrationTimeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        registrationTable.setItems(registrationData);
    }

    // 主畫面切換頁面時會呼叫
    public void setCurrentStudentId(String studentId) {
        this.currentStudentId = studentId;
        loadEventMap();
        loadRegistrationData();
        registrationTable.setItems(registrationData);
        updateTotalLabel();
    }

    // 讀取活動列表.csv 到 eventMap
    private void loadEventMap() {
        eventMap.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(EVENT_CSV_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String eventId = parts[0].trim();
                    String title = parts[1].trim();
                    String location = parts[2].trim();
                    String time = parts[3].trim();
                    String organizer = parts[4].trim();
                    eventMap.put(eventId, new EventData(title, location, time, organizer));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 讀取已報名.csv 並組合資料
    private void loadRegistrationData() {
        registrationData.clear();
        allData.clear();
        if (currentStudentId == null) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(REG_CSV_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String studentId = parts[0].trim();
                    String eventId = parts[1].trim();
                    String regTime = parts[2].trim();
                    if (studentId.equals(currentStudentId) && eventMap.containsKey(eventId)) {
                        EventData event = eventMap.get(eventId);
                        RegistrationRecord record = new RegistrationRecord(
                                event.getTitle(), event.getLocation(), event.getTime(), event.getOrganizer(), regTime, "已報名");
                        registrationData.add(record);
                        allData.add(record);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        totalLabel.setText("總計活動: " + registrationData.size() + " 項");
    }

    // 查詢功能（依關鍵字過濾）
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = (keywordField != null) ? keywordField.getText().trim().toLowerCase() : "";
        registrationData.clear();
        if (keyword.isEmpty()) {
            registrationData.addAll(allData);
        } else {
            for (RegistrationRecord record : allData) {
                if (record.getEventTitle().toLowerCase().contains(keyword)
                        || record.getLocation().toLowerCase().contains(keyword)
                        || record.getOrganizer().toLowerCase().contains(keyword)) {
                    registrationData.add(record);
                }
            }
        }
        updateTotalLabel();
    }

    // 顯示全部
    @FXML
    private void handleShowAll(ActionEvent event) {
        registrationData.setAll(allData);
        if (keywordField != null) keywordField.clear();
        updateTotalLabel();
    }

    // 重新整理
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadEventMap();
        loadRegistrationData();
        updateTotalLabel();
        if (keywordField != null) keywordField.clear();
    }

    // 清除搜尋
    @FXML
    private void handleClearSearch(ActionEvent event) {
        if (keywordField != null) keywordField.clear();
        registrationData.setAll(allData);
        updateTotalLabel();
    }

    @FXML
    private void handleCancelSelected(ActionEvent event) {
        RegistrationRecord selected = registrationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "提醒", "請先選擇要取消的活動！");
            return;
        }
        // 彈出確認對話框
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("確認取消");
        alert.setHeaderText("你確定要取消這個報名嗎？");
        alert.setContentText("活動名稱：" + selected.getEventTitle());
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // **1. 從 CSV 移除那一筆**
                boolean removed = removeRecordFromCSV(selected);

                // **2. 從畫面上移除**
                registrationData.remove(selected);
                allData.remove(selected);
                updateTotalLabel();

                if (removed) {
                    showAlert(Alert.AlertType.INFORMATION, "已取消", "已成功取消報名！");
                } else {
                    showAlert(Alert.AlertType.WARNING, "提醒", "取消報名失敗（檔案內無此紀錄）");
                }
            }
        });
    }

    // === 新增這個方法 ===
    private boolean removeRecordFromCSV(RegistrationRecord record) {
        File inputFile = new File(REG_CSV_PATH);
        File tempFile = new File(REG_CSV_PATH + ".tmp");
        boolean removed = false;

        try (
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"))
        ) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    writer.println(line); // 保留標題
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 3) continue;
                // 比對「學生ID + 活動名稱/編號 + 報名時間」是否完全相符
                String studentId = parts[0].trim();
                String eventId = parts[1].trim();
                String regTime = parts[2].trim();
                // eventMap 反查 eventId by eventTitle
                String realEventId = null;
                for (Map.Entry<String, EventData> entry : eventMap.entrySet()) {
                    if (entry.getValue().getTitle().equals(record.getEventTitle())) {
                        realEventId = entry.getKey();
                        break;
                    }
                }
                if (studentId.equals(currentStudentId)
                        && eventId.equals(realEventId)
                        && regTime.equals(record.getRegistrationTime())) {
                    removed = true;
                    continue; // 跳過這一筆（不寫入）
                }
                writer.println(line); // 其餘資料寫回
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        // 覆蓋原檔
        if (removed) {
            if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
                System.err.println("CSV 檔案取代失敗！");
                return false;
            }
        } else {
            tempFile.delete();
        }
        return removed;
    }


    @FXML
    private void handleBack(javafx.event.ActionEvent event) {
        // 返回上一頁邏輯，以下為基本寫法，可依你需求調整
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("學生主畫面.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) registrationTable.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("學生主畫面");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "錯誤", "無法返回主畫面！");
        }
    }

    // 共用訊息提示方法
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 內部 class：活動資料
    private static class EventData {
        private final String title, location, time, organizer;
        public EventData(String title, String location, String time, String organizer) {
            this.title = title;
            this.location = location;
            this.time = time;
            this.organizer = organizer;
        }
        public String getTitle() { return title; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getOrganizer() { return organizer; }
    }

    // 報名紀錄資料
    public static class RegistrationRecord {
        private final javafx.beans.property.SimpleStringProperty eventTitle;
        private final javafx.beans.property.SimpleStringProperty location;
        private final javafx.beans.property.SimpleStringProperty time;
        private final javafx.beans.property.SimpleStringProperty organizer;
        private final javafx.beans.property.SimpleStringProperty registrationTime;
        private final javafx.beans.property.SimpleStringProperty status; // 新增

        public RegistrationRecord(String eventTitle, String location, String time, String organizer, String registrationTime, String status) {
            this.eventTitle = new javafx.beans.property.SimpleStringProperty(eventTitle);
            this.location = new javafx.beans.property.SimpleStringProperty(location);
            this.time = new javafx.beans.property.SimpleStringProperty(time);
            this.organizer = new javafx.beans.property.SimpleStringProperty(organizer);
            this.registrationTime = new javafx.beans.property.SimpleStringProperty(registrationTime);
            this.status = new javafx.beans.property.SimpleStringProperty(status); // 新增
        }
        public javafx.beans.property.StringProperty eventTitleProperty() { return eventTitle; }
        public javafx.beans.property.StringProperty locationProperty() { return location; }
        public javafx.beans.property.StringProperty timeProperty() { return time; }
        public javafx.beans.property.StringProperty organizerProperty() { return organizer; }
        public javafx.beans.property.StringProperty registrationTimeProperty() { return registrationTime; }
        public javafx.beans.property.StringProperty statusProperty() { return status; } // 新增

        public String getEventTitle() { return eventTitle.get(); }
        public String getLocation() { return location.get(); }
        public String getTime() { return time.get(); }
        public String getOrganizer() { return organizer.get(); }
        public String getRegistrationTime() { return registrationTime.get(); }
        public String getStatus() { return status.get(); }
    }
}
