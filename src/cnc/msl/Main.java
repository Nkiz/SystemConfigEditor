package cnc.msl;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import cnc.msl.view.MainViewController;

public class Main extends Application {
	
	private Stage mainStage;
    private AnchorPane mainLayout;
    private static boolean isRunning = false;

	@Override
	public void start(Stage mainStage) {
		this.mainStage = mainStage;
        this.mainStage.setTitle("ConfigApp");
        isRunning = true;
	
        showMainView();
        mainStage.setOnCloseRequest(e -> Platform.exit());
        
	}
	
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		isRunning = false;
		super.stop();
		System.exit(0);
	}
	
	public static boolean getIsRunning(){
		return isRunning;
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
            
            mainStage.getIcons().add(new Image("cnc/msl/images/vs-color.png"));
            mainStage.setTitle("SystemConfig Editor");
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
