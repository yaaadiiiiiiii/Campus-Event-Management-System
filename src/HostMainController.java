import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HostMainController {

    @FXML private Button eventManagementButton;  // 活動管理按鈕
    @FXML private Button logoutButton;           // 登出按鈕

    /**
     * 處理活動管理按鈕點擊事件
     */
    @FXML
    private void handleEventManagement() {
        try {
            // 載入活動管理畫面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/活動管理.fxml"));
            Parent root = loader.load();

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