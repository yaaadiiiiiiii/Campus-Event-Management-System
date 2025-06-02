import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Organizer;

import java.io.IOException;

public class HostMainController {

    private Organizer organizer;

    @FXML private Button eventManagementButton;  // 活動管理按鈕
    @FXML private Button registrationListButton; // 修正：改為正確的按鈕名稱
    @FXML private Button logoutButton;           // 登出按鈕
    @FXML private Label welcomeLabel;            // 歡迎標籤（可選）

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
        // 更新主辦人資訊顯示
        if (welcomeLabel != null && organizer != null) {
            welcomeLabel.setText("歡迎，" + organizer.getName() + "!");
        }
        System.out.println("設定主辦人：" + organizer.getName() + " (ID: " + organizer.getId() + ")");
    }

    /**
     * 處理活動管理按鈕點擊事件
     */
    @FXML
    private void handleEventManagement() {
        try {
            // 載入活動管理畫面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/活動管理.fxml"));
            Parent root = loader.load();

            // 取得 EventController 並設定當前主辦人
            EventController controller = loader.getController();
            if (organizer != null) {
                controller.setCurrentOrganizer(organizer);
                System.out.println("已將主辦人資訊傳遞到活動管理畫面：" + organizer.getName());
            }

            // 取得當前視窗
            Stage stage = (Stage) eventManagementButton.getScene().getWindow();

            // 設定新場景
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("活動管理系統");
            stage.show();

        } catch (IOException e) {
            System.err.println("無法載入活動管理畫面：" + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "載入失敗", "無法開啟活動管理畫面，請稍後再試。");
        }
    }

    // 處理報名名單查詢按鈕點擊
    @FXML
    private void handleRegistrationList() {
        try {
            // 載入報名名單查詢的 FXML 檔案
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/報名名單查詢.fxml"));
            Parent root = loader.load();

            // 獲取當前窗口
            Stage stage = (Stage) registrationListButton.getScene().getWindow();

            // 設置新場景
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("報名名單查詢");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "載入失敗", "無法開啟報名名單查詢畫面，請稍後再試。");
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
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("系統登入");
            stage.show();

            System.out.println("主辦人 " + (organizer != null ? organizer.getName() : "未知") + " 已登出");

        } catch (IOException e) {
            System.err.println("無法載入登入畫面：" + e.getMessage());
            e.printStackTrace();
            // 如果無法載入登入畫面，至少關閉當前視窗
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * 顯示提示訊息
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}