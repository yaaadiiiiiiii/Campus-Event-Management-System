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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

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
    private Organizer currentOrganizer; // 添加當前主辦人資訊

    private final String csvPath = "活動列表.csv";

    public void setCurrentOrganizer(Organizer organizer) {
        this.currentOrganizer = organizer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadEventsFromCSV();
        eventTable.setItems(eventList);
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
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

    @FXML
    private void handleAddEvent() {
        EventDialog dialog = new EventDialog();
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(event -> {
            // 生成唯一ID
            String eventId = generateEventId();
            // 設定當前主辦人
            if (currentOrganizer != null) {
                event.setOrganizer(currentOrganizer);
            }
            // 創建新的Event物件，包含ID
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
            // 保持原主辦人
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
        loadEventsFromCSV();
        eventTable.refresh();
        showAlert("完成", "資料已重新整理！", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToMain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/主辦人主畫面.fxml"));
            Parent root = loader.load();

            // 傳遞主辦人資訊回主畫面
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

    // 生成唯一活動ID
    private String generateEventId() {
        return "A" + String.format("%02d", eventList.size() + 1);
    }

    // CSV 載入方法 - 修正版本
    private void loadEventsFromCSV() {
        eventList.clear();

        // 先嘗試讀取 resources 資料夾中的檔案
        try (InputStream inputStream = getClass().getResourceAsStream("/活動列表.csv")) {
            if (inputStream != null) {
                loadFromInputStream(inputStream);
                return;
            }
        } catch (IOException e) {
            System.out.println("無法從 resources 讀取檔案，嘗試從當前目錄讀取...");
        }

        // 如果 resources 中沒有，嘗試從當前目錄讀取
        File file = new File(csvPath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                loadFromBufferedReader(br);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("錯誤", "無法讀取活動列表！", Alert.AlertType.ERROR);
            }
        } else {
            System.out.println("CSV 檔案不存在，將建立新檔案");
        }
    }

    private void loadFromInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream, "UTF-8"))) {
            loadFromBufferedReader(br);
        }
    }

    private void loadFromBufferedReader(BufferedReader br) throws IOException {
        String line;
        boolean firstLine = true;

        while ((line = br.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue; // 跳過標題行
            }

            String[] tokens = line.split(",", -1);
            if (tokens.length >= 6) {
                try {
                    String id = tokens[0].trim();
                    String title = tokens[1].trim();
                    String location = tokens[2].trim();
                    String time = tokens[3].trim();
                    String organizerName = tokens[4].trim();
                    int capacity = Integer.parseInt(tokens[5].trim());

                    // 創建主辦人物件
                    Organizer organizer = new Organizer("", organizerName, "");

                    // 創建活動物件
                    Event event = new Event(id, title, location, time, capacity, organizer);
                    eventList.add(event);

                } catch (NumberFormatException e) {
                    System.out.println("解析數字時發生錯誤：" + line);
                }
            }
        }

        System.out.println("成功載入 " + eventList.size() + " 個活動");
    }

    private void saveEventsToCSV() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(csvPath), "UTF-8"))) {

            // 寫入標題行（與 CSV 格式一致）
            bw.write("活動編號,標題,地點,時間,主辦單位,名額");
            bw.newLine();

            // 寫入活動資料
            for (Event event : eventList) {
                String line = String.format("%s,%s,%s,%s,%s,%d",
                        event.getId() != null ? event.getId() : generateEventId(),
                        event.getTitle(),
                        event.getLocation(),
                        event.getTime(),
                        event.getOrganizer().getName(),
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