import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import model.User;
import model.Student;
import model.Organizer;
import java.io.FileReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class login {

    @FXML
    private TextField studentIdField;
    @FXML
    private TextField passwordField;

    private ArrayList<User> userList = new ArrayList<>();

    // 初始化時讀 users.csv 產生物件
    public void initialize() {
        loadUsersFromCSV();
    }

    private void loadUsersFromCSV() {
        // 嘗試多種編碼方式
        String[] encodings = {"Big5", "MS950", "UTF-8", "GBK"};

        for (String encoding : encodings) {
            try (InputStream inputStream = getClass().getResourceAsStream("/users.csv");
                 BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, encoding))) {

                userList.clear(); // 清空之前的嘗試
                String line;

                // 讀取所有行並處理
                while ((line = br.readLine()) != null) {
                    // 跳過空行
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    // 處理每一行
                    processUserLine(line);
                }

                // 檢查是否成功讀取到用戶
                if (!userList.isEmpty()) {
                    return; // 成功讀取，退出方法
                }

            } catch (Exception e) {
                // 嘗試下一個編碼
            }
        }
    }

    private void processUserLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        try {
            String[] data = line.split(",");

            if (data.length >= 4) {
                String id = data[0].trim();
                String name = data[1].trim();
                String password = data[2].trim();
                String role = data[3].trim();

                // 跳過明顯是標題的行
                if (id.equalsIgnoreCase("id") || id.equalsIgnoreCase("學號") || id.contains("ID")) {
                    return;
                }

                if (role.equalsIgnoreCase("s")) {
                    userList.add(new Student(id, name, password));
                } else if (role.equalsIgnoreCase("h")) {
                    userList.add(new Organizer(id, name, password));
                }
            }
        } catch (Exception e) {
            // 靜默處理錯誤
        }
    }

    @FXML
    private void handleLogin() {
        String userId = studentIdField.getText().trim();
        String password = passwordField.getText().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.WARNING, "輸入錯誤", "請輸入學號和密碼。");
            passwordField.clear(); // 清除密碼欄位
            return;
        }

        for (User u : userList) {
            if (u.getId().equals(userId) && u.getPassword().equals(password)) {
                showAlert(AlertType.INFORMATION, "登入成功", "歡迎，" + u.getName() + "！");
                if (u instanceof Student) {
                    loadStudentMain((Student) u);
                } else if (u instanceof Organizer) {
                    loadOrganizerMain((Organizer) u);
                }
                return;
            }
        }

        showAlert(AlertType.ERROR, "登入失敗", "學號或密碼錯誤！");
        passwordField.clear(); // 登入失敗時清除密碼欄位
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // 主畫面切換，根據你的FXML配置
    private void loadStudentMain(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("學生主畫面.fxml"));
            Parent root = loader.load();
            StudentMainController controller = loader.getController();
            controller.setStudent(student);

            // 如果學生主畫面有活動瀏覽功能，也可以在這裡設定學生ID
            // 或者在學生主畫面的 Controller 中處理

            Stage stage = (Stage) studentIdField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadOrganizerMain(Organizer organizer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("主辦人主畫面.fxml"));
            Parent root = loader.load();
            HostMainController controller = loader.getController();
            controller.setOrganizer(organizer);
            Stage stage = (Stage) studentIdField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}