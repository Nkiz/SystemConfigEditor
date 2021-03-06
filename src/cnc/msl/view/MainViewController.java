package cnc.msl.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cnc.msl.Main;
import cnc.msl.controller.Controller;
import cnc.msl.model.Value;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.converter.DefaultStringConverter;
import cnc.msl.controller.FileSystemEventHandler;
import cnc.msl.controller.ProgressEventHandler;

public class MainViewController {
	private Main mainApp;
	public File selectedWs = new File("");
	public File selectedDir = new File("");
	private File selectedNewDir = new File("");
//	private File selectedDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
//	private File selectedNewDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
	public File selectedFile;
	private Controller controller = new Controller(this);
	private ObservableList<String[]> items = FXCollections.observableArrayList();
	private FileSystemEventHandler fileSystemEventHandler = new FileSystemEventHandler(controller);
	private ProgressEventHandler progressEventHandler = new ProgressEventHandler(controller);
	public boolean isLoading = false;
	
	@FXML
	public TreeTableView<String> tbl_directory;
	@FXML
	public TreeTableView<String[]> tbl_elements;
	@FXML
	Button btn_del;
	@FXML
	Button btn_add;
	@FXML
	Button btn_addNode;
	@FXML
	Button btn_save;
	@FXML
	Button btn_cancel;
	@FXML
	Label lbl_changes;
	@FXML
	public Label lbl_load;
	@FXML
	ButtonBar btnbar;
	@FXML
	public AnchorPane ap_table;
	@FXML
	public VBox vbox_main;
	@FXML
	SplitPane sp_main;
	@FXML
	ImageView img_logo;
	@FXML
	public ProgressIndicator pi_load;
	
	@FXML
    private void initialize() {
		this.initElementList();
//		this.loadDirectoryList();
		this.initElementsList();
//		fileSystemEventHandler = new FileSystemEventHandler(controller);
		new Thread(fileSystemEventHandler).start();
        new Thread(progressEventHandler).start();
	}

	
	@FXML
	private void handleChooseWs(ActionEvent event) throws IOException {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Workspace");
		File defaultDirectory = selectedDir;
		try {
			if(!defaultDirectory.getPath().equals("")) {
				chooser.setInitialDirectory(defaultDirectory);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			this.selectedWs = chooser.showDialog(this.mainApp.getMainStage());
			this.selectedDir = this.selectedWs;
			//this.selectedNewDir = chooser.showDialog(this.mainApp.getMainStage());
			this.loadDirectoryList();
			fileSystemEventHandler.register(this.selectedWs.toPath());
		} catch (Exception e) {
			System.out.println("No WS selected");
		}
	}
	
	public void loadDirectoryList() {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				lbl_changes.setVisible(false);
				try {
					if(!selectedWs.getPath().equals("")) {
						tbl_directory.setRoot(controller.getNodesForDirectory(selectedWs));
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		});
	}
	
	public void initElementList() {
		//Creating a column
        TreeTableColumn<String,String> column = new TreeTableColumn<>("Configuration Files");
//        column.se
     
        //Defining cell content
        column.setCellValueFactory((CellDataFeatures<String, String> p) -> 
            new ReadOnlyStringWrapper(p.getValue().getValue()));  
        tbl_directory.getColumns().add(column);
        column.setSortable(false);
        tbl_directory.widthProperty().addListener((obs, oldVal, newVal) -> {
            column.setPrefWidth((double)newVal);
        });
	}
	
	public void initElementsList() {
		lbl_changes.setVisible(false);
		TreeItem<String> root = new TreeItem<>();
		//Creating a column
        TreeTableColumn<String[],String> elemColumn = new TreeTableColumn<>("Key");
        TreeTableColumn<String[],String> valueColumn = new TreeTableColumn<>("Value");
        TreeTableColumn<String[],String> commentColumn = new TreeTableColumn<>("Comment");
//        TreeTableColumn<String[],String> overwirteColumn = new TreeTableColumn<>("OV");
        
     
        //Defining cell content
        valueColumn.setCellValueFactory((CellDataFeatures<String[], String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue()[1]));  
        elemColumn.setCellValueFactory((CellDataFeatures<String[], String> p) -> 
        	new ReadOnlyStringWrapper(p.getValue().getValue()[0])); 
        commentColumn.setCellValueFactory((CellDataFeatures<String[], String> p) -> 
    		new ReadOnlyStringWrapper(p.getValue().getValue()[2])); 

        elemColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        commentColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

        
        valueColumn.setCellFactory(new Callback<TreeTableColumn<String[],String>, TreeTableCell<String[],String>>() {
            @Override
            public TextFieldTreeTableCell<String[], String> call(TreeTableColumn<String[], String> param) {
                return new TextFieldTreeTableCell<String[], String>(new DefaultStringConverter()) {

	                @Override
	                public void updateItem(String item, boolean empty) {
//	                	String tmpItem = item;
//	                	if(!item.isEmpty()) {
//	                		tmpItem = item.replace("(<X>)", "");
//	                	}
                    	if(!empty) {
	                        try {
	                        	super.updateItem(item.replace("(<X>)", ""), empty);
	                        	if(item.contains("(<X>)")){
	                        		item = item.replace("(<X>)", "");
	                        		setText(item);
	                        		BackgroundFill fill = new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY);
								    this.setBackground(new Background(fill));
	                            }else {
	                            	BackgroundFill fill = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
	                                this.setBackground(new Background(fill));
	                                item = item.replace("(<X>)", "");
	                        		setText(item);
	                            }
							} catch (Exception e) {
								BackgroundFill fill = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
	                            this.setBackground(new Background(fill));
	                            item = item.replace("(<X>)", "");
	                    		setText(item);
							}
                    	}else {
                    		super.updateItem(item, empty);
                    		BackgroundFill fill = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
                            this.setBackground(new Background(fill));
                    	}
                    }
                };
            }
        });
        
        valueColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	String[] newValue = t.getRowValue().getValue();
        	newValue[1] = t.getNewValue();
            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue);
            lbl_changes.setVisible(true);
        });
        
        elemColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	TreeItem<String[]> rowItem = new TreeItem<>();
        	rowItem = t.getRowValue();
        	if(rowItem.getValue()[0].contains(".conf")) {
        		( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(rowItem.getValue());
        	}else {
	        	String[] newValue2 = t.getRowValue().getValue();
	        	newValue2[0] = t.getNewValue();
	        	
	            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue2);
	            lbl_changes.setVisible(true);
        	}
        });
        
        commentColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	String[] newValue3 = t.getRowValue().getValue();
        	newValue3[2] = t.getNewValue();
            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue3);
            lbl_changes.setVisible(true);
        });

        tbl_elements.getColumns().add(elemColumn);
        tbl_elements.getColumns().add(valueColumn);
        tbl_elements.getColumns().add(commentColumn);
