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

import java.io.*;
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

    private final String csvPath = "活動列表.csv"; // 檔案名可根據需要更換

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        organizerColumn.setCellValueFactory(new PropertyValueFactory<>("organizer"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        setupActionColumn();
        loadEventsFromCSV(csvPath);
        eventTable.setItems(eventList);
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
            eventList.add(event);
            saveEventsToCSV(csvPath);
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
            event.setOrganizer(updatedEvent.getOrganizer());
            event.setCapacity(updatedEvent.getCapacity());
            eventTable.refresh();
            saveEventsToCSV(csvPath);
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
            saveEventsToCSV(csvPath);
            showAlert("成功", "活動已成功刪除！", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRefresh() {
        loadEventsFromCSV(csvPath);
        eventTable.refresh();
        showAlert("完成", "資料已重新整理！", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToMain() {
        try {
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

    /*** CSV 載入與存檔邏輯 ***/
    private void loadEventsFromCSV(String path) {
        eventList.clear();
        File file = new File(path);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // 跳過標題
                String[] tokens = line.split(",", -1);
                if (tokens.length >= 5) {
                    String title = tokens[0];
                    String location = tokens[1];
                    String time = tokens[2];
                    String organizer = tokens[3];
                    int capacity = Integer.parseInt(tokens[4]);
                    eventList.add(new Event(title, location, time, organizer, capacity));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("錯誤", "無法讀取活動列表！", Alert.AlertType.ERROR);
        }
    }

    private void saveEventsToCSV(String path) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
            bw.write("標題,地點,時間,主辦單位,名額");
            bw.newLine();
            for (Event event : eventList) {
                String line = String.format("%s,%s,%s,%s,%d",
                        event.getTitle(), event.getLocation(), event.getTime(),
                        event.getOrganizer(), event.getCapacity());
                bw.write(line);
                bw.newLine();
            }
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
