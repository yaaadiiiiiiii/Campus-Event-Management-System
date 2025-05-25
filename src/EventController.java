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

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 設定表格欄位與資料的對應
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        organizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizer"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        // 設定操作欄位的按鈕
        setupActionColumn();

        // 載入初始資料
        loadSampleData();
        eventTable.setItems(eventList);
    }

    private void setupActionColumn() {
        Callback<TableColumn<Event, Void>, TableCell<Event, Void>> cellFactory =
                new Callback<TableColumn<Event, Void>, TableCell<Event, Void>>() {
                    @Override
                    public TableCell<Event, Void> call(final TableColumn<Event, Void> param) {
                        final TableCell<Event, Void> cell = new TableCell<Event, Void>() {

                            private final Button editBtn = new Button("編輯");
                            private final Button deleteBtn = new Button("刪除");
                            private final HBox hbox = new HBox(editBtn, deleteBtn);

                            {
                                // 設定按鈕樣式
                                editBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                                hbox.setSpacing(5);

                                // 設定按鈕事件
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
                        return cell;
                    }
                };

        actionColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddEvent() {
        EventDialog dialog = new EventDialog();
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(event -> {
            eventList.add(event);
            showAlert("成功", "活動已成功新增！", Alert.AlertType.INFORMATION);
        });
    }

    private void handleEditEvent(Event event) {
        EventDialog dialog = new EventDialog(event);
        Optional<Event> result = dialog.showAndWait();

        result.ifPresent(updatedEvent -> {
            // 更新原有活動的資料
            event.setTitle(updatedEvent.getTitle());
            event.setLocation(updatedEvent.getLocation());
            event.setTime(updatedEvent.getTime());
            event.setOrganizer(updatedEvent.getOrganizer().getId());
            event.setCapacity(updatedEvent.getCapacity());

            // 重新整理表格
            eventTable.refresh();
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
            showAlert("成功", "活動已成功刪除！", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRefresh() {
        eventTable.refresh();
        showAlert("完成", "資料已重新整理！", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToMain() {
        try {
            // 回到主辦人主畫面
            Parent root = FXMLLoader.load(getClass().getResource("/主辦人主畫面.fxml"));
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

    private void loadSampleData() {
        // 載入一些範例資料
        eventList.addAll(
                new Event("程式設計競賽", "電算中心", "2025-06-01 09:00", "資訊系", 50),
                new Event("音樂會", "大禮堂", "2025-06-15 19:00", "音樂系", 200),
                new Event("學術研討會", "國際會議廳", "2025-06-20 13:30", "研發處", 100)
        );
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}