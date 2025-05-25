import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/login.fxml"));
        // 設定場景大小
        Scene scene = new Scene(root, 800, 500);

        primaryStage.setTitle("登入系統");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);  // 最小寬度
        primaryStage.setMinHeight(500); // 最小高度
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
