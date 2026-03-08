package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.StageStyle;


public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
        Scene scene = new Scene(root);
        Image icon;
        stage.initStyle(StageStyle.UNDECORATED);
        icon = new Image("application/Image.png");
        stage.getIcons().add(icon);
        stage.setTitle("Xpert");
        stage.setScene(scene);
        stage.show();
    }
}
