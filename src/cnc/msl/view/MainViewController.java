package cnc.msl.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cnc.msl.Main;
import cnc.msl.controller.Controller;
import cnc.msl.model.Value;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;

import cnc.msl.controller.FileSystemEventHandler;

public class MainViewController {
	private Main mainApp;
	private File selectedDir = new File("");
	private File selectedNewDir = new File("");
//	private File selectedDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
//	private File selectedNewDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
	private File selectedFile;
	private Controller controller = new Controller(this);
	private ObservableList<String[]> items = FXCollections.observableArrayList();
	private FileSystemEventHandler fileSystemEventHandler = new FileSystemEventHandler(controller);
	
	@FXML
	TreeTableView<String> tbl_directory;
	@FXML
	TreeTableView<String[]> tbl_elements;
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
    private void initialize() {
		this.initElementList();
//		this.loadDirectoryList();
		this.initElementsList();
//		fileSystemEventHandler = new FileSystemEventHandler(controller);
        new Thread(fileSystemEventHandler).start();
	}
	
	@FXML
	private void handleChooseWs(ActionEvent event) throws IOException {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Workspace");
		File defaultDirectory = selectedDir;
		if(!defaultDirectory.getPath().equals("")) {
			chooser.setInitialDirectory(defaultDirectory);
		}
		this.selectedDir = chooser.showDialog(this.mainApp.getMainStage());
		//this.selectedNewDir = chooser.showDialog(this.mainApp.getMainStage());
		this.loadDirectoryList();
//		System.out.println(this.selectedDir);
		fileSystemEventHandler.register(this.selectedDir.toPath());
	}
	
	public void loadDirectoryList() {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				lbl_changes.setVisible(false);
				if(!selectedDir.getPath().equals("")) {
					tbl_directory.setRoot(controller.getNodesForDirectory(selectedDir));
				}
				
			}
		});
	}
	
	public void initElementList() {
		//Creating a column
        TreeTableColumn<String,String> column = new TreeTableColumn<>("Configuration Files");
        column.setPrefWidth(tbl_directory.getPrefWidth());
     
        //Defining cell content
        column.setCellValueFactory((CellDataFeatures<String, String> p) -> 
            new ReadOnlyStringWrapper(p.getValue().getValue()));  
        tbl_directory.getColumns().add(column);
	}
	
	public void initElementsList() {
		lbl_changes.setVisible(false);
		TreeItem<String> root = new TreeItem<>();
		//Creating a column
        TreeTableColumn<String[],String> elemColumn = new TreeTableColumn<>("Key");
        elemColumn.setPrefWidth(tbl_elements.getPrefWidth()/2);
        TreeTableColumn<String[],String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setPrefWidth(tbl_elements.getPrefWidth()/2);
     
        //Defining cell content
        valueColumn.setCellValueFactory((CellDataFeatures<String[], String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue()[1]));  
        elemColumn.setCellValueFactory((CellDataFeatures<String[], String> p) -> 
        	new ReadOnlyStringWrapper(p.getValue().getValue()[0])); 
        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        elemColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        
        valueColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	TreeItem<String[]> selItem = new TreeItem<>();
        	TreeItem<String[]> rowItem = new TreeItem<>();
        	rowItem = t.getRowValue();
        	String[] newValue = t.getRowValue().getValue();
        	newValue[1] = t.getNewValue();
            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue);
            lbl_changes.setVisible(true);
        });
        
        elemColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	TreeItem<String[]> selItem = new TreeItem<>();
        	TreeItem<String[]> rowItem = new TreeItem<>();
        	rowItem = t.getRowValue();
        	String[] newValue2 = t.getRowValue().getValue();
        	newValue2[0] = t.getNewValue();
            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue2);
        });

        tbl_elements.getColumns().add(elemColumn);
        tbl_elements.getColumns().add(valueColumn);
        tbl_elements.setEditable(true);
	}
	
	@FXML
	private void handleSelectFile(MouseEvent event) throws IOException {
		if (event.getClickCount() == 2) {
			selectedNewDir = new File(selectedDir + controller.getPath(tbl_directory.getSelectionModel().getSelectedItem(), ""));
			this.selectedFile = new File(tbl_directory.getSelectionModel().getSelectedItem().getValue());
			if(this.selectedFile.getName().contains(".conf")) {
				if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
					tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
				}
				else {
					tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
				}
				lbl_changes.setVisible(false);
//				controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
			}
		}
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
		
