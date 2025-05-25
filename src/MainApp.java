import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args); // JavaFX 的啟動點
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("JavaFX 設定成功 🎉");

        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("測試 JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
