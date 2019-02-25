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
	private MainViewController mainViewController;
	public Controller(MainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}
	public TreeItem<String> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
		TreeItem<String> root = new TreeItem<String>(directory.getName());
		TreeItem<String> tmp;
        for(File f : directory.listFiles()) {
            if(f.isDirectory()) { //Then we call the function recursively
            	tmp = getNodesForDirectory(f);
            	if(!tmp.getChildren().isEmpty()) {
            		root.getChildren().add(getNodesForDirectory(f));
            	}
            } 
        }
        for(File f : directory.listFiles()) {
            if(!f.isDirectory()) {
            	if(f.getName().contains(".conf")) {
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
		        		if(line[0].startsWith("\t#")) {
			        		line = lineString.split("#");
//		        			key = "#Comment#";
//			        		value = line[1].trim();
			        		key = "";
			        		value = "";
			        		comment = line[1].trim();
			        		//TODO: erst mal Comments raus
//			        		continue;
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
			e1.printStackTrace();
		}

        return new Pair<>(root, "end");
    }
	
	public void saveNodes(TreeTableView<String[]> allNode, File selectedDir, File selectedFile){
		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		TreeItem<String[]> root	    =  allNode.getRoot();
		String fileName				= selectedFile.getPath();
		String [] fileParts			= fileName.split(".conf");
		if (!fileName.contains("conf")) {
			return;
		}
	    try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + File.separator + fileParts[0] + "-yaml.conf"));
	        bw.write("---" + System.lineSeparator());
	        getNode(allNode.getRoot(), bw);
			bw.close();
	    } catch (IOException e) {
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
				System.out.println(node.getChildren().get(i).getValue()[0] + node.getChildren().get(i).getValue()[1] + node.getChildren().get(i).getValue()[2] );
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
		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		Object yamlValue			= new Object();
		Yaml yaml = new Yaml();
		InputStream targetStream = new FileInputStream(file);
		Map<String,Object> result = (Map<String,Object>)yaml.load(targetStream);
//		Reader reader = new FileReader(file);
//		Iterable<Event> events = yaml.parse(reader);
//	    for (Event evt : events) {
//	    	System.out.println("Typ: " + evt.getClass());
//	    	System.out.println(evt.getStartMark().get_snippet());
//	    	System.out.println("Line: " + evt.getStartMark().getLine());
////            if (evt instanceof MappingStartEvent) {
////                System.out.println("Test");
////            } else if (evt instanceof MappingEndEvent) {
////            	System.out.println("Test");
////            } else if (evt instanceof SequenceStartEvent) {
////            	System.out.println("Test");
////            } else if (evt instanceof SequenceEndEvent) {
////            	System.out.println("Test");
////            } else if (evt instanceof ScalarEvent) {
////            	System.out.println("Test");
////            } else if (evt instanceof AliasEvent) {
////            	System.out.println("Test");
////            }
//        }

		TreeItem<String[]> root 		= new TreeItem<>(new String[] {file.getName(), "", "", ""});
		
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
						}
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
						for (int i = 0; i < listValueLine.length; i++) {
							if(listValueLine[i].toString().trim().equals("null")) {
								line = new String[] {"#LINECOMMENT#", "#LINECOMMENT#"}; 
								comment = commentList.get(commentIndex);
							}else {
								line = listValueLine[i].split(":");
							}
							if(line.length == 2) {
								try {
									lineItem = new TreeItem<>(new String[] {line[0], line[1], comment, checkOverwrite(null, item.getValue()[0], line[0])});
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
						lineItem = new TreeItem<>(new String[] {line[0], line[1], comment, checkOverwrite(null, item.getValue()[0], line[0])});
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
		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
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
				path = "/" + newPath;
			}
			else if( path.contains(".conf")) {
				path = newPath;
			}
			else {
				path = newPath + "/" + path;
			}
		}
		return path;
	}
	
	public boolean convertAllFiles(File directory) throws IOException {
        for(File f : directory.listFiles()) {
            if(f.isDirectory()) { //Then we call the function recursively
            	convertAllFiles(f); 
            }else {
            	if(Files.lines(Paths.get(f.getPath())).toArray()[0].equals("---")) {
					mainViewController.tbl_elements.setRoot(getYamlNodesFromFile(f,0, null).getKey());
					saveNodesYaml(mainViewController.tbl_elements, f.getParentFile(), new File(f.getName()));
				}
				else {
					mainViewController.tbl_elements.setRoot(getNodesForElements(f,0, null).getKey());
					saveNodes(mainViewController.tbl_elements, f.getParentFile(), new File(f.getName()));
				}
            }
        }
		return true;
	}
	public String checkOverwrite(File file, String parent, String key) throws IOException {
		File tmpFile = file;
		Yaml yaml = new Yaml();
		Collection<Object> col = null;
		Map<String, String> linkedHashMap = new LinkedHashMap<String, String>();
		String tmp = "";
		if(tmpFile == null) {
			tmpFile = mainViewController.selectedWs;
//			return "X";
		}
		for(File f : tmpFile.listFiles()) {
            if(f.isDirectory()) { //Then we call the function recursively
            	if(checkOverwrite(f,parent, key) == "X") {
            		return "X";
            	} 
            }else {
            	if(mainViewController.selectedDir.equals(f.getParentFile()) || mainViewController.selectedDir.length() < f.getParentFile().length()) {
    				continue;
    			}
            	if(Files.lines(Paths.get(f.getPath())).toArray()[0].equals("---")) {
            		InputStream targetStream = new FileInputStream(f);
            		Map<String,Object> result = (Map<String,Object>)yaml.load(targetStream);
            		try (Stream<String> stream = Files.lines(Paths.get(f.getPath()))) {
    			        Object[] allLines = stream.toArray();
    			        for (int i=0; i < allLines.length; i++) {
    			        	if(parent.equals("") || parent == null) {
    			        		continue;
    			        	}
    			        	if(allLines[i].toString().contains(parent)) {
    			        		for (int j = i; j < allLines.length; j++) {
    			        			if(!allLines[j].toString().endsWith(":")) {
	    			        			tmp = allLines[j].toString().split(":")[0].replaceAll("- ", "").replaceAll(":", "").trim();
	    			        			if(allLines[j].toString().split(":")[0].replaceAll("- ", "").replaceAll(":", "").trim().equals(key.trim())) {
	    			        				return "X";
	    			        			}
    			        			}
								}
//    			        		System.out.println("found");
    			        	}
//    			        	System.out.println(allLines[i]);
    			        }
            		}catch (Exception e) {
						// TODO: handle exception
					}
//            		for (Map.Entry<String, Object> entry : result.entrySet())
//            		{
//            			for (int i = 0; i < Object[]entry.getValue(); i++) {
//							
//						}
//            			System.out.println(entry.getValue());
//            			System.out.println(entry.getKey());
//            		}
            		col = result.values();
//            		Pair<TreeItem<String[]>, String> list  = getYamlNodesFromFile(f,0, null);
					
//				}
//				else {
//					getNodesForElements(f,0, null);
				}
            }
        }
		return "";
	}
}