//		System.out.println(newFile.getAbsolutePath());
		return;
	}
	
	@FXML
	private void handleDelFile(ActionEvent event) {
		File delFile = new File(this.selectedDir + "\\" + this.selectedFile);
		File delFile2 = new File("C:/Users/nkiz_x240/Desktop/SystemConfig/cnc-msl-master/etc/Alica-yaml.conf");
//		delFile
		System.out.println(delFile2.getPath());
		System.out.println(delFile2.delete());
		this.loadDirectoryList();
		System.out.println("DelFile");
		return;
	}
	
	@FXML
	private void addNode() throws FileNotFoundException {
		lbl_changes.setVisible(true);
		TreeItem<String[]> newNode = new TreeItem<>();
		TreeItem<String[]> newLine = new TreeItem<>();
		String[] newNodeValue = {"NewNode", ""};
		String[] newLineValue = {"newLine", "X"};
		newLine.setValue(newLineValue);
		newNode.getChildren().add(newLine);
		newNode.setValue(newNodeValue);
//		int index = tbl_elements.getSelectionModel().getSelectedIndex();
		int index = tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().indexOf(tbl_elements.getSelectionModel().getSelectedItem());
//		System.out.println(index);
		tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().add(index+1,newNode);
		tbl_elements.refresh();
	}
	
	@FXML
	private void addLine() throws FileNotFoundException {
		lbl_changes.setVisible(true);
		TreeItem<String[]> newLine = new TreeItem<>();
		String[] newValue = {"newLine", "X"};
		newLine.setValue(newValue);
//		int index = tbl_elements.getSelectionModel().getSelectedIndex();
		int index = tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().indexOf(tbl_elements.getSelectionModel().getSelectedItem());
//		System.out.println(index);
		tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().add(index+1,newLine);
		tbl_elements.refresh();
	}
	
	@FXML
	private void delLine() throws FileNotFoundException {
		lbl_changes.setVisible(true);
		ObservableList selectedItems = tbl_elements.getSelectionModel().getSelectedItems();
		TreeItem<String[]> selectedItem = (TreeItem<String[]>) selectedItems.get(0);
		tbl_elements.getSelectionModel().getSelectedItem().getParent().getChildren().remove(selectedItem);
		tbl_elements.refresh();
		lbl_changes.setVisible(true);
	}
	
	@FXML
	private void saveFile() throws FileNotFoundException, IOException {
		if(this.selectedFile.getName().contains(".conf")) {
			if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
				controller.saveNodesYaml(tbl_elements, selectedNewDir, selectedFile);
			}
			else {
				controller.saveNodes(tbl_elements, selectedNewDir, selectedFile);
			}
			lbl_changes.setVisible(false);
//			controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
		}
//		controller.saveNodes(tbl_elements, selectedNewDir, selectedFile);
		tbl_directory.setRoot(controller.getNodesForDirectory(selectedDir));

		if(this.selectedFile.getName().contains(".conf")) {
			if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
				tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
			}
			else {
				tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
			}
//			controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
		}
		
	}
	
	@FXML
	private void cancelFile() throws FileNotFoundException, IOException {
		if(Files.lines(Paths.get(this.selectedDir + "/" + this.selectedFile)).toArray()[0].equals("---")) {
			tbl_elements.setRoot(controller.getYamlNodesFromFile(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
		}
		else {
			tbl_elements.setRoot(controller.getNodesForElements(new File(this.selectedNewDir + "/" + this.selectedFile),0, null).getKey());
		}
		lbl_changes.setVisible(false);
	}
	
	@FXML
	private void handleReloadDirectory(ActionEvent event) throws IOException {
		this.loadDirectoryList();
	}
	
	public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
	}
}