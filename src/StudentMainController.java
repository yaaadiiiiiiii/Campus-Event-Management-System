import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

    // 新增：靜態變數保存當前登入的學生資訊
    private static Student currentLoggedInStudent;

    private Student student;

    @FXML
    private Button browseEventsButton;

    @FXML
    private Button registerEventsButton;

    @FXML
    private Button logoutButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("學生主畫面已載入");

        // 如果本地 student 為空，嘗試從靜態變數恢復
        if (student == null && currentLoggedInStudent != null) {
            student = currentLoggedInStudent;
        }
    }

    // 新增：靜態方法設定當前登入學生
    public static void setCurrentLoggedInStudent(Student student) {
        currentLoggedInStudent = student;
    }

    // 新增：靜態方法獲取當前登入學生
    public static Student getCurrentLoggedInStudent() {
        return currentLoggedInStudent;
    }

    // 新增：靜態方法清除登入資訊
    public static void clearLoggedInStudent() {
        currentLoggedInStudent = null;
    }

    public void setStudent(Student student) {
        this.student = student;
        // 同時保存到靜態變數
        currentLoggedInStudent = student;
    }

    /**
     * 獲取當前學生（優先使用靜態變數）
     */
    private Student getCurrentStudent() {
        // 優先使用靜態變數中的資訊
        if (currentLoggedInStudent != null) {
            return currentLoggedInStudent;
        }
        return student;
    }

    /**
     * 處理瀏覽活動按鈕點擊事件
     */
    @FXML
    private void handleBrowseEvents(ActionEvent event) {
        Student currentStudent = getCurrentStudent();
        if (currentStudent == null) {
            showErrorAlert("錯誤", "學生資訊遺失", "請重新登入！");
            handleLogout();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("活動瀏覽.fxml"));
            Parent root = loader.load();

            // 取得 EventBrowserController 並設定學生資訊
            EventBrowserController eventController = loader.getController();
            eventController.setCurrentStudent(currentStudent);

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
        Student currentStudent = getCurrentStudent();
        if (currentStudent == null) {
            showErrorAlert("錯誤", "學生資訊遺失", "請重新登入！");
            handleLogout();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("查詢報名紀錄.fxml"));
            Parent root = loader.load();

            // 取得 RegistrationRecordController 並傳遞學生ID
            RegistrationRecordController controller = loader.getController();
            controller.setCurrentStudentId(currentStudent.getId());

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

    /**
     * 處理登出按鈕點擊事件
     */
    @FXML
    private void handleLogout() {
        try {
            // 清除靜態變數中的學生資訊
            clearLoggedInStudent();

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
     * 返回主選單
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