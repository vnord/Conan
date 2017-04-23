package start;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
 
public class Main extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sketch.fxml"));
        Scene scene = new Scene(root, 1200, 600);
        scene.getStylesheets().add("minimalistStyle.css");
        stage.setTitle("Conan");
        stage.setScene(scene);
        stage.show();
    }
 public static void main(String[] args) {
        launch(args);
    }
}
