package cnc.msl;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import cnc.msl.view.MainViewController;

public class Main extends Application {
	
	private Stage mainStage;
    private AnchorPane mainLayout;

	@Override
	public void start(Stage mainStage) {
		this.mainStage = mainStage;
        this.mainStage.setTitle("ConfigApp");
        showMainView();
	}
	
	public void showMainView() {
        try {
            // Load mainView
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("view/MainView.fxml"));
            AnchorPane mainView = (AnchorPane) loader.load();
            Scene scene = new Scene(mainView);
            
            MainViewController controller = loader.getController();
            controller.setMainApp(this);
            
            mainStage.setScene(scene);
            mainStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public Stage getMainStage() {
        return mainStage;
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
