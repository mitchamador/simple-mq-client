package gbas.testapp.fxmqsample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader();

        Parent root = loader.load(getClass().getResourceAsStream("/fxml/main.fxml"));

        stage.setTitle("JavaFX - JMS - MQ - Sample application");
        stage.setScene(new Scene(root));

        stage.getScene().getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());

        MainController controller = loader.getController();
        controller.setScene(stage.getScene());

        stage.show();


    }
}