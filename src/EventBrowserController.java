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

import java.io.IOException;
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

        // 設置報名按鈕欄位
        actionColumn.setCellFactory(new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
            @Override
            public TableCell<Event, Void> call(TableColumn<Event, Void> param) {
                return new TableCell<Event, Void>() {
                    private final Button registerButton = new Button("報名");

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
                            if (event.getRemainingCapacity() <= 0) {
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
        eventList.addAll(
                new Event("程式設計工作坊", "電腦教室A", "2024-06-15 14:00", "資訊科學系", 20),
                new Event("創業講座", "演講廳B", "2024-06-20 10:00", "商管學院", 50),
                new Event("攝影比賽", "校園各處", "2024-06-25 全天", "攝影社", 30),
                new Event("英語角", "語言中心", "2024-06-18 18:00", "英語學習中心", 15),
                new Event("音樂會", "音樂廳", "2024-06-22 19:30", "音樂系", 100),
                new Event("籃球比賽", "體育館", "2024-06-28 15:00", "體育系", 0), // 測試額滿狀態
                new Event("文學講座", "圖書館", "2024-07-01 10:00", "中文系", 25)
        );
        filteredEventList.setAll(eventList);
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
        if (event.getRemainingCapacity() > 0) {
            // 報名成功，減少剩餘名額
            event.setRemainingCapacity(event.getRemainingCapacity() - 1);

            // 刷新表格
            eventTable.refresh();

            // 顯示成功訊息
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

    // Event 類別
    public static class Event {
        private final SimpleStringProperty eventName;
        private final SimpleStringProperty location;
        private final SimpleStringProperty time;
        private final SimpleStringProperty organizer;
        private final SimpleIntegerProperty remainingCapacity;

        public Event(String eventName, String location, String time, String organizer, int remainingCapacity) {
            this.eventName = new SimpleStringProperty(eventName);
            this.location = new SimpleStringProperty(location);
            this.time = new SimpleStringProperty(time);
            this.organizer = new SimpleStringProperty(organizer);
            this.remainingCapacity = new SimpleIntegerProperty(remainingCapacity);
        }

        // Getter methods
        public String getEventName() { return eventName.get(); }
        public String getLocation() { return location.get(); }
        public String getTime() { return time.get(); }
        public String getOrganizer() { return organizer.get(); }
        public int getRemainingCapacity() { return remainingCapacity.get(); }

        // Setter methods
        public void setEventName(String eventName) { this.eventName.set(eventName); }
        public void setLocation(String location) { this.location.set(location); }
        public void setTime(String time) { this.time.set(time); }
        public void setOrganizer(String organizer) { this.organizer.set(organizer); }
        public void setRemainingCapacity(int remainingCapacity) { this.remainingCapacity.set(remainingCapacity); }

        // Property methods for TableView binding
        public SimpleStringProperty eventNameProperty() { return eventName; }
        public SimpleStringProperty locationProperty() { return location; }
        public SimpleStringProperty timeProperty() { return time; }
        public SimpleStringProperty organizerProperty() { return organizer; }
        public SimpleIntegerProperty remainingCapacityProperty() { return remainingCapacity; }
    }
}