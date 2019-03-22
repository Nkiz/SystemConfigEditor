package cnc.msl.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cnc.msl.Main;
import cnc.msl.view.MainViewController;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;


public class ProgressEventHandler implements Runnable  {
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private boolean trace = false;
    private Controller controller;
    final ProgressBar progressBar = new ProgressBar(0);

    public ProgressEventHandler(Controller controller) {
        this.controller = controller;
    }


	@Override
	public void run() {
		try {
		  System.out.println("In ProgressEvenetHandler");
          this.trace = true;
          if(!Main.getIsRunning()) {
        	  System.out.println("Stop");
          }
          while (Main.getIsRunning()) {
//        	  System.out.println(controller.mainViewController.isLoading);
              if(controller.mainViewController.isLoading) {
            	  if(!controller.mainViewController.pi_load.isVisible()) {
            		  Platform.runLater(() -> {
//            			  controller.mainViewController.pi_load.setVisible(true);
            		  });
            	  }
              }else {
            	  if(controller.mainViewController.pi_load.isVisible()) {
//            		  Platform.runLater(() ->controller.mainViewController.pi_load.setVisible(false));
            	  }
              }
          }
	   }catch (Exception e) {
			System.out.println("Error in ProgressEvenetHandler");
	   }	
	}
}