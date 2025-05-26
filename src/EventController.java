import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Event;
import model.Organizer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.HashSet;
import java.util.Set;

public class EventController implements Initializable {

    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> titleColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> timeColumn;
    @FXML private TableColumn<Event, String> organizerColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TableColumn<Event, Void> actionColumn;
    @FXML private Button addButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private Organizer currentOrganizer;

    // 用户ID到名称的映射
    private Map<String, String> userIdToNameMap = new HashMap<>();

    private final String csvPath = "src/活動列表.csv";
    private final String usersPath = "src/users.csv";

    public void setCurrentOrganizer(Organizer organizer) {
        this.currentOrganizer = organizer;
        loadUserIdToNameMapping(); // 加载用户映射
        loadEventsFromCSV();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        eventTable.setItems(eventList);
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        // 显示主办单位名称而不是ID
        organizerColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getOrganizer().getName()));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        setupActionColumn();
    }

    private void setupActionColumn() {
        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactory =
                new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
                    @Override
                    public TableCell<Event, Void> call(final TableColumn<Event, Void> param) {
                        return new TableCell<Event, Void>() {

                            private final Button editBtn = new Button("編輯");
                            private final Button deleteBtn = new Button("刪除");
                            private final HBox hbox = new HBox(editBtn, deleteBtn);

                            {
                                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                                hbox.setSpacing(5);

                                editBtn.setOnAction(event -> {
                                    Event selectedEvent = getTableView().getItems().get(getIndex());
                                    handleEditEvent(selectedEvent);
                                });

                                deleteBtn.setOnAction(event -> {
                                    Event selectedEvent = getTableView().getItems().get(getIndex());
                                    handleDeleteEvent(selectedEvent);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(hbox);
                                }
                            }
                        };
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    /**
     * 加载用户ID到名称的映射
     */
    private void loadUserIdToNameMapping() {
        userIdToNameMap.clear();
        File usersFile = new File(usersPath);

        if (usersFile.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(usersFile), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",", -1);
                    if (tokens.length >= 2) {
                        String userId = tokens[0].trim();
                        String userName = tokens[1].trim();
                        userIdToNameMap.put(userId, userName);
                    }
                }
                System.out.println("加载用户映射完成，共 " + userIdToNameMap.size() + " 个用户");

            } catch (IOException e) {
                System.err.println("无法读取用户文件：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("用户文件不存在：" + usersPath);
        }
    }

    /**
     * 根据用户ID获取用户名称
     */
    private String getUserNameById(String userId) {
        return userIdToNameMap.getOrDefault(userId, userId); // 如果找不到名称，返回ID
    }

    @FXML
    private void handleAddEvent() {
        EventDialog dialog = new EventDialog(null, currentOrganizer);
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(event -> {
            String eventId = generateEventId();
            if (currentOrganizer != null) {
                event.setOrganizer(currentOrganizer);
            }
            Event newEvent = new Event(eventId, event.getTitle(), event.getLocation(),
                    event.getTime(), event.getCapacity(), event.getOrganizer());

            eventList.add(newEvent);
            saveEventsToCSV();
            showAlert("成功", "活動已成功新增！", Alert.AlertType.INFORMATION);
        });
    }

    private void handleEditEvent(Event event) {
        EventDialog dialog = new EventDialog(event);
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(updatedEvent -> {
            event.setTitle(updatedEvent.getTitle());
            event.setLocation(updatedEvent.getLocation());
            event.setTime(updatedEvent.getTime());
            event.setCapacity(updatedEvent.getCapacity());
            event.setOrganizer(currentOrganizer);
            eventTable.refresh();
            saveEventsToCSV();
            showAlert("成功", "活動已成功更新！", Alert.AlertType.INFORMATION);
        });
    }

    private void handleDeleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("確認刪除");
        alert.setHeaderText("您確定要刪除這個活動嗎？");
        alert.setContentText("活動：" + event.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eventList.remove(event);
            saveEventsToCSV();
            showAlert("成功", "活動已成功刪除！", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRefresh() {
        loadUserIdToNameMapping(); // 刷新时重新加载用户映射
        loadEventsFromCSV();
        eventTable.refresh();
        showAlert("完成", "資料已重新整理！", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/主辦人主畫面.fxml"));
            Parent root = loader.load();

            HostMainController controller = loader.getController();
            if (currentOrganizer != null) {
                controller.setOrganizer(currentOrganizer);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("主辦人管理系統");
            stage.show();
        } catch (IOException e) {
            System.err.println("無法載入主辦人主畫面：" + e.getMessage());
            e.printStackTrace();
            showAlert("載入失敗", "無法返回主畫面，請稍後再試。", Alert.AlertType.ERROR);
        }
    }

    private String generateEventId() {
        Set<String> existingIds = new HashSet<>();

        // 讀取CSV檔案中所有現有的活動編號
        File file = new File(csvPath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                String line;
                boolean firstLine = true;

                while ((line = br.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue; // 跳過標題行
                    }

                    String[] tokens = line.split(",", -1);
                    if (tokens.length >= 1) {
                        String existingId = tokens[0].trim();
                        if (!existingId.isEmpty()) {
                            existingIds.add(existingId);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("讀取現有活動編號時發生錯誤：" + e.getMessage());
            }
        }

        // 生成唯一的活動編號
        int counter = 1;
        String newId;

        do {
            newId = "A" + String.format("%02d", counter);
            counter++;
        } while (existingIds.contains(newId));

        System.out.println("生成新的活動編號：" + newId);
        return newId;
    }

    /**
     * 只加载当前主办人的活动，并正确显示主办单位名称
     */
    private void loadEventsFromCSV() {
        if (currentOrganizer == null) return;
        eventList.clear();

        File file = new File(csvPath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                loadFromBufferedReader(br);
                System.out.println("從檔案載入活動資料：" + csvPath + "，當前主辦人 " + currentOrganizer.getName() + " 共有 " + eventList.size() + " 個活動");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("錯誤", "無法讀取活動列表檔案：" + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            System.out.println("CSV 檔案不存在：" + csvPath + "，建立空白活動列表");
            try {
                saveEventsToCSV();
            } catch (Exception e) {
                System.out.println("無法創建 CSV 檔案：" + e.getMessage());
            }
        }
    }

    private void loadFromBufferedReader(BufferedReader br) throws IOException {
        String line;
        boolean firstLine = true;
        int lineNumber = 0;

        while ((line = br.readLine()) != null) {
            lineNumber++;

            if (firstLine) {
                firstLine = false;
                System.out.println("CSV 標題行：" + line);
                continue;
            }

            String[] tokens = line.split(",", -1);
            System.out.println("處理第 " + lineNumber + " 行：" + line);

            if (tokens.length >= 6) {
                try {
                    String id = tokens[0].trim();
                    String title = tokens[1].trim();
                    String location = tokens[2].trim();
                    String time = tokens[3].trim();
                    String organizerId = tokens[4].trim();
                    int capacity = Integer.parseInt(tokens[5].trim());

                    System.out.println("解析資料 - ID: " + id + ", 標題: " + title + ", 主辦人ID: " + organizerId + ", 當前主辦人ID: " + (currentOrganizer != null ? currentOrganizer.getId() : "null"));

                    if (organizerId.isEmpty()) {
                        System.out.println("警告：活動 '" + title + "' 的主辦單位為空，跳過此筆資料");
                        continue;
                    }

                    // 根据ID获取主办人名称
                    String organizerName = getUserNameById(organizerId);

                    // 创建主办人对象，使用正确的名称
                    Organizer organizer = new Organizer(organizerId, organizerName, "");

                    // 只显示当前主办人的活动
                    if (currentOrganizer != null && organizerId.equals(currentOrganizer.getId())) {
                        Event event = new Event(id, title, location, time, capacity, organizer);
                        eventList.add(event);
                        System.out.println("成功加入活動：" + title + "，主办人：" + organizerName);
                    } else {
                        System.out.println("主辦人ID不符，跳過活動：" + title + " (活動主辦人：" + organizerName + "，當前登入：" + currentOrganizer.getName() + ")");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("解析數字時發生錯誤 (第" + lineNumber + "行)：" + line + ", 錯誤：" + e.getMessage());
                }
            } else {
                System.out.println("資料格式不正確 (第" + lineNumber + "行，欄位數量: " + tokens.length + ")：" + line);
            }
        }
    }

    /**
     * 保存活动到CSV - 保存时使用主办单位ID
     */
    private void saveEventsToCSV() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvPath), "UTF-8"))) {

            // 写入标题行
            bw.write("活動編號,標題,地點,時間,主辦單位,名額");
            bw.newLine();

            // 写入活动资料 - 主办单位字段保存ID而非名称
            for (Event event : eventList) {
                String organizerId = event.getOrganizer().getId();
                if (organizerId == null || organizerId.isEmpty()) {
                    // 如果没有ID，使用当前主办人的ID
                    organizerId = currentOrganizer != null ? currentOrganizer.getId() : "";
                }

                String line = String.format("%s,%s,%s,%s,%s,%d",
                        event.getId() != null ? event.getId() : generateEventId(),
                        event.getTitle(),
                        event.getLocation(),
                        event.getTime(),
                        organizerId,  // 保存ID而非名称
                        event.getCapacity());
                bw.write(line);
                bw.newLine();
            }

            System.out.println("活動資料已成功儲存到 CSV 檔案");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("錯誤", "無法儲存活動列表！", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}