import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Student;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EventBrowserController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> eventIdColumn;      // 新增活動編號欄
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> timeColumn;
    @FXML private TableColumn<Event, String> organizerColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TableColumn<Event, Void> actionColumn;
    @FXML private Button backButton;
    private String currentStudentId;
    private Student currentStudent;
    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEventList = FXCollections.observableArrayList();

    // 新增：用於存儲用戶ID到姓名的映射
    private Map<String, String> userIdToNameMap = new HashMap<>();

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        this.currentStudentId = student.getId();
        System.out.println("當前登入學生: " + student.getName() + " (ID: " + student.getId() + ")");
        if (eventTable != null) {
            reloadEvents();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUserData(); // 新增：載入用戶資料
        setupTableColumns();
        reloadEvents();
        eventTable.setItems(filteredEventList);
    }

    // 新增：載入用戶資料的方法
    private void loadUserData() {
        userIdToNameMap.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/users.csv"));
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String userId = parts[0].trim();
                    String userName = parts[1].trim();
                    userIdToNameMap.put(userId, userName);
                }
            }
            reader.close();
            System.out.println("成功載入 " + userIdToNameMap.size() + " 個用戶資料");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("讀取用戶資料時發生錯誤: " + e.getMessage());
        }
    }

    // 新增：根據用戶ID獲取姓名的方法
    private String getOrganizerName(String organizerId) {
        return userIdToNameMap.getOrDefault(organizerId, organizerId); // 如果找不到對應姓名，返回原ID
    }

    private void setupTableColumns() {
        // 注意：FXML 裡要有對應 eventIdColumn，否則要自己加上
        eventIdColumn.setCellValueFactory(new PropertyValueFactory<>("eventId"));
        eventNameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        organizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizer"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("remainingCapacity"));

        // 報名按鈕欄位
        actionColumn.setCellFactory(new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
            @Override
            public TableCell<Event, Void> call(TableColumn<Event, Void> param) {
                return new TableCell<Event, Void>() {
                    private final Button registerButton = new Button();

                    {
                        registerButton.setOnAction(event -> {
                            Event selectedEvent = getTableView().getItems().get(getIndex());
                            handleRegistration(selectedEvent);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Event event = getTableView().getItems().get(getIndex());
                            boolean alreadyRegistered = (currentStudentId != null) &&
                                    isAlreadyRegistered(currentStudentId, event.getEventId());
                            if (alreadyRegistered) {
                                registerButton.setText("已報名");
                                registerButton.setDisable(true);
                                registerButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                            } else if (event.getRemainingCapacity() <= 0) {
                                registerButton.setText("已額滿");
                                registerButton.setDisable(true);
                                registerButton.setStyle("-fx-background-color: #cccccc;");
                            } else {
                                registerButton.setText("報名");
                                registerButton.setDisable(false);
                                registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                            }
                            setGraphic(registerButton);
                        }
                    }

                };
            }
        });
    }


    public void reloadEvents() {
        loadEventsFromCSV();
        filteredEventList.setAll(eventList);
        eventTable.refresh();
    }

    private void loadSampleData() {
        loadEventsFromCSV();
        filteredEventList.setAll(eventList);
    }

    private void loadEventsFromCSV() {
        eventList.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/活動列表.csv"));
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 6) {
                    String eventId = parts[0].trim();
                    String eventName = parts[1].trim();
                    String location = parts[2].trim();
                    String time = parts[3].trim();
                    String organizerId = parts[4].trim(); // 這是主辦單位ID
                    String organizerName = getOrganizerName(organizerId); // 轉換為姓名
                    int remainingCapacity = 0;
                    try {
                        remainingCapacity = Integer.parseInt(parts[5].trim());
                    } catch (NumberFormatException e) {
                        remainingCapacity = 0;
                    }
                    // 使用主辦人姓名而不是ID創建Event物件
                    Event event = new Event(eventId, eventName, location, time, organizerName, remainingCapacity);
                    eventList.add(event);
                }
            }
            reader.close();
            System.out.println("成功載入 " + eventList.size() + " 個活動");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("讀取 CSV 檔案時發生錯誤: " + e.getMessage());
        }
    }


    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        if (searchText.isEmpty()) {
            filteredEventList.setAll(eventList);
        } else {
            filteredEventList.clear();
            for (Event event : eventList) {
                if (event.getEventName().toLowerCase().contains(searchText) ||
                        event.getLocation().toLowerCase().contains(searchText) ||
                        event.getOrganizer().toLowerCase().contains(searchText) ||
                        event.getEventId().toLowerCase().contains(searchText)) {
                    filteredEventList.add(event);
                }
            }
        }
    }

    @FXML
    private void showAllEvents() {
        searchField.clear();
        filteredEventList.setAll(eventList);
    }

    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/學生主畫面.fxml"));
            Parent root = loader.load();
            StudentMainController controller = loader.getController();
            if (currentStudent != null) {
                controller.setStudent(currentStudent);
            }
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("學生系統");
            stage.show();
        } catch (IOException e) {
            System.err.println("無法載入學生主畫面：" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRegistration(Event event) {
        if (currentStudentId == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("錯誤");
            alert.setHeaderText(null);
            alert.setContentText("無法取得學生資訊，請重新登入！");
            alert.showAndWait();
            return;
        }
        if (isAlreadyRegistered(currentStudentId, event.getEventId())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("已報名");
            alert.setHeaderText(null);
            alert.setContentText("您已經報名過「" + event.getEventName() + "」活動！");
            alert.showAndWait();
            return;
        }
        if (event.getRemainingCapacity() > 0) {
            event.setRemainingCapacity(event.getRemainingCapacity() - 1);
            // <<== 新增呼叫，寫回活動列表.csv
            updateEventCapacityInCSV(event.getEventId(), event.getRemainingCapacity());
            eventTable.refresh();
            saveRegistrationToCSV(currentStudentId, event.getEventId());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("報名成功");
            alert.setHeaderText(null);
            alert.setContentText("恭喜！您已成功報名「" + event.getEventName() + "」活動。\n" +
                    "剩餘名額：" + event.getRemainingCapacity());
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("報名失敗");
            alert.setHeaderText(null);
            alert.setContentText("很抱歉，「" + event.getEventName() + "」活動名額已滿！");
            alert.showAndWait();
        }
    }

    private void saveRegistrationToCSV(String studentId, String eventId) {
        try {
            String csvPath = "src/已報名.csv";
            File csvFile = new File(csvPath);
            boolean fileExists = csvFile.exists();
            FileWriter writer = new FileWriter(csvFile, true);
            PrintWriter printWriter = new PrintWriter(writer);
            if (!fileExists) {
                printWriter.println("學生ID,活動編號,報名時間");
            }
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = now.format(formatter);
            printWriter.printf("%s,%s,%s%n", studentId, eventId, currentTime);
            printWriter.close();
            writer.close();
            System.out.println("報名資訊已寫入已報名.csv檔案");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("錯誤");
            alert.setHeaderText(null);
            alert.setContentText("無法寫入報名資料: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean isAlreadyRegistered(String studentId, String eventId) {
        try {
            File csvFile = new File("src/已報名.csv");
            if (!csvFile.exists()) {
                return false;
            }
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    String registeredStudentId = parts[0].trim();
                    String registeredEventId = parts[1].trim();
                    if (registeredStudentId.equals(studentId) && registeredEventId.equals(eventId)) {
                        reader.close();
                        return true;
                    }
                }
            }
            reader.close();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateEventCapacityInCSV(String eventId, int newCapacity) {
        String csvPath = "src/活動列表.csv";
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) {
            System.out.println("找不到活動列表.csv，無法更新名額！");
            return;
        }
        try {
            // 1. 先讀出全部資料
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            StringBuilder sb = new StringBuilder();
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    sb.append(line).append("\n");
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 6 && parts[0].trim().equals(eventId)) {
                    // 找到目標活動，把剩餘名額改為新值
                    parts[5] = String.valueOf(newCapacity);
                    line = String.join(",", parts);
                }
                sb.append(line).append("\n");
            }
            reader.close();

            // 2. 寫回所有資料（覆蓋原檔）
            FileWriter writer = new FileWriter(csvFile, false);
            writer.write(sb.toString());
            writer.close();
            System.out.println("活動 " + eventId + " 名額已同步更新至 " + newCapacity);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("更新活動名額失敗：" + e.getMessage());
        }
    }




    // 靜態內部類
    public static class Event {
        private final SimpleStringProperty eventId; // 新增活動編號
        private final SimpleStringProperty eventName;
        private final SimpleStringProperty location;
        private final SimpleStringProperty time;
        private final SimpleStringProperty organizer;
        private final SimpleIntegerProperty remainingCapacity;
        private final SimpleBooleanProperty registered = new SimpleBooleanProperty(false);

        public Event(String eventId, String eventName, String location, String time, String organizer, int remainingCapacity) {
            this.eventId = new SimpleStringProperty(eventId);
            this.eventName = new SimpleStringProperty(eventName);
            this.location = new SimpleStringProperty(location);
            this.time = new SimpleStringProperty(time);
            this.organizer = new SimpleStringProperty(organizer);
            this.remainingCapacity = new SimpleIntegerProperty(remainingCapacity);
        }

        public String getEventId() { return eventId.get(); }
        public String getEventName() { return eventName.get(); }
        public String getLocation() { return location.get(); }
        public String getTime() { return time.get(); }
        public String getOrganizer() { return organizer.get(); }
        public int getRemainingCapacity() { return remainingCapacity.get(); }
        public boolean isRegistered() { return registered.get(); }
        public void setRemainingCapacity(int remainingCapacity) { this.remainingCapacity.set(remainingCapacity); }
        public void setRegistered(boolean value) { this.registered.set(value); }
    }
}