//        tbl_elements.getColumns().add(overwirteColumn);
        tbl_elements.setEditable(true);
        
        tbl_elements.widthProperty().addListener((obs, oldVal, newVal) -> {
        	elemColumn.setPrefWidth((double)newVal/3);
        	valueColumn.setPrefWidth((double)newVal/3);
        	commentColumn.setPrefWidth((double)newVal/3);
//        	commentColumn.setPrefWidth((double)newVal/4 + (double)newVal/24);
//        	overwirteColumn.setPrefWidth((double)newVal/24);
        });
        
        vbox_main.heightProperty().addListener((obs, oldVal, newVal) -> {
        	sp_main.setPrefHeight((double)newVal * 0.9);
        });
        vbox_main.widthProperty().addListener((obs, oldVal, newVal) -> {
        	
        	btnbar.setPrefWidth((double)newVal);
        	btnbar.setMaxWidth((double)newVal);
        	btnbar.setMinWidth((double)newVal);
        	if((double)newVal >=860) {
        		img_logo.translateXProperty().set(860 - (double)newVal);
        	}else {
        		img_logo.translateXProperty().set((double)0);
        	}
        });
	}
	
	@FXML
	private void handleSelectFile(MouseEvent event) throws IOException {
		Charset charset = Charset.defaultCharset();  
		if (event.getClickCount() == 2) {
			if(tbl_directory.getSelectionModel().getSelectedItem() != null) {
				this.isLoading = true;
				selectedNewDir = new File(selectedWs + controller.getPath(tbl_directory.getSelectionModel().getSelectedItem(), ""));
				this.selectedDir = selectedNewDir;
				this.selectedFile = new File(tbl_directory.getSelectionModel().getSelectedItem().getValue());
				if(this.selectedFile.getName().contains(".conf")) {
//					String test1 = selectedNewDir + File.separator + this.selectedFile;
//					Path test2 = Paths.get(test1);
//					Object [] test3 = Files.lines(test2, charset).toArray();
//					
					if(Files.lines(Paths.get(selectedNewDir + File.separator + this.selectedFile), charset).toArray()[0].equals("---")) {
						tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
					}
					else {
						tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
					}
					this.isLoading = false;
//					pi_load.setVisible(false);
	//				controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
				}
			}
		}
	}
	@FXML
	private void handleConvAllFiles(ActionEvent event) throws IOException {
		try {
			if(!selectedWs.toPath().toString().equals("")) {
				System.out.println("Convert All Files");
				this.isLoading = true;
				controller.convertAllFiles(selectedWs);
			}else {
				System.out.println("No WS selected!");
			}
		} catch (Exception e) {
			System.out.println("Error");
		}
		this.isLoading = false;
		return;
	}
	@FXML
	private void handleNewFile(ActionEvent event) throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Create new Config");
		ExtensionFilter extensionFilter = new ExtensionFilter("ConfigFile", "*.conf");
