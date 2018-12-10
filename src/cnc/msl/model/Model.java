package cnc.msl.model;

import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Model {
	private Stage mainStage;
    private AnchorPane mainLayout;
    
    public void setMainStage(Stage mainStage) {
    	this.mainStage = mainStage;
    }
    
    public Stage getMainStage() {
    	return this.mainStage;
    }
    
    public void setMainLayout(AnchorPane mainLayout) {
    	this.mainLayout = mainLayout;
    }
    
    public AnchorPane getMainLayout() {
    	return this.mainLayout;
    }
    
}
