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

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class EventBrowserController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, String> eventNameColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, String> timeColumn;
    @FXML private TableColumn<Event, String> organizerColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TableColumn<Event, Void> actionColumn;
    @FXML private Button backButton;

    private ObservableList<Event> eventList = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEventList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadSampleData();
        eventTable.setItems(filteredEventList);
    }

    private void setupTableColumns() {
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
                            if (event.isRegistered()) {
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

    private void loadSampleData() {
        loadEventsFromCSV();
        filteredEventList.setAll(eventList);
    }

    private void loadEventsFromCSV() {
        try {
            // 嘗試從 resources 資料夾載入 CSV 檔案
            InputStream inputStream = getClass().getResourceAsStream("/活動列表.csv");
            if (inputStream == null) {
                // 如果在 resources 根目錄找不到，嘗試在同一個套件目錄下尋找
                inputStream = getClass().getResourceAsStream("活動列表.csv");
            }

            if (inputStream == null) {
                System.out.println("錯誤：找不到活動列表.csv檔案");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 跳過標題行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 解析 CSV 行
                String[] parts = line.split(",", -1);

                if (parts.length >= 5) {
                    String eventName = parts[0].trim();
                    String location = parts[1].trim();
                    String time = parts[2].trim();
                    String organizer = parts[3].trim();
                    int remainingCapacity;

                    try {
                        remainingCapacity = Integer.parseInt(parts[4].trim());
                    } catch (NumberFormatException e) {
                        remainingCapacity = 0;
                    }

                    Event event = new Event(
                            eventName,
                            location,
                            time,
                            organizer,
                            remainingCapacity
                    );

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
                        event.getOrganizer().toLowerCase().contains(searchText)) {
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
            // 返回學生主畫面
            Parent root = FXMLLoader.load(getClass().getResource("/學生主畫面.fxml"));
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
        if (event.isRegistered()) {
            // 如果已經報名，顯示已報名訊息
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("已報名");
            alert.setHeaderText(null);
            alert.setContentText("您已經報名過「" + event.getEventName() + "」活動！");
            alert.showAndWait();

        } else if (event.getRemainingCapacity() > 0) {
            // 執行報名
            event.setRemainingCapacity(event.getRemainingCapacity() - 1);
            event.setRegistered(true);
            eventTable.refresh();

            // 更新 CSV 檔案
            saveEventsToCSV();

            // 顯示報名成功訊息
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("報名成功");
            alert.setHeaderText(null);
            alert.setContentText("恭喜！您已成功報名「" + event.getEventName() + "」活動。\n" +
                    "剩餘名額：" + event.getRemainingCapacity());
            alert.showAndWait();

        } else {
            // 顯示額滿訊息
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("報名失敗");
            alert.setHeaderText(null);
            alert.setContentText("很抱歉，「" + event.getEventName() + "」活動名額已滿！");
            alert.showAndWait();
        }
    }

    private void saveEventsToCSV() {
        try {
            String csvPath = "src/main/resources/活動列表.csv";
            File csvFile = new File(csvPath);

            if (!csvFile.exists()) {
                csvPath = "活動列表.csv";
                csvFile = new File(csvPath);
            }

            FileWriter writer = new FileWriter(csvFile, false);
            PrintWriter printWriter = new PrintWriter(writer);

            // 寫入標題行
            printWriter.println("標題,地點,時間,主辦單位,名額");

            // 寫入所有活動資料
            for (Event event : eventList) {
                printWriter.printf("%s,%s,%s,%s,%d%n",
                        event.getEventName(),
                        event.getLocation(),
                        event.getTime(),
                        event.getOrganizer(),
                        event.getRemainingCapacity()
                );
            }

            printWriter.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("錯誤");
            alert.setHeaderText(null);
            alert.setContentText("無法更新 CSV 檔案: " + e.getMessage());
            alert.showAndWait();
        }
    }



    // Event 類別
    public static class Event {
        private final SimpleStringProperty eventName;
        private final SimpleStringProperty location;
        private final SimpleStringProperty time;
        private final SimpleStringProperty organizer;
        private final SimpleIntegerProperty remainingCapacity;
        private final SimpleBooleanProperty registered = new SimpleBooleanProperty(false);

        public Event(String eventName, String location, String time, String organizer, int remainingCapacity) {
            this.eventName = new SimpleStringProperty(eventName);
            this.location = new SimpleStringProperty(location);
            this.time = new SimpleStringProperty(time);
            this.organizer = new SimpleStringProperty(organizer);
            this.remainingCapacity = new SimpleIntegerProperty(remainingCapacity);
        }

        // 新增報名方法
        public boolean register() {
            if (remainingCapacity.get() > 0 && !registered.get()) {
                remainingCapacity.set(remainingCapacity.get() - 1);
                registered.set(true);
                return true;
            }
            return false;
        }

        // 檢查是否可以報名
        public boolean canRegister() {
            return remainingCapacity.get() > 0 && !registered.get();
        }

        // 檢查是否已額滿
        public boolean isFull() {
            return remainingCapacity.get() <= 0;
        }


        // Getter methods
        public String getEventName() { return eventName.get(); }
        public String getLocation() { return location.get(); }
        public String getTime() { return time.get(); }
        public String getOrganizer() { return organizer.get(); }
        public int getRemainingCapacity() { return remainingCapacity.get(); }
        public boolean isRegistered() { return registered.get(); }

        // Setter methods

        public void setRemainingCapacity(int remainingCapacity) { this.remainingCapacity.set(remainingCapacity); }
        public void setRegistered(boolean value) { this.registered.set(value); }
    }
}