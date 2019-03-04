package cnc.msl.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

//import com.amihaiemil.eoyaml.Yaml;

import org.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cnc.msl.model.YamlDynamicNode;
import cnc.msl.model.YamlDynamicRootNode;
import cnc.msl.model.YamlDynamicValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Pair;

import cnc.msl.view.MainViewController;

public class Controller {
	public MainViewController mainViewController;
	public Controller(MainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}
	public TreeItem<String> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
		TreeItem<String> root = new TreeItem<String>(directory.getName());
		TreeItem<String> tmp;
		File allFiles[] = directory.listFiles();
		Arrays.sort(allFiles);
        for(File f : allFiles) {
            if(f.isDirectory()) { //Then we call the function recursively
            	tmp = getNodesForDirectory(f);
            	if(!tmp.getChildren().isEmpty()) {
            		root.getChildren().add(getNodesForDirectory(f));
            	}
            } 
        }
        for(File f : allFiles) {
            if(!f.isDirectory()) {
            	if(f.getName().endsWith(".conf")) {
            		root.getChildren().add(new TreeItem<String>(f.getName()));
            	}
            }
        }
        return root;
    }
	
	public Pair<TreeItem<String[]>, String> getNodesForElements(File file, int index, TreeItem<String[]> node) { //Returns a TreeItem representation of the specified directory
		TreeItem<String[]> root 		= new TreeItem<>(new String[] {file.getName(), "", "", ""});
		String key 					= "";
		String value 				= "";
		String comment 				= "";
		String[] line				= null;
		TreeItem<String[]> lineItem 	= null;
		TreeItem<String[]> lineNode 	= null;
		TreeItem<String[]> lineNodeTmp 	= null;
		boolean isNode				= false;
		String lineString			= "";
		Pair<TreeItem<String[]>, String> pair = null;
		
		if(node != null) {
			lineNode = node;
		}
		
		try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
	        Object[] allLines = stream.toArray();
	        // read File
	        for (int i=index; i < allLines.length; i++) {
	        	Object object = allLines[i];
	        	lineString = object.toString();
	        	lineString.trim();
	        	if(lineString.equals("") || lineString.equals("\t")) {
//	        		++index;

	        		continue;
	        	}
	        	//end node
	        	if(lineString.contains("[!") && !lineString.contains("#")) { 
	        		return new Pair<>(lineNode, Integer.toString(i));
        		}
	        	//is Node
	        	else if (lineString.contains("[") && !lineString.contains("#")) {
	        		if(node == null) {
	        			lineNode = new TreeItem<>(new String[] {lineString, "", "", ""});
	        			pair = this.getNodesForElements(file, i+1, lineNode);
		        		lineNode = pair.getKey();
	        		}else {
	        			lineItem = new TreeItem<>(new String[] {lineString, "", "", ""});
	        			pair = this.getNodesForElements(file, i+1, lineItem);
	        			lineItem = pair.getKey();
			        	lineNode.getChildren().add(lineItem);
			        	root.getChildren().clear();
	        		}
	        		root.getChildren().add(lineNode);
	        		if(pair.getValue() != "end"){
	        			index = i = Integer.parseInt(pair.getValue());
	        		}
	        	}
	        	//is value
	        	else {
		        	try {
		        		line = lineString.split("=");
		        		if(line[0].replaceAll("/t", "").trim().startsWith("#")) {
			        		line = lineString.replaceAll("/t", "").trim().split("#");
//		        			key = "#Comment#";
//			        		value = line[1].trim();
			        		key = "";
			        		value = "";
			        		comment = line[1].trim();
			        		//TODO: erst mal Comments raus
//			        		continue;
		        		}else if(line[1].replaceAll("/t", "").trim().contains("#")){
		        			key = line[0].trim();
		        			line = line[1].replaceAll("/t", "").trim().split("#",2);
				        	value = line[0].trim();
				        	comment = line[1].replaceAll("#", "").trim();
		        		}else {
			        		key = line[0].trim();
				        	value = line[1].trim();
				        	comment = "";
		        		}
		        	}catch (Exception e) {
	        			key = object.toString().trim();
		        		value = "";
					}
		        	lineItem = new TreeItem<>(new String[] {key, value, comment, ""});
		        	if(lineNode == null) {
		        		continue;
		        	}
		        	lineNode.getChildren().add(lineItem);
	        	}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("error in getNodeForElements");
			e1.printStackTrace();
		}

        return new Pair<>(root, "end");
    }
	
	public void saveNodes(TreeTableView<String[]> allNode, File selectedDir, File selectedFile){
//		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		TreeItem<String[]> root	    =  allNode.getRoot();
		String fileName				= selectedFile.getPath();
		String [] fileParts			= fileName.split(".conf");
		if (!fileName.contains("conf")) {
			return;
		}
	    try {
//	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + File.separator + fileParts[0] + "-yaml.conf"));
	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + File.separator + fileName));
	        bw.write("---" + System.lineSeparator());
	        getNode(allNode.getRoot(), bw);
			bw.close();
	    } catch (IOException e) {
	    	System.out.println("error in saveNodes");
	        e.printStackTrace();
	    }
		return;
	}
	public String getNode(TreeItem<String[]> node, BufferedWriter bw) throws IOException{
		YamlDynamicValue<String> yamlClassValue = new YamlDynamicValue<String>();
		YamlDynamicNode<String> yamlClassNode = new YamlDynamicNode<String>();
		YamlDynamicRootNode<String> yamlClassRootNode = new YamlDynamicRootNode<String>();
		String tmpValue;
		if(node.getValue()[0].equals("") && !node.getValue()[0].equals("")) {
			return "  -" + System.lineSeparator();
		}
		if(node.getValue()[0].contains("[") || node.getValue()[0].contains(".conf")){
			for (int i = 0; i < node.getChildren().size(); i++) {
//				System.out.println(node.getChildren().get(i).getValue()[0] + node.getChildren().get(i).getValue()[1] + node.getChildren().get(i).getValue()[2] );
				if((node.getChildren().get(i).getValue()[0].contains("[") && node.getChildren().get(i).getValue()[0] !=null)) {
					if(node.getValue()[0].contains(".conf")) {
						yamlClassRootNode.addKey(node.getChildren().get(i).getValue()[0].replace("[", "").replace("]", ""));
						yamlClassRootNode.addValue(node.getChildren().get(i).getValue()[1]);
						yamlClassRootNode.addComment(node.getChildren().get(i).getValue()[2]);
						bw.write(yamlClassRootNode.toString());
					}else {
						yamlClassNode.addKey(node.getChildren().get(i).getValue()[0].replace("[", "").replace("]", "").replace("\t", ""));
						yamlClassNode.addValue(node.getChildren().get(i).getValue()[1]);
						yamlClassNode.addComment(node.getChildren().get(i).getValue()[2]);
						bw.write("  " + yamlClassNode.toString());
					}
				}
				tmpValue = getNode(node.getChildren().get(i),bw);
				if(!tmpValue.contains("null")) {
//					if(!node.getParent().getValue()[0].contains(".conf")) {
					if(node.getParent().getValue()[0].contains("[")) {
						bw.write("  ");
					}
					if(tmpValue.contains("- : #")) {
//						tmpValue = tmpValue.replaceAll("- : ", "- LINECOMMENT:/*");
						tmpValue = tmpValue.replaceAll("- : ", "- #LINECOMMENT# : #LINECOMMENT# ");
//						tmpValue = tmpValue.replaceAll("- : ", "");
					}
					bw.write(tmpValue);
				}
			}
			return yamlClassRootNode.toString();
		}else {
			yamlClassValue.addKey(node.getValue()[0].replace("[", "").replace("]", ""));
			yamlClassValue.addValue(node.getValue()[1]);
			yamlClassValue.addComment(node.getValue()[2]);
			return yamlClassValue.toString();
		}
		
	}
	
	public void saveNodesYaml(TreeTableView<String[]> allNode, File selectedDir, File selectedFile) throws FileNotFoundException{
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + File.separator + selectedFile));
	        bw.write("---" + System.lineSeparator());
	        this.getNodesFromYaml2(allNode.getRoot(), bw, 0);
			bw.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return ;
	}
	
	public Pair<TreeItem<String[]>, String> getYamlNodesFromFile(File file, int index, TreeItem<String[]> node) throws IOException {
		Pair<TreeItem<String[]>, String> pair = null;
		TreeItem<String[]> lineItem 	= null;
		TreeItem<String[]> lineNode 	= null;
//		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		Object yamlValue			= new Object();
		Yaml yaml = new Yaml();
		InputStream targetStream = new FileInputStream(file);
		Map<String,Object> result = (Map<String,Object>)yaml.load(targetStream);

		TreeItem<String[]> root 		= new TreeItem<>(new String[] {file.getAbsolutePath(), "", "", ""});
		
		try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
	        Object[] allLines = stream.toArray();
		}catch (Exception e) {
			// TODO: handle exception
		}
		
		for (Map.Entry<String, Object> entry : result.entrySet())
		{
			lineItem = getNodesFromYaml(entry.getKey(),entry.getValue(), file);
			root.getChildren().add(lineItem);
		}
		return new Pair<>(root, "end");
	}
	
	public TreeItem<String[]> getNodesFromYaml(String map_key, Object map_value, File file) throws IOException {
		Yaml yaml = new Yaml();
		TreeItem<String[]> item 		= new TreeItem<>(new String[] {map_key, "", "", ""});
		String key 					= "";
		String value 				= "";
		String[] line				= null;
		String[] listValueLine		= null;
		TreeItem<String[]> lineItem 	= null;
		ArrayList<Object> list;
		ArrayList<Object> listTmp = null;
		ArrayList<String> commentList = new ArrayList<String>();
		boolean isNode				= false;
		String lineString			= "";
		String lineStringTmp		= "";
		Pair<TreeItem<String[]>, String> pair = null;
		String comment				= "";
		int commentIndex			= 0;
		
		if(!map_value.equals(null)) {
			if(map_value.getClass().equals(java.util.ArrayList.class)) {
				comment = "";
				try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
			        Object[] allLines = stream.toArray();
			        for (int i=0; i < allLines.length; i++) {
//			        	index = i;
			        	Object object = allLines[i];
			        	lineString = object.toString();
			        	lineStringTmp = lineString.replaceAll("- ", "").replaceAll(":", "").trim();
			        	if(lineStringTmp.equals(map_key)) {
			        		commentIndex = 0;
			        		commentList.clear();
			        	}
			        	if(lineString.contains("- #LINECOMMENT#")) {
//			        		if(!lineString.split(":")[1].contains("#LINECOMMENT#")){
			        			commentList.add(lineString.split(":")[1].replaceAll("#LINECOMMENT#", "").replaceAll("#", "").trim());
//			        		}else {
//			        			commentList.add(lineString.split(":")[1]);
//			        		}
			        	}
			        	if(lineString.contains(map_key + ":")) {
			        		if(lineString.contains("#")) {
			        			item.setValue(new String[] {map_key, "", lineString.split("#")[1].toString()});
			        			break;
			        		}
			        	}
					}
				}catch (Exception e) {
					// TODO: handle exception
				}
				list = (ArrayList<Object>) map_value;
				for(Object listValue:list) {
					comment = "";
					if(listValue == null) {
						comment = commentList.get(commentIndex);
						++commentIndex;
						try {
							lineItem = new TreeItem<>(new String[] {"#LINECOMMENT#", "#LINECOMMENT#", comment, ""});
						}catch (Exception e) {
							// TODO: handle exception
							System.out.println("error comment");
						}
//					System.out.println("COMMENT: " + comment);
						item.getChildren().add(lineItem);
						continue;
					}
					try (Stream<String> stream = Files.lines(Paths.get(file.getPath()))) {
				        Object[] allLines = stream.toArray();
				        for (int i=0; i < allLines.length; i++) {
//				        	index = i;
				        	Object object = allLines[i];
				        	lineString = object.toString();
				        	if(lineString.contains(listValue.toString())) {
				        		if(lineString.contains("#")) {
				        			comment = lineString.split("#")[1];
				        			break;
				        		}
				        	}
						}
					}catch (Exception e) {
					}
					if(listValue.toString().startsWith("[")) {
						listValue = listValue.toString().replace("[", "").replace("]", "");
						listValueLine = listValue.toString().split(",");
						comment = "";
						for (int i = 0; i < listValueLine.length; i++) {
							if(listValueLine[i].toString().trim().equals("null")) {
								line = new String[] {"#LINECOMMENT#", "#LINECOMMENT#"}; 
								comment = commentList.get(commentIndex);
								commentIndex++;
							}else {
								line = listValueLine[i].split(":");
							}
							if(line.length == 2) {
								try {
//									lineItem = new TreeItem<>(new String[] {line[0], line[1], comment, checkOverwrite(null, item.getValue()[0], line[0])});
									lineItem = new TreeItem<>(new String[] {line[0], line[1], comment});
								} catch (Exception e) {
									// TODO: handle exception
								}
								item.getChildren().add(lineItem);
							}
						}
						continue;
					}
					if(listValue.toString().startsWith("{")) {
						listValueLine = listValue.toString().split("=");
						listValueLine[0] = listValueLine[0].replace("{", "");
						listValueLine[1] = listValueLine[1].replace("}", "");
						listTmp = new ArrayList<Object>();
						listTmp.add(listValueLine[1]);
						item.getChildren().add(getNodesFromYaml(listValueLine[0], listTmp, file));
						continue;
					}
					line = listValue.toString().split(":");
					if(line.length == 2) {
//						lineItem = new TreeItem<>(new String[] {line[0], line[1], comment, checkOverwrite(null, item.getValue()[0], line[0])});
						lineItem = new TreeItem<>(new String[] {line[0], line[1], comment});
						item.getChildren().add(lineItem);
					}else {
						item.getChildren().add(getNodesFromYaml(line[0],listValue, file));
					}
				}
			}
		}
		return item;
	}
	
	public Pair<TreeItem<String[]>, String> getNodesFromYaml2(TreeItem<String[]> node,  BufferedWriter bw, int index) throws IOException {
//		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		Object yamlValue			= new Object();
		Yaml yaml = new Yaml();
		YamlDynamicValue<String> yamlClassValue = new YamlDynamicValue<String>();
		YamlDynamicNode<String> yamlClassNode = new YamlDynamicNode<String>();
		YamlDynamicRootNode<String> yamlClassRootNode = new YamlDynamicRootNode<String>();
		String tmp = "";
		int localIndex = index;
		if(!node.getChildren().isEmpty()) {
			for (int i = 0; i < node.getChildren().size(); i++) {
				if(node.getChildren().get(i).getValue()[1].equals("")) {
					if(!node.getValue()[0].contains(".conf")) {
						yamlClassNode.addKey(node.getChildren().get(i).getValue()[0]);
						yamlClassNode.addValue("");
						yamlClassNode.addComment(node.getChildren().get(i).getValue()[2]);
						if(yamlClassNode.getKey().equals("")) {
							bw.write("  -");
						}else {
							bw.write("  " + yamlClassNode.toString());
						}
						if(localIndex == index) {
							++localIndex;
						}
					}else {
						yamlClassRootNode.addKey(node.getChildren().get(i).getValue()[0]);
						yamlClassRootNode.addValue("");
						yamlClassRootNode.addComment(node.getChildren().get(i).getValue()[2]);
						bw.write(yamlClassRootNode.toString());
					}
					getNodesFromYaml2(node.getChildren().get(i), bw, localIndex);
				}else {
					for (int j = 0; j < index; j++) {
						bw.write("  ");
					}
					if(node.getChildren().get(i).getValue()[0].equals("")) {
						bw.write("-");
					}else {
						yamlClassValue.addKey(node.getChildren().get(i).getValue()[0]);
						yamlClassValue.addValue(node.getChildren().get(i).getValue()[1]);
						yamlClassValue.addComment(node.getChildren().get(i).getValue()[2]);
						bw.write(yamlClassValue.toString());
					}
				}
			}
		}else {
			if(node.getValue()[0].equals("")) {
				bw.write("-");
			}else {
				yamlClassValue.addKey(node.getValue()[0]);
				yamlClassValue.addValue(node.getValue()[1]);
				yamlClassValue.addComment(node.getValue()[2]);
				bw.write(yamlClassValue.toString());
			}
		}
		return new Pair<>(node, "end");
	}
	
	public void loadList() {
		this.mainViewController.loadDirectoryList();
		return;
	}
	
	public String getPath(TreeItem<String> item, String path){
		TreeItem <String> parentItem = item.getParent();
		String newPath;
		if(parentItem != null) {
			newPath = getPath(parentItem, item.getValue());
			if(newPath.contains(".conf")) {
				return "";
			}
			if(path.equals("")) {
				path = File.separator + newPath;
			}
			else if( path.contains(".conf")) {
				path = newPath;
			}
			else {
				path = newPath + File.separator + path;
			}
		}
		return path;
	}
	
	public boolean convertAllFiles(File directory) throws IOException {
		TreeTableView<String[]> tbl_elements = new TreeTableView<String[]>();
        for(File f : directory.listFiles()) {
            if(f.isDirectory()) { //Then we call the function recursively
            	convertAllFiles(f); 
            }else {
            	if(!f.getName().endsWith(".conf")) {
            		continue;
            	}
            	if(Files.lines(Paths.get(f.getPath())).toArray()[0].equals("---")) {
//            		tbl_elements = new TreeTableView<String[]>();
//            		tbl_elements.setRoot(getYamlNodesFromFile(f,0, null).getKey());
//					saveNodesYaml(tbl_elements, f.getParentFile(), new File(f.getName()));
				}
				else {
					tbl_elements = new TreeTableView<String[]>();
					tbl_elements.setRoot(getNodesForElements(f,0, null).getKey());
					saveNodes(tbl_elements, f.getParentFile(), new File(f.getName()));
				}
            }
        }
		return true;
	}
	public boolean checkOverwrite(File file, String key) throws IOException {
		File tmpFile = file;
		Yaml yaml = new Yaml();
		Collection<Object> col = null;
		Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();
		String tmp = "";
		if(tmpFile == null) {
			tmpFile = mainViewController.selectedDir;
//			return "X";
		}
		for(File f : tmpFile.listFiles()) {
//			System.out.println("Path: " + f.getPath() + f);
            if(f.isDirectory()) { //Then we call the function recursively
//            	System.out.println(mainViewController.selectedFile);
//        		System.out.println("FOLDER: " + f.getPath());
            	if(checkOverwrite(f,key)) {
            		return true;
            	} 
            }else {
//            	System.out.println("FILE: " + f.getName());
            	if(!f.getName().equals(mainViewController.selectedFile.getName())) {
            		//System.out.println("Selected " + mainViewController.selectedFile + "=!" + f.getName());
            		continue;
            	}else {
//            		System.out.println("Find other " + f.getName());
            	}
//            	if(key.equals("MaxPWM")) {
//            		System.out.println(key);
//            	}
//            	System.out.println("Sel. Dir: " + mainViewController.selectedDir);
//            	System.out.println("Loop Dir: "+ f.getParentFile());
//            	if(mainViewController.selectedDir.length() >= f.getParentFile().length()) {
//    				continue;
//    			}
            	try {
            		if(Files.lines(Paths.get(f.getPath())).toArray()[0].equals("---")) {
                		InputStream targetStream = new FileInputStream(f);
                		Map<String,Object> result = (Map<String,Object>)yaml.load(targetStream);
                		try (Stream<String> stream = Files.lines(Paths.get(f.getPath()))) {
        			        Object[] allLines = stream.toArray();
        			        for (int i=0; i < allLines.length; i++) {
//        			        	if(allLines[i].toString().contains(parent)) {
//        			        		for (int j = i; j < allLines.length; j++) {
        			        	if(!allLines[i].toString().endsWith(":")) {
				        			tmp = allLines[i].toString().split(":")[0].replaceAll("- ", "").replaceAll(":", "").trim();
				        			if(allLines[i].toString().split(":")[0].replaceAll("- ", "").replaceAll(":", "").trim().equals(key.trim())) {
				        				if(!mainViewController.selectedDir.equals(f.getParentFile())) {
//				        					System.out.println("X: " + f.getPath());
				        					return true;
				        				}else {
//				        					System.out.println("SAME FILE");
				        				}
				        			}
			        			}
//    								}
//        			        	}
        			        }
                		}catch (Exception e) {
    						return false;
    					}
    				}
				} catch (Exception e) {
					return false;
				}
            }
        }
		return false;
	}
}
