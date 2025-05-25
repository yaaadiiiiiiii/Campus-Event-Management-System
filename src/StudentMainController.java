import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StudentMainController implements Initializable {

    @FXML
    private Button browseEventsButton;

    @FXML
    private Button registerEventsButton;

    @FXML private Button logoutButton;           // 登出按鈕

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化方法，可以在此處設置初始狀態
        System.out.println("學生主畫面已載入");
    }

    /**
     * 處理瀏覽活動按鈕點擊事件
     */
    @FXML
    private void handleBrowseEvents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("活動瀏覽.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) browseEventsButton.getScene().getWindow();

            Scene scene = new Scene(root, 800, 500);
            stage.setScene(scene);
            stage.setTitle("瀏覽活動");

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法載入瀏覽活動頁面", e.getMessage());
        }
    }

    /**
     * 處理報名活動按鈕點擊事件
     */
    @FXML
    private void handleRegisterEvents(ActionEvent event) {
        try {
            // 載入報名活動頁面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("查詢報名紀錄.fxml"));
            Parent root = loader.load();

            // 獲取當前視窗
            Stage stage = (Stage) registerEventsButton.getScene().getWindow();

            // 設置新場景
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("報名活動");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法載入報名活動頁面", e.getMessage());
        }
    }

    /**
     * 處理登出按鈕點擊事件
     */
    @FXML
    private void handleLogout() {
        try {
            // 回到登入畫面
            Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("系統登入");
            stage.show();

        } catch (IOException e) {
            System.err.println("無法載入登入畫面：" + e.getMessage());
            e.printStackTrace();
            // 如果無法載入登入畫面，至少關閉當前視窗
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * 顯示錯誤警告對話框
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 顯示資訊對話框
     */
    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 返回主選單（如果需要的話）
     */
    public void returnToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("學生主畫面.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) browseEventsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("主選單");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法返回主選單", e.getMessage());
        }
    }
}