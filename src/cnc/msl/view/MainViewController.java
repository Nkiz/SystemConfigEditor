package cnc.msl.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cnc.msl.Main;
import cnc.msl.controller.Controller;
import cnc.msl.model.Value;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Pair;

public class MainViewController {
	private Main mainApp;
	private File selectedDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
	private File selectedNewDir = new File("C:\\Users\\nkiz_x240\\Desktop\\SystemConfig\\cnc-msl-master\\etc");
	private File selectedFile;
	private Controller controller = new Controller();
	private ObservableList<String[]> items = FXCollections.observableArrayList();
	
	@FXML
	TreeTableView<String> tbl_directory;
	@FXML
	TreeTableView<String[]> tbl_elements;
	@FXML
	Button btn_del;
	@FXML
	Button btn_add;
	@FXML
	Button btn_save;
	@FXML
	Button btn_cancel;
	
	@FXML
    private void initialize() {
		this.loadDirectoryList();
		this.initElementsList();
	}
	@FXML
	private void handleChooseWs(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Workspace");
		File defaultDirectory = selectedDir;
		chooser.setInitialDirectory(defaultDirectory);
		this.selectedDir = chooser.showDialog(this.mainApp.getMainStage());
		this.selectedNewDir = chooser.showDialog(this.mainApp.getMainStage());
	}
	
	public void loadDirectoryList() {
		tbl_directory.setRoot(controller.getNodesForDirectory(selectedDir));
		//Creating a column
        TreeTableColumn<String,String> column = new TreeTableColumn<>();
        column.setPrefWidth(tbl_directory.getPrefWidth());
     
        //Defining cell content
        column.setCellValueFactory((CellDataFeatures<String, String> p) -> 
            new ReadOnlyStringWrapper(p.getValue().getValue()));  
        tbl_directory.getColumns().add(column);
	}
	
	public void initElementsList() {
		TreeItem<String> root = new TreeItem<>();
		//Creating a column
        TreeTableColumn<String[],String> elemColumn = new TreeTableColumn<>();
        elemColumn.setPrefWidth(tbl_elements.getPrefWidth()/2);
        TreeTableColumn<String[],String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setPrefWidth(tbl_elements.getPrefWidth()/2);
     
        //Defining cell content
        valueColumn.setCellValueFactory((CellDataFeatures<String[], String> p) ->
            new ReadOnlyStringWrapper(p.getValue().getValue()[1]));  
        elemColumn.setCellValueFactory((CellDataFeatures<String[], String> p) -> 
        	new ReadOnlyStringWrapper(p.getValue().getValue()[0])); 
        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        
        valueColumn.setOnEditCommit((CellEditEvent<String[], String> t) -> {
        	TreeItem<String[]> selItem = new TreeItem<>();
        	TreeItem<String[]> rowItem = new TreeItem<>();
        	rowItem = t.getRowValue();
        	String[] newValue = t.getRowValue().getValue();
        	newValue[1] = t.getNewValue();
            ( t.getTreeTableView().getTreeItem(t.getTreeTablePosition().getRow())).setValue(newValue);
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
//				controller.getNodesForElementsFromYaml(new File(this.selectedDir + "/" + this.selectedFile),0, null);
			}
		}
	}
	@FXML
	private void addLine() throws FileNotFoundException {
//		controller.getNodesFromYaml2(new File(this.selectedNewDir + "/" + this.selectedFile),0, null, null);
		System.out.println("ADD LINE");
	}
	
	@FXML
	private void delLine() {
//		System.out.println("DEL LINE");
		ObservableList selectedItems = tbl_elements.getSelectionModel().getSelectedItems();
		TreeItem<String[]> selectedItem = (TreeItem<String[]>) selectedItems.get(0);
		int selectedIndex = tbl_elements.getSelectionModel().getSelectedIndex();
		System.out.println(selectedIndex);
		int findIndex = tbl_elements.getRoot().getChildren().get(0).getChildren().indexOf(selectedItem);
//		String[] value = (String[]) selectedItem.getValue();
//		tbl_elements.getColumns().get(1).getColumns().remove(selectedIndex);
		System.out.println("Find index: " + findIndex);
//		System.out.println(tbl_elements.getRoot().getChildren().get(0).getChildren().remove(selectedIndex));
//		System.out.println(value[0]);
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
	private void cancelFile() {
		System.out.println("CANCEL FILE");
	}
	
	public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
	}
}