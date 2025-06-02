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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Event;
import model.Organizer;

import java.io.*;
import java.net.URL;
import java.util.*;

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
    @FXML private Button importButton;
    @FXML private Button exportButton;
    @FXML private TextField searchField;
    @FXML private Button searchButton;

    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEventList = FXCollections.observableArrayList();
    private Organizer currentOrganizer;

    // 靜態變數來保存當前使用者資訊，避免在畫面切換時遺失
    private static Organizer globalCurrentOrganizer;

    // 用户ID到名称的映射
    private Map<String, String> userIdToNameMap = new HashMap<>();

    // CSV檔案路徑 - 支援多種路徑
    private final String[] csvPaths = {
            "活動列表.csv",
            "src/活動列表.csv",
            "src/main/resources/活動列表.csv",
            "./活動列表.csv",
            "../活動列表.csv"
    };

    private final String usersPath = "src/users.csv";

    public void setCurrentOrganizer(Organizer organizer) {
        this.currentOrganizer = organizer;
        globalCurrentOrganizer = organizer; // 同時設置靜態變數
        System.out.println("設置當前主辦人：" + (organizer != null ? organizer.getName() : "null"));
        loadUserIdToNameMapping();
        loadEventsFromCSV();
    }

    // 新增：獲取當前主辦人的靜態方法
    public static Organizer getGlobalCurrentOrganizer() {
        return globalCurrentOrganizer;
    }

    // 新增：設置全域當前主辦人的靜態方法
    public static void setGlobalCurrentOrganizer(Organizer organizer) {
        globalCurrentOrganizer = organizer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        eventTable.setItems(filteredEventList);

        // 如果當前主辦人為空，嘗試從靜態變數恢復
        if (currentOrganizer == null && globalCurrentOrganizer != null) {
            System.out.println("從靜態變數恢復當前主辦人：" + globalCurrentOrganizer.getName());
            setCurrentOrganizer(globalCurrentOrganizer);
        }

        // 初始化搜尋功能
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterEvents(newValue);
            });
        }
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
     * 搜尋過濾功能
     */
    private void filterEvents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredEventList.setAll(eventList);
        } else {
            String lowerCaseFilter = searchText.toLowerCase().trim();
            filteredEventList.clear();

            for (Event event : eventList) {
                if (event.getTitle().toLowerCase().contains(lowerCaseFilter) ||
                        event.getLocation().toLowerCase().contains(lowerCaseFilter) ||
                        event.getTime().toLowerCase().contains(lowerCaseFilter) ||
                        event.getOrganizer().getName().toLowerCase().contains(lowerCaseFilter)) {
                    filteredEventList.add(event);
                }
            }
        }
    }

    @FXML
    private void handleSearch() {
        if (searchField != null) {
            filterEvents(searchField.getText());
        }
    }

    /**
     * 加载用户ID到名称的映射
     */
    private void loadUserIdToNameMapping() {
        userIdToNameMap.clear();

        // 嘗試多個可能的路徑
        String[] usersPaths = {"users.csv", "src/users.csv", "src/main/resources/users.csv"};

        for (String path : usersPaths) {
            File usersFile = new File(path);
            if (usersFile.exists()) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(usersFile), "UTF-8"))) {

                    String line;
                    boolean isFirstLine = true;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue; // 跳過標題行
                        }

                        String[] tokens = line.split(",", -1);
                        if (tokens.length >= 2) {
                            String userId = tokens[0].trim();
                            String userName = tokens[1].trim();
                            userIdToNameMap.put(userId, userName);
                        }
                    }
                    System.out.println("從 " + path + " 加載用户映射完成，共 " + userIdToNameMap.size() + " 個用户");
                    break; // 成功載入就停止嘗試其他路徑

                } catch (IOException e) {
                    System.err.println("無法讀取用户文件 " + path + "：" + e.getMessage());
                }
            }
        }

        if (userIdToNameMap.isEmpty()) {
            System.out.println("警告：未找到用户文件或用户映射為空");
        }
    }

    /**
     * 根据用户ID获取用户名称
     */
    private String getUserNameById(String userId) {
        return userIdToNameMap.getOrDefault(userId, userId);
    }

    @FXML
    private void handleAddEvent() {
        // 確保有當前主辦人
        if (currentOrganizer == null) {
            showAlert("錯誤", "無法識別當前使用者，請重新登入", Alert.AlertType.ERROR);
            return;
        }

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
            filteredEventList.add(newEvent);
            saveEventsToCSV();
            showAlert("成功", "活動已成功新增！", Alert.AlertType.INFORMATION);
        });
    }

    private void handleEditEvent(Event event) {
        EventDialog dialog = new EventDialog(event, currentOrganizer);
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(updatedEvent -> {
            event.setTitle(updatedEvent.getTitle());
            event.setLocation(updatedEvent.getLocation());
            event.setTime(updatedEvent.getTime());
            event.setCapacity(updatedEvent.getCapacity());

            eventTable.refresh();
            saveEventsToCSV();
            showAlert("成功", "活動已成功更新！", Alert.AlertType.INFORMATION);
        });
    }

    private void handleDeleteEvent(Event event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("確認刪除");
        alert.setHeaderText("您確定要刪除这個活動嗎？");
        alert.setContentText("活動：" + event.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eventList.remove(event);
            filteredEventList.remove(event);
            saveEventsToCSV();
            showAlert("成功", "活動已成功刪除！", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * 導入CSV檔案
     */
    @FXML
    private void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("選擇CSV檔案");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV檔案", "*.csv")
        );

        Stage stage = (Stage) addButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            importEventsFromCSV(selectedFile);
        }
    }

    /**
     * 匯出CSV檔案
     */
    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("儲存CSV檔案");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV檔案", "*.csv")
        );
        fileChooser.setInitialFileName("活動列表_" +
                java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                ) + ".csv");

        Stage stage = (Stage) addButton.getScene().getWindow();
        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile != null) {
            exportEventsToCSV(selectedFile);
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("=== 開始重新整理 ===");
        System.out.println("當前主辦人：" + (currentOrganizer != null ?
                currentOrganizer.getId() + " (" + currentOrganizer.getName() + ")" : "null"));

        // 確保有當前主辦人
        if (currentOrganizer == null && globalCurrentOrganizer != null) {
            currentOrganizer = globalCurrentOrganizer;
        }

        loadUserIdToNameMapping();
        loadEventsFromCSV();
        eventTable.refresh();

        System.out.println("eventList 大小：" + eventList.size());
        System.out.println("filteredEventList 大小：" + filteredEventList.size());

        showAlert("完成", "資料已重新整理！載入了 " + eventList.size() + " 個活動", Alert.AlertType.INFORMATION);
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

    // 其餘方法保持不變...
    private String generateEventId() {
        Set<String> existingIds = new HashSet<>();

        // 從現有活動列表中取得所有ID
        for (Event event : eventList) {
            if (event.getId() != null && !event.getId().isEmpty()) {
                existingIds.add(event.getId());
            }
        }

        // 生成唯一的活動編號
        int counter = 1;
        String newId;

        do {
            newId = "A" + String.format("%03d", counter);
            counter++;
        } while (existingIds.contains(newId));

        System.out.println("生成新的活動編號：" + newId);
        return newId;
    }

    private void loadEventsFromCSV() {
        if (currentOrganizer == null) {
            System.out.println("警告：當前主辦人為空，無法載入活動");
            return;
        }

        eventList.clear();
        String usedPath = null;

        // 嘗試多個可能的CSV檔案路徑
        for (String csvPath : csvPaths) {
            File file = new File(csvPath);
            if (file.exists() && file.canRead()) {
                usedPath = csvPath;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

                    loadFromBufferedReader(br);
                    System.out.println("成功從 " + csvPath + " 載入活動資料，當前主辦人 " +
                            currentOrganizer.getName() + " 共有 " + eventList.size() + " 個活動");
                    break;

                } catch (IOException e) {
                    System.err.println("讀取 " + csvPath + " 時發生錯誤：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        if (usedPath == null) {
            System.out.println("找不到活動列表CSV檔案，嘗試以下路徑：");
            for (String path : csvPaths) {
                System.out.println("  - " + path);
            }
            // 創建一個空的CSV檔案
            createEmptyCSV();
        }

        // 更新過濾列表
        filteredEventList.setAll(eventList);
    }

    private void loadEventsFromCSVResource() {
        try {
            // 嘗試從 resources 目錄載入
            InputStream inputStream = getClass().getResourceAsStream("/活動列表.csv");
            if (inputStream != null) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(inputStream, "UTF-8"));
                loadFromBufferedReader(br);
                br.close();
                return;
            }
        } catch (IOException e) {
            System.err.println("從資源載入失敗：" + e.getMessage());
        }

        // 如果資源載入失敗，使用原有的文件路徑方式
        loadEventsFromCSV();
    }

    private void importEventsFromCSV(File file) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {

            List<Event> importedEvents = new ArrayList<>();
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int importedCount = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (firstLine) {
                    firstLine = false;
                    System.out.println("導入檔案標題行：" + line);
                    continue;
                }

                String[] tokens = line.split(",", -1);

                if (tokens.length >= 5) { // 至少需要5個欄位
                    try {
                        String id = tokens.length > 0 ? tokens[0].trim() : generateEventId();
                        String title = tokens.length > 1 ? tokens[1].trim() : "";
                        String location = tokens.length > 2 ? tokens[2].trim() : "";
                        String time = tokens.length > 3 ? tokens[3].trim() : "";
                        String organizerInfo = tokens.length > 4 ? tokens[4].trim() : "";
                        int capacity = tokens.length > 5 ? Integer.parseInt(tokens[5].trim()) : 0;

                        if (title.isEmpty()) {
                            System.out.println("跳過空標題的活動 (第" + lineNumber + "行)");
                            continue;
                        }

                        // 確保ID唯一
                        if (id.isEmpty() || isEventIdExists(id)) {
                            id = generateEventId();
                        }

                        // 使用當前主辦人
                        Organizer organizer = currentOrganizer;

                        Event event = new Event(id, title, location, time, capacity, organizer);
                        importedEvents.add(event);
                        importedCount++;

                    } catch (NumberFormatException e) {
                        System.out.println("解析數字時發生錯誤 (第" + lineNumber + "行)，跳過此筆資料");
                    }
                } else {
                    System.out.println("資料格式不正確 (第" + lineNumber + "行，欄位數量: " + tokens.length + ")");
                }
            }

            // 將導入的活動加入到列表中
            eventList.addAll(importedEvents);
            filteredEventList.setAll(eventList);

            // 儲存更新後的資料
            saveEventsToCSV();

            showAlert("導入完成",
                    "成功導入 " + importedCount + " 個活動\n共處理 " + (lineNumber - 1) + " 行資料",
                    Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("導入失敗", "無法讀取檔案：" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportEventsToCSV(File file) {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {

            // 寫入標題行
            bw.write("活動編號,標題,地點,時間,主辦單位,名額");
            bw.newLine();

            // 寫入活動資料
            for (Event event : eventList) {
                String line = String.format("%s,%s,%s,%s,%s,%d",
                        event.getId() != null ? event.getId() : "",
                        event.getTitle(),
                        event.getLocation(),
                        event.getTime(),
                        event.getOrganizer().getName(),
                        event.getCapacity());
                bw.write(line);
                bw.newLine();
            }

            showAlert("匯出完成", "成功匯出 " + eventList.size() + " 個活動到檔案", Alert.AlertType.INFORMATION);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("匯出失敗", "無法寫入檔案：" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isEventIdExists(String id) {
        return eventList.stream().anyMatch(event -> id.equals(event.getId()));
    }

    private void createEmptyCSV() {
        try {
            String defaultPath = "活動列表.csv";
            File csvFile = new File(defaultPath);

            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"))) {

                bw.write("活動編號,標題,地點,時間,主辦單位,名額");
                bw.newLine();
                System.out.println("已創建空的CSV檔案：" + defaultPath);
            }
        } catch (IOException e) {
            System.err.println("無法創建CSV檔案：" + e.getMessage());
        }
    }

    private void loadFromBufferedReader(BufferedReader br) throws IOException {
        String line;
        boolean firstLine = true;
        int lineNumber = 0;
        int loadedCount = 0;

        System.out.println("=== 開始載入 CSV 資料 ===");
        System.out.println("當前主辦人：" + (currentOrganizer != null ?
                currentOrganizer.getId() + " (" + currentOrganizer.getName() + ")" : "null"));

        while ((line = br.readLine()) != null) {
            lineNumber++;

            if (firstLine) {
                firstLine = false;
                continue;
            }

            String[] tokens = line.split(",", -1);

            if (tokens.length >= 5) {
                try {
                    String id = tokens[0].trim();
                    String title = tokens[1].trim();
                    String location = tokens[2].trim();
                    String time = tokens[3].trim();
                    String organizerInfo = tokens[4].trim();
                    int capacity = tokens.length > 5 ? Integer.parseInt(tokens[5].trim()) : 0;

                    if (title.isEmpty()) {
                        continue;
                    }

                    // 處理主辦人資訊
                    Organizer organizer;
                    String organizerName = getUserNameById(organizerInfo);

                    if (organizerName.equals(organizerInfo)) {
                        organizerName = organizerInfo;
                    }

                    organizer = new Organizer(organizerInfo, organizerName, "");

                    // 檢查是否應該載入此活動
                    boolean shouldLoad = false;
                    if (currentOrganizer != null) {
                        shouldLoad = organizerInfo.equals(currentOrganizer.getId()) ||
                                organizerInfo.equals(currentOrganizer.getName());
                    } else {
                        shouldLoad = true;
                    }

                    if (shouldLoad) {
                        Event event = new Event(id, title, location, time, capacity, organizer);
                        eventList.add(event);
                        loadedCount++;
                    }

                } catch (NumberFormatException e) {
                    System.out.println("解析數字時發生錯誤 (第" + lineNumber + "行)");
                }
            }
        }

        System.out.println("=== CSV 載入完成 ===");
        System.out.println("成功載入 " + loadedCount + " 個活動");
    }

    private void saveEventsToCSV() {
        String csvPath = findExistingCSVPath();
        if (csvPath == null) {
            csvPath = "活動列表.csv";
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvPath), "UTF-8"))) {

            // 寫入標題行
            bw.write("活動編號,標題,地點,時間,主辦單位,名額");
            bw.newLine();

            // 寫入活動資料
            for (Event event : eventList) {
                String organizerId = event.getOrganizer().getId();
                if (organizerId == null || organizerId.isEmpty()) {
                    organizerId = currentOrganizer != null ? currentOrganizer.getId() : "";
                }

                String line = String.format("%s,%s,%s,%s,%s,%d",
                        event.getId() != null ? event.getId() : generateEventId(),
                        event.getTitle(),
                        event.getLocation(),
                        event.getTime(),
                        organizerId,
                        event.getCapacity());
                bw.write(line);
                bw.newLine();
            }

            System.out.println("活動資料已成功儲存到：" + csvPath);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("錯誤", "無法儲存活動列表！", Alert.AlertType.ERROR);
        }
    }

    private String findExistingCSVPath() {
        for (String path : csvPaths) {
            File file = new File(path);
            if (file.exists() && file.canWrite()) {
                return path;
            }
        }
        return null;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}