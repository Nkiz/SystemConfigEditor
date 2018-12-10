package org.yaml.snakeyaml.comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

final class MyRepresenter extends Representer {

	public MyRepresenter() {
		this.representers.put(MyCommentMap.class, new RepresentCommentMap());
	}

	private class RepresentCommentMap implements Represent {
		public Node representData(Object data) {
			MyCommentMap map = (MyCommentMap) data;
			final MappingNode mappingNode = (MappingNode) representMapping(Tag.MAP, map, Boolean.FALSE);
			if (map.mapLevelComments != null) {
				mappingNode.setPreComments(Arrays.asList(map.mapLevelComments));
			}
			return mappingNode;
		}
	}

	protected Node representMapping(Tag tag, Map<?, ?> mapping, Boolean flowStyle) {
		List<NodeTuple> value = new ArrayList<NodeTuple>(mapping.size());
		MappingNode node = new MappingNode(tag, value, flowStyle);
		representedObjects.put(objectToRepresent, node);
		boolean bestStyle = true;
		for (Map.Entry<?, ?> entry : mapping.entrySet()) {
			Node nodeKey = representData(entry.getKey());
			Node nodeValue = representData(entry.getValue());
			if (!(nodeKey instanceof ScalarNode && ((ScalarNode) nodeKey).getStyle() == null)) {
				bestStyle = false;
			}
			if (!(nodeValue instanceof ScalarNode && ((ScalarNode) nodeValue).getStyle() == null)) {
				bestStyle = false;
			}
			value.add(new NodeTuple(nodeKey, nodeValue));

			if (mapping instanceof MyCommentMap) {
				final String[] comment = ((MyCommentMap) mapping).valueComments.get(entry.getKey());
				if (comment != null) {
					nodeKey.setPreComments(Arrays.asList(comment));
				}
			}
		}
		if (flowStyle == null) {
			if (defaultFlowStyle != FlowStyle.AUTO) {
				node.setFlowStyle(defaultFlowStyle.getStyleBoolean());
			} else {
				node.setFlowStyle(bestStyle);
			}
		}
		return node;
	}

}