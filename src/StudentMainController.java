import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.Student;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class StudentMainController implements Initializable {

    private Student student;

    @FXML
    private Button browseEventsButton;

    @FXML
    private Button registerEventsButton;

    @FXML
    private Button logoutButton;           // 登出按鈕

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("學生主畫面已載入");
    }

    // 只保留這一個正確的 setStudent(Student)
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * 處理瀏覽活動按鈕點擊事件
     */
    @FXML
    private void handleBrowseEvents(ActionEvent event) {
        if (student == null) {
            showErrorAlert("錯誤", "學生資訊遺失", "請重新登入！");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("活動瀏覽.fxml"));
            Parent root = loader.load();

            // 取得 EventBrowserController 並設定學生資訊
            EventBrowserController eventController = loader.getController();
            eventController.setCurrentStudent(student); // 傳遞學生物件

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
     * 處理報名活動按鈕點擊事件（查詢報名紀錄）
     */
    @FXML
    private void handleRegisterEvents(ActionEvent event) {
        if (student == null) {
            showErrorAlert("錯誤", "學生資訊遺失", "請重新登入！");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("查詢報名紀錄.fxml"));
            Parent root = loader.load();

            // 取得 RegistrationRecordController 並傳遞學生ID
            RegistrationRecordController controller = loader.getController();
            controller.setCurrentStudentId(student.getId());

            Stage stage = (Stage) registerEventsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("查詢報名紀錄");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法載入報名活動頁面", e.getMessage());
        }
    }

    // 在學生主畫面控制器中，當點擊「查詢報名紀錄」按鈕時：

    @FXML
    private void handleViewRegistrationRecords(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("報名紀錄查詢.fxml"));
            Parent root = loader.load();

            // 取得控制器並設定當前學生ID
            RegistrationRecordController controller = loader.getController();
            controller.setCurrentStudentId(currentStudentId); // currentStudentId 是當前登入的學生ID

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("報名紀錄查詢");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("錯誤", "無法開啟報名紀錄頁面", e.getMessage());
        }
    }

    // 假設您的學生主畫面控制器有一個設定當前學生的方法：
    private String currentStudentId;

    public void setCurrentStudent(String studentId) {
        this.currentStudentId = studentId;
        // 其他初始化邏輯...
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
