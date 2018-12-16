package cnc.msl.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import org.yaml.snakeyaml.Yaml;

//import org.yaml.snakeyaml.*;
//import src.main.java.org.yaml.snakeyaml.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import cnc.msl.model.YamlDynamicNode;
import cnc.msl.model.YamlDynamicRootNode;
import cnc.msl.model.YamlDynamicValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.util.Pair;

public class Controller {
	public TreeItem<String> getNodesForDirectory(File directory) { //Returns a TreeItem representation of the specified directory
		TreeItem<String> root = new TreeItem<String>(directory.getName());
        for(File f : directory.listFiles()) {
            //System.out.println("Loading " + f.getName());
            if(f.isDirectory()) { //Then we call the function recursively
                root.getChildren().add(getNodesForDirectory(f));
            } else {
                root.getChildren().add(new TreeItem<String>(f.getName()));
            }
        }
        return root;
    }
	
	public Pair<TreeItem<String[]>, String> getNodesForElements(File file, int index, TreeItem<String[]> node) { //Returns a TreeItem representation of the specified directory
		TreeItem<String[]> root 		= new TreeItem<>(new String[] {file.getName(), ""});
		String key 					= "";
		String value 				= "";
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
//	        	index = i;
	        	Object object = allLines[i];
	        	lineString = object.toString();
	        	lineString.trim();
	        	if(lineString.equals("") || lineString.equals("\t")) {
//	        		++index;

	        		continue;
	        	}
	        	//end node
//	        	if(lineString.startsWith("[!") || lineString.startsWith("\t[!") || lineString.startsWith("\t\t[!") || lineString.startsWith("\t\t\t[!") || lineString.startsWith("\t\t\t\t[!")) { 
	        	if(lineString.contains("[!") && !lineString.contains("#")) { 
	        		return new Pair<>(lineNode, Integer.toString(i));
        		}
	        	//is Node
//	        	else if (lineString.startsWith("[") || lineString.startsWith("\t[") || lineString.startsWith("\t\t[") || lineString.startsWith("\t\t\t[") || lineString.startsWith("\t\t\t\t[")) {
	        	else if (lineString.contains("[") && !lineString.contains("#")) {
	        		if(node == null) {
	        			lineNode = new TreeItem<>(new String[] {lineString, ""});
	        			pair = this.getNodesForElements(file, i+1, lineNode);
		        		lineNode = pair.getKey();
	        		}else {
	        			lineItem = new TreeItem<>(new String[] {lineString, ""});
	        			pair = this.getNodesForElements(file, i+1, lineItem);
	        			lineItem = pair.getKey();
			        	lineNode.getChildren().add(lineItem);
			        	root.getChildren().clear();
	        		}
	        		root.getChildren().add(lineNode);
//	        		lineNodeTmp = lineNode;
//	        		lineNode = null;
	        		
//	        		for (TreeItem<String[]> nodeObject : lineNode.getChildren()) {
//	        			System.out.println("Node: " + nodeObject.getValue()[0]);
//					}
	        		if(pair.getValue() != "end"){
	        			index = i = Integer.parseInt(pair.getValue());
//	        			if(index < allLines.length) {
//	        				i++;
//	        				index++;
//	        			}
	        		}
//	        		root.getChildren().add(node);
//	        		for (TreeItem<String[]> nodeObject : root.getChildren()) {
//	        			System.out.println("RootNode: " + nodeObject.getValue()[0]);
//					}
	        	}
	        	//is value
	        	else {
		        	try {
		        		line = lineString.split("=");
		        		if(line[0].startsWith("\t#")) {
			        		line = lineString.split("#");
		        			key = "#Comment#";
			        		value = line[1].trim();
			        		//TODO: erst mal Comments raus
			        		continue;
		        		}else {
			        		key = line[0].trim();
				        	value = line[1].trim();
		        		}
		        	}catch (Exception e) {
	        			key = object.toString().trim();
		        		value = "";
					}
//		        	lineItem = new TreeItem<String>(key + "-" + value);
		        	lineItem = new TreeItem<>(new String[] {key, value});
		        	if(lineNode == null) {
		        		continue;
//		        		lineNode = new TreeItem<>(new String[] {"ROOT", ""});
//		        		root.getChildren().add(lineNode);
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
		File newFile = new File(fileParts[0] + "-yaml.conf");
		if (!newFile.exists()) {
	        try {
	        	newFile.createNewFile();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + "\\" + newFile.getName()));
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
//		if(node.getValue()[0].startsWith("[") || node.getValue()[0].startsWith("\t[") || node.getValue()[0].startsWith("\t\t[") || node.getValue()[0].startsWith("\t\t\t[") || node.getValue()[0].startsWith("\t\t\t\t[") || node.getValue()[0].contains(".conf")){
		if(node.getValue()[0].equals("")) {
			return "  -" + System.lineSeparator();
		}
		if(node.getValue()[0].contains("[") || node.getValue()[0].contains(".conf")){
			for (int i = 0; i < node.getChildren().size(); i++) {
				if(node.getChildren().get(i).getValue()[0].contains("[") && node.getChildren().get(i).getValue()[0] !=null) {
					if(node.getValue()[0].contains(".conf")) {
						yamlClassRootNode.addKey(node.getChildren().get(i).getValue()[0].replace("[", "").replace("]", ""));
						yamlClassRootNode.addValue(node.getChildren().get(i).getValue()[1]);
						bw.write(yamlClassRootNode.toString());
					}else {
						yamlClassNode.addKey(node.getChildren().get(i).getValue()[0].replace("[", "").replace("]", "").replace("\t", ""));
						yamlClassNode.addValue(node.getChildren().get(i).getValue()[1]);
						bw.write("  " + yamlClassNode.toString());
					}
				}
				tmpValue = getNode(node.getChildren().get(i),bw);
				if(!tmpValue.contains("null")) {
//					if(!node.getParent().getValue()[0].contains(".conf")) {
					if(node.getParent().getValue()[0].contains("[")) {
						bw.write("  ");
					}
					bw.write(tmpValue);
				}
			}
			return yamlClassRootNode.toString();
		}else {
			yamlClassValue.addKey(node.getValue()[0].replace("[", "").replace("]", ""));
			yamlClassValue.addValue(node.getValue()[1]);
			return yamlClassValue.toString();
		}
		
	}
	
	public void saveNodesYaml(TreeTableView<String[]> allNode, File selectedDir, File selectedFile) throws FileNotFoundException{
		try {
	        BufferedWriter bw = new BufferedWriter(new FileWriter(selectedDir + "\\" + selectedFile));
	        bw.write("---" + System.lineSeparator());
	        this.getNodesFromYaml2(allNode.getRoot(), bw, 0);
			bw.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
		return ;
	}
	
	public Pair<TreeItem<String[]>, String> getYamlNodesFromFile(File file, int index, TreeItem<String[]> node) throws FileNotFoundException {
		Pair<TreeItem<String[]>, String> pair = null;
		TreeItem<String[]> lineItem 	= null;
		TreeItem<String[]> lineNode 	= null;
		ObjectMapper mapper 		= new ObjectMapper(new YAMLFactory());
		Object yamlValue			= new Object();
		Yaml yaml = new Yaml();
		InputStream targetStream = new FileInputStream(file);
		Map<String,Object> result = (Map<String,Object>)yaml.load(targetStream);
		//System.out.println(result.toString());
		TreeItem<String[]> root 		= new TreeItem<>(new String[] {file.getName(), ""});
		for (Map.Entry<String, Object> entry : result.entrySet())
		{
			lineItem = getNodesFromYaml(entry.getKey(),entry.getValue());
			root.getChildren().add(lineItem);
//		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		return new Pair<>(root, "end");
	}
	
	public TreeItem<String[]> getNodesFromYaml(String map_key, Object map_value) throws FileNotFoundException {
		Yaml yaml = new Yaml();
		TreeItem<String[]> item 		= new TreeItem<>(new String[] {map_key, ""});
		String key 					= "";
		String value 				= "";
		String[] line				= null;
		String[] listValueLine		= null;
		TreeItem<String[]> lineItem 	= null;
		ArrayList<Object> list;
		ArrayList<Object> listTmp = null;
		boolean isNode				= false;
		String lineString			= "";
		Pair<TreeItem<String[]>, String> pair = null;
		
		//System.out.println(map_value.getClass());
		
		
		if(!map_value.equals(null)) {
//			System.out.println(map_value.getClass());
			if(map_value.getClass().equals(java.util.ArrayList.class)) {
				list = (ArrayList<Object>) map_value;
				for(Object listValue:list) {
					if(listValue == null) {
						lineItem = new TreeItem<>(new String[] {"", ""});
						item.getChildren().add(lineItem);
						continue;
					}
//					System.out.println(listValue.toString());
					if(listValue.toString().startsWith("[")) {
						listValue = listValue.toString().replace("[", "").replace("]", "");
						listValueLine = listValue.toString().split(",");
						for (int i = 0; i < listValueLine.length; i++) {
							line = listValueLine[i].split(":");
							if(line.length == 2) {
								lineItem = new TreeItem<>(new String[] {line[0], line[1]});
								item.getChildren().add(lineItem);
							}
						}
						continue;
					}
					if(listValue.toString().startsWith("{")) {
						listValueLine = listValue.toString().split("=");
						listValueLine[0] = listValueLine[0].replace("{", "");
						listValueLine[1] = listValueLine[1].replace("}", "");
//						System.out.println("Neue Node gefunden: " + listValueLine[0] + "-->" + listValueLine[1]);
////						InputStream targetStream = new FileInputStream(listValueLine[1].toString());
//						@SuppressWarnings("unchecked")
//						Map<String,Object> result = (Map<String,Object>)yaml.load(listValueLine[1].trim());
						listTmp = new ArrayList<Object>();
						listTmp.add(listValueLine[1]);
						item.getChildren().add(getNodesFromYaml(listValueLine[0], listTmp));
						continue;
					}
//					if(listValue.toString())
					line = listValue.toString().split(":");
					if(line.length == 2) {
						lineItem = new TreeItem<>(new String[] {line[0], line[1]});
						item.getChildren().add(lineItem);
					}else {
						item.getChildren().add(getNodesFromYaml(line[0],listValue));
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
				bw.write(yamlClassValue.toString());
			}
		}
		return new Pair<>(node, "end");
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
}
