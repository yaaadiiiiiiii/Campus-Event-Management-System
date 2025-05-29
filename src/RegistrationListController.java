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
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
    @FXML private TableColumn<Registration, String> registrationTimeColumn;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private ObservableList<Registration> allRegistrations = FXCollections.observableArrayList();
    private ObservableList<Registration> filteredRegistrations = FXCollections.observableArrayList();
    private ObservableList<String> eventList = FXCollections.observableArrayList();

    // 儲存活動資訊的 Map
    private Map<String, EventInfo> eventInfoMap = new HashMap<>();

    // 儲存學生資訊的 Map（如果有學生資料的話）
    private Map<String, String> studentNameMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadUserData();
        loadEventData();
        loadRegistrationData();
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
        registrationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("registrationTime"));
    }

    private void loadUserData() {
        try {
            // 讀取 users.csv
            InputStream inputStream = getClass().getResourceAsStream("/users.csv");
            if (inputStream == null) {
                System.err.println("找不到users.csv檔案");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String userId = parts[0].trim();
                    String userName = parts[1].trim();
                    String userType = parts[3].trim();

                    // 只儲存學生資料 (userType = "s")
                    if ("s".equals(userType)) {
                        studentNameMap.put(userId, userName);
                    }
                }
            }
            reader.close();

            System.out.println("載入了 " + studentNameMap.size() + " 位學生資料");

        } catch (IOException e) {
            System.err.println("讀取使用者資料時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEventData() {
        try {
            // 讀取活動列表.csv
            InputStream inputStream = getClass().getResourceAsStream("/活動列表.csv");
            if (inputStream == null) {
                System.err.println("找不到活動列表.csv檔案");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }

                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String eventId = parts[0].trim();
                    String title = parts[1].trim();
                    String location = parts[2].trim();
                    String time = parts[3].trim();
                    String organizer = parts[4].trim();
                    int capacity = Integer.parseInt(parts[5].trim());

                    EventInfo eventInfo = new EventInfo(eventId, title, location, time, organizer, capacity);
                    eventInfoMap.put(eventId, eventInfo);
                    eventList.add(title);
                }
            }
            reader.close();

        } catch (IOException | NumberFormatException e) {
            System.err.println("讀取活動列表時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRegistrationData() {
        try {
            // 讀取已報名.csv
            InputStream inputStream = getClass().getResourceAsStream("/已報名.csv");
            if (inputStream == null) {
                System.err.println("找不到已報名.csv檔案");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String studentId = parts[0].trim();
                    String eventId = parts[1].trim();
                    String registrationTime = parts[2].trim();

                    // 從活動資訊中獲取活動名稱
                    EventInfo eventInfo = eventInfoMap.get(eventId);
                    String eventName = eventInfo != null ? eventInfo.getTitle() : "未知活動";

                    // 獲取學生姓名（如果沒有學生資料，使用學號）
                    String studentName = getStudentName(studentId);

                    Registration registration = new Registration(
                            studentId,
                            studentName,
                            eventName,
                            registrationTime
                    );

                    allRegistrations.add(registration);
                }
            }
            reader.close();

        } catch (IOException e) {
            System.err.println("讀取報名資料時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getStudentName(String studentId) {
        // 從 studentNameMap 中獲取學生姓名，如果找不到則返回學號
        return studentNameMap.getOrDefault(studentId, studentId);
    }

    private void setupEventComboBox() {
        eventComboBox.setItems(eventList);
        // 添加"全部活動"選項
        eventComboBox.getItems().add(0, "全部活動");
        eventComboBox.setValue("全部活動");
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
            // 從活動資訊中獲取總名額
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
        // 從活動資訊中獲取名額
        for (EventInfo eventInfo : eventInfoMap.values()) {
            if (eventInfo.getTitle().equals(eventName)) {
                return eventInfo.getCapacity();
            }
        }
        return 0;
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
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            // 寫入 BOM 以支援中文
            writer.write('\uFEFF');

            // 寫入標題行
            writer.write("序號,學號,姓名,活動名稱,報名時間\n");

            // 寫入資料
            int sequence = 1;
            for (Registration registration : filteredRegistrations) {
                writer.write(String.format("%d,%s,%s,%s,%s\n",
                        sequence++,
                        registration.getStudentId(),
                        registration.getStudentName(),
                        registration.getEventName(),
                        registration.getRegistrationTime()
                ));
            }
        }
    }

    @FXML
    private void handleRefresh() {
        // 重新載入資料
        allRegistrations.clear();
        eventList.clear();
        eventInfoMap.clear();
        studentNameMap.clear();

        loadUserData();
        loadEventData();
        loadRegistrationData();
        setupEventComboBox();

        filteredRegistrations.setAll(allRegistrations);
        registrationTable.setItems(filteredRegistrations);
        updateStatistics();

        showInfoAlert("重新整理", "資料已更新");
    }

//    @FXML
//    private void handleDeleteRegistration() {
//        Registration selected = registrationTable.getSelectionModel().getSelectedItem();
//        if (selected == null) {
//            showWarningAlert("請選擇要取消的報名記錄");
//            return;
//        }
//
//        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
//        confirmAlert.setTitle("確認取消報名");
//        confirmAlert.setHeaderText("確定要取消以下報名記錄嗎？");
//        confirmAlert.setContentText("學生：" + selected.getStudentName() +
//                "\n活動：" + selected.getEventName());
//
//        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
//            allRegistrations.remove(selected);
//            filteredRegistrations.remove(selected);
//            updateStatistics();
//            showInfoAlert("取消成功", "已成功取消該學生的報名記錄");
//        }
//    }

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

    // EventInfo 類別
    private static class EventInfo {
        private final String id;
        private final String title;
        private final String location;
        private final String time;
        private final String organizer;
        private final int capacity;

        public EventInfo(String id, String title, String location, String time, String organizer, int capacity) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.time = time;
            this.organizer = organizer;
            this.capacity = capacity;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getLocation() { return location; }
        public String getTime() { return time; }
        public String getOrganizer() { return organizer; }
        public int getCapacity() { return capacity; }
    }

    // Registration 資料類別
    public static class Registration {
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty studentName;
        private final SimpleStringProperty eventName;
        private final SimpleStringProperty registrationTime;

        public Registration(String studentId, String studentName, String eventName, String registrationTime) {
            this.studentId = new SimpleStringProperty(studentId);
            this.studentName = new SimpleStringProperty(studentName);
            this.eventName = new SimpleStringProperty(eventName);
            this.registrationTime = new SimpleStringProperty(registrationTime);
        }

        // Getter methods
        public String getStudentId() { return studentId.get(); }
        public String getStudentName() { return studentName.get(); }
        public String getEventName() { return eventName.get(); }
        public String getRegistrationTime() { return registrationTime.get(); }

        // Property methods for TableView binding
        public SimpleStringProperty studentIdProperty() { return studentId; }
        public SimpleStringProperty studentNameProperty() { return studentName; }
        public SimpleStringProperty eventNameProperty() { return eventName; }
        public SimpleStringProperty registrationTimeProperty() { return registrationTime; }
    }
}