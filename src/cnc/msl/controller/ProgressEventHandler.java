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

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;


public class ProgressEventHandler implements Runnable  {
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private boolean trace = false;
    private Controller controller;

    public ProgressEventHandler(Controller controller) {
        this.controller = controller;
    }


	@Override
	public void run() {
		try {
		  System.out.println("In ProgressEvenetHandler");
          this.trace = true;
          while (Main.getIsRunning()) {
//        	  System.out.println(controller.mainViewController.isLoading);
              if(controller.mainViewController.isLoading) {
            	  controller.mainViewController.pi_load.setVisible(true);
              }else {
            	  controller.mainViewController.pi_load.setVisible(false);
              }
          }
	   }catch (Exception e) {
			System.out.println("Error in ProgressEvenetHandler");
	   }	
	}
}