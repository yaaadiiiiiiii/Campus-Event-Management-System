import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class RegistrationListController implements Initializable {

    @FXML private ComboBox<String> eventComboBox;
    @FXML private TextField studentSearchField;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Label totalRegistrationsLabel;
    @FXML private Label remainingCapacityLabel;
    @FXML private Label eventStatusLabel;
    @FXML private TableView<Registration> registrationTable;
    @FXML private TableColumn<Registration, Integer> sequenceColumn;
    @FXML private TableColumn<Registration, String> studentIdColumn;
    @FXML private TableColumn<Registration, String> studentNameColumn;
    @FXML private TableColumn<Registration, String> departmentColumn;
    @FXML private TableColumn<Registration, String> emailColumn;
    @FXML private TableColumn<Registration, String> phoneColumn;
    @FXML private TableColumn<Registration, String> registrationTimeColumn;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private ObservableList<Registration> allRegistrations = FXCollections.observableArrayList();
    private ObservableList<Registration> filteredRegistrations = FXCollections.observableArrayList();
    private ObservableList<String> eventList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadEventList();
        loadSampleData();
        setupEventComboBox();

        // 預設顯示所有報名記錄
        filteredRegistrations.setAll(allRegistrations);
        registrationTable.setItems(filteredRegistrations);
        updateStatistics();
    }

    private void setupTableColumns() {
        sequenceColumn.setCellValueFactory(cellData -> {
            int index = registrationTable.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(index).asObject();
        });

        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        registrationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("registrationTime"));
    }

    private void loadEventList() {
        // 載入活動列表（實際應該從資料庫讀取）
        eventList.addAll(
                "程式設計工作坊",
                "創業講座",
                "攝影比賽",
                "英語角",
                "音樂會",
                "籃球比賽",
                "文學講座"
        );
    }

    private void setupEventComboBox() {
        eventComboBox.setItems(eventList);
        // 添加"全部活動"選項
        eventComboBox.getItems().add(0, "全部活動");
        eventComboBox.setValue("全部活動");
    }

    private void loadSampleData() {
        // 載入範例報名資料（實際應該從資料庫讀取）
        allRegistrations.addAll(
                new Registration("A001", "王小明", "資訊工程系", "wang@email.com", "0912345678", "程式設計工作坊", "2024-06-10 10:30"),
                new Registration("A002", "李美華", "企業管理系", "li@email.com", "0923456789", "創業講座", "2024-06-11 14:20"),
                new Registration("A003", "張志豪", "資訊工程系", "zhang@email.com", "0934567890", "程式設計工作坊", "2024-06-10 11:45"),
                new Registration("A004", "陳雅婷", "外國語文系", "chen@email.com", "0945678901", "英語角", "2024-06-12 09:15"),
                new Registration("A005", "林建宏", "音樂系", "lin@email.com", "0956789012", "音樂會", "2024-06-13 16:30"),
                new Registration("A006", "黃淑芬", "中國文學系", "huang@email.com", "0967890123", "文學講座", "2024-06-14 13:45"),
                new Registration("A007", "吳志明", "資訊工程系", "wu@email.com", "0978901234", "程式設計工作坊", "2024-06-10 15:20"),
                new Registration("A008", "劉雅琪", "企業管理系", "liu@email.com", "0989012345", "創業講座", "2024-06-11 16:10")
        );
    }

    @FXML
    private void handleEventSelection() {
        String selectedEvent = eventComboBox.getValue();
        if (selectedEvent == null) return;

        filterRegistrations();
        updateStatistics();
    }

    @FXML
    private void handleSearch() {
        filterRegistrations();
    }

    @FXML
    private void handleClear() {
        studentSearchField.clear();
        eventComboBox.setValue("全部活動");
        filteredRegistrations.setAll(allRegistrations);
        registrationTable.setItems(filteredRegistrations);
        updateStatistics();
    }

    private void filterRegistrations() {
        String selectedEvent = eventComboBox.getValue();
        String searchText = studentSearchField.getText().toLowerCase().trim();

        filteredRegistrations.clear();

        for (Registration registration : allRegistrations) {
            boolean matchEvent = selectedEvent.equals("全部活動") ||
                    registration.getEventName().equals(selectedEvent);

            boolean matchSearch = searchText.isEmpty() ||
                    registration.getStudentId().toLowerCase().contains(searchText) ||
                    registration.getStudentName().toLowerCase().contains(searchText);

            if (matchEvent && matchSearch) {
                filteredRegistrations.add(registration);
            }
        }

        registrationTable.setItems(filteredRegistrations);
        updateStatistics();
    }

    private void updateStatistics() {
        String selectedEvent = eventComboBox.getValue();
        int totalRegistrations = filteredRegistrations.size();

        totalRegistrationsLabel.setText("總報名人數：" + totalRegistrations);

        if (selectedEvent != null && !selectedEvent.equals("全部活動")) {
            // 這裡應該從資料庫獲取活動的總名額
            int totalCapacity = getTotalCapacityForEvent(selectedEvent);
            int remaining = Math.max(0, totalCapacity - totalRegistrations);
            remainingCapacityLabel.setText("剩餘名額：" + remaining);
            eventStatusLabel.setText("活動狀態：" + (remaining > 0 ? "開放報名" : "已額滿"));
        } else {
            remainingCapacityLabel.setText("剩餘名額：-");
            eventStatusLabel.setText("活動狀態：全部活動");
        }
    }

    private int getTotalCapacityForEvent(String eventName) {
        // 實際應該從資料庫獲取，這裡提供範例數據
        switch (eventName) {
            case "程式設計工作坊": return 20;
            case "創業講座": return 50;
            case "攝影比賽": return 30;
            case "英語角": return 15;
            case "音樂會": return 100;
            case "籃球比賽": return 16;
            case "文學講座": return 25;
            default: return 0;
        }
    }

    @FXML
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("匯出報名名單");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            Stage stage = (Stage) exportButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                exportToCSV(file);
                showInfoAlert("匯出成功", "報名名單已成功匯出至：" + file.getAbsolutePath());
            }

        } catch (Exception e) {
            showErrorAlert("匯出失敗", "匯出過程中發生錯誤：" + e.getMessage());
        }
    }

    private void exportToCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            // 寫入 BOM 以支援中文
            writer.write('\uFEFF');

            // 寫入標題行
            writer.write("序號,學號,姓名,系所,電子郵件,聯絡電話,活動名稱,報名時間\n");

            // 寫入資料
            int sequence = 1;
            for (Registration registration : filteredRegistrations) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        sequence++,
                        registration.getStudentId(),
                        registration.getStudentName(),
                        registration.getDepartment(),
                        registration.getEmail(),
                        registration.getPhone(),
                        registration.getEventName(),
                        registration.getRegistrationTime()
                ));
            }
        }
    }

    @FXML
    private void handleRefresh() {
        // 重新載入資料（實際應該從資料庫重新讀取）
        loadSampleData();
        filterRegistrations();
        showInfoAlert("重新整理", "資料已更新");
    }

    @FXML
    private void handleDeleteRegistration() {
        Registration selected = registrationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("請選擇要取消的報名記錄");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("確認取消報名");
        confirmAlert.setHeaderText("確定要取消以下報名記錄嗎？");
        confirmAlert.setContentText("學生：" + selected.getStudentName() +
                "\n活動：" + selected.getEventName());

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            allRegistrations.remove(selected);
            filteredRegistrations.remove(selected);
            updateStatistics();
            showInfoAlert("取消成功", "已成功取消該學生的報名記錄");
        }
    }

    @FXML
    private void handleBackToMain() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/主辦人主畫面.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();

            // 保持當前視窗尺寸
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            stage.setScene(new Scene(root, currentWidth, currentHeight));
            stage.setTitle("主辦人管理系統");
            stage.show();
        } catch (IOException e) {
            System.err.println("無法載入主辦人主畫面：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("警告");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Registration 資料類別
    public static class Registration {
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty department;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty eventName;
        private final SimpleStringProperty registrationTime;

        public Registration(String studentId, String studentName, String department,
                            String email, String phone, String eventName, String registrationTime) {
            this.studentId = new SimpleStringProperty(studentId);
            this.studentName = new SimpleStringProperty(studentName);
            this.department = new SimpleStringProperty(department);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.eventName = new SimpleStringProperty(eventName);
            this.registrationTime = new SimpleStringProperty(registrationTime);
        }

        // Getter methods
        public String getStudentId() { return studentId.get(); }
        public String getStudentName() { return studentName.get(); }
        public String getDepartment() { return department.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getEventName() { return eventName.get(); }
        public String getRegistrationTime() { return registrationTime.get(); }

        // Property methods for TableView binding
        public SimpleStringProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public SimpleStringProperty departmentProperty() { return department; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
        public SimpleStringProperty eventNameProperty() { return eventName; }
        public SimpleStringProperty registrationTimeProperty() { return registrationTime; }
    }
}