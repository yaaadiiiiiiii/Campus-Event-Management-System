import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;

public class login {

    @FXML
    private TextField studentIdField;
    @FXML
    private TextField passwordField;

    @FXML
    public void handleLogin() {
        String userId = studentIdField.getText().trim();
        String password = passwordField.getText().trim();
        System.out.println("登入嘗試，帳號：" + userId + "，密碼：" + password);

        if (userId.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "輸入錯誤", "請輸入帳號和密碼。");
            return;
        }

        String userRole = validateUser(userId, password);
        if (userRole != null) {
            String welcomeMessage;
            String nextPage;

            if (userRole.equals("h")) {
                welcomeMessage = "歡迎主辦人使用系統！";
                nextPage = "/login.fxml"; // 主辦人介面
            } else {
                welcomeMessage = "歡迎學生使用系統！";
                nextPage = "/學生主畫面.fxml"; // 學生介面
            }

            showAlert(AlertType.INFORMATION, "登入成功", welcomeMessage);

            // 關閉登入視窗並開啟對應介面
            redirectToNextPage(nextPage);

        } else {
            showAlert(AlertType.ERROR, "登入失敗", "帳號或密碼錯誤，請重新輸入。");
            // 清空密碼欄位，保留帳號
            passwordField.clear();
        }
    }

    /**
     * 驗證使用者身份
     * @param userId 使用者ID
     * @return 如果使用者存在，回傳角色('h'代表主辦人，'s'代表學生)，否則回傳null
     */
    private String validateUser(String userId, String password) {
        try (InputStream inputStream = getClass().getResourceAsStream("/users.csv");
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                System.err.println("找不到 users.csv 檔案");
                return null;
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length >= 3) {
                    String csvUserId = parts[0].trim();
                    String csvPassword = parts[1].trim();
                    String csvRole = parts[2].trim();

                    if (csvUserId.equals(userId) && csvPassword.equals(password)) {
                        System.out.println("驗證成功 - 使用者：" + userId + "，角色：" + csvRole);
                        return csvRole; // 回傳角色
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("讀取 CSV 檔案時發生錯誤：" + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 導向對應的介面
     * @param fxmlPath FXML檔案路徑
     */
    private void redirectToNextPage(String fxmlPath) {
        try {
            // 載入新的場景
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) studentIdField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("系統主頁");
            stage.show();
        } catch (IOException e) {
            System.err.println("無法載入介面：" + fxmlPath);
            e.printStackTrace();
            // 如果無法載入指定介面，至少關閉登入視窗
            Stage stage = (Stage) studentIdField.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}