//		File newFile = fileChooser.showDialog(this.mainApp.getMainStage());
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		if(!this.selectedDir.getPath().equals("")) {
			fileChooser.setInitialDirectory(this.selectedDir);
		}
		lbl_changes.setVisible(true);
		File newFile = fileChooser.showSaveDialog(this.mainApp.getMainStage());
		newFile.createNewFile();
		this.selectedDir = newFile.getParentFile();
		tbl_directory.setRoot(controller.getNodesForDirectory(selectedDir));
		
		this.selectedFile = newFile;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(this.selectedFile));
		String nodeName = this.selectedFile.getName().replace(".conf", "");
        bw.write("---" + System.lineSeparator() + nodeName + ":" + System.lineSeparator() + "  - newLine:X");
		bw.close();
		
		tbl_elements.setRoot(controller.getYamlNodesFromFile(this.selectedFile,0, null).getKey());
		
		return;
	}
	
	@FXML
	private void handleDelFile(ActionEvent event) {
		File delFile = new File(this.selectedDir + File.separator + this.selectedFile);
		delFile.setWritable(true);
		//		delFile
		try {
			System.out.println(Files.isWritable(delFile.toPath()));
			Files.delete(delFile.toPath());
//			delFile.delete();
		}catch (Exception e) {
			System.out.println("Can't delete File!");
		}
		this.loadDirectoryList();
		System.out.println("DelFile");
		return;
	}
	
	@FXML
	private void addNode() throws FileNotFoundException {
		
		TreeItem<String[]> newNode = new TreeItem<>();
		TreeItem<String[]> newLine = new TreeItem<>();
		String[] newNodeValue = {"NewNode", "", ""};
		String[] newLineValue = {"newLine", "X", ""};
		newLine.setValue(newLineValue);
		newNode.getChildren().add(newLine);
		newNode.setValue(newNodeValue);
		try {
			int index = tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().indexOf(tbl_elements.getSelectionModel().getSelectedItem());
			tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().add(index+1,newNode);
			tbl_elements.refresh();
			lbl_changes.setVisible(true);
		} catch (Exception e) {
		}
	}
	
	@FXML
	private void addLine() throws FileNotFoundException {
		try {
			TreeItem<String[]> newLine = new TreeItem<>();
			String[] newValue = {"newLine", "X", ""};
			newLine.setValue(newValue);
			int index = tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().indexOf(tbl_elements.getSelectionModel().getSelectedItem());
			tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().add(index+1,newLine);
			tbl_elements.refresh();
			lbl_changes.setVisible(true);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@FXML
	private void delLine() throws FileNotFoundException {
		try {
			ObservableList selectedItems = tbl_elements.getSelectionModel().getSelectedItems();
			TreeItem<String[]> selectedItem = (TreeItem<String[]>) selectedItems.get(0);
			tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().remove(selectedItem);
			tbl_elements.refresh();
			lbl_changes.setVisible(true);
			lbl_changes.setVisible(true);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@FXML
	private void saveFile() throws FileNotFoundException, IOException {
		try {
			if(this.selectedFile.getName().contains(".conf")) {
				if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
					controller.saveNodesYaml(tbl_elements, selectedNewDir, selectedFile);
				}
				else {
					controller.saveNodes(tbl_elements, selectedNewDir, selectedFile);
				}
				lbl_changes.setVisible(false);
//				controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
			}
//			controller.saveNodes(tbl_elements, selectedNewDir, selectedFile);
			tbl_directory.setRoot(controller.getNodesForDirectory(selectedWs));

			if(this.selectedFile.getName().contains(".conf")) {
				if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
					tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
				}
				else {
					tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	@FXML
	private void cancelFile() throws FileNotFoundException, IOException {
		try {
			if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
				tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
			}
			else {
				tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
			}
			lbl_changes.setVisible(false);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@FXML
	private void handleReloadDirectory(ActionEvent event) throws IOException {
		this.loadDirectoryList();
	}
	
	public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
	}
	
//	public void showIndicator() {
//		Platform.runLater(new Runnable() {
//			@Override public void run() {
//				if(pi_load.isVisible()) {
//					pi_load.setVisible(false);
//				}else {
//					pi_load.setVisible(true);
//				}
//			}
//		});
//	}
}