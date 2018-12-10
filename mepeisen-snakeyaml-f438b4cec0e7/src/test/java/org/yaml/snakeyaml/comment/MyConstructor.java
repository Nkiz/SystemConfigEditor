package org.yaml.snakeyaml.comment;

import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;

final class MyConstructor extends Constructor {

	/**
	 * Constructor
	 */
	public MyConstructor() {
		super();
		this.yamlConstructors.put(Tag.MAP, new MyMapConstruct());
	}

	@Override
	protected MyCommentMap createDefaultMap() {
		return new MyCommentMap();
	}

	protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
		super.constructMapping2ndStep(node, mapping);

		List<NodeTuple> nodeValue = node.getValue();
		for (NodeTuple tuple : nodeValue) {
			Node keyNode = tuple.getKeyNode();
			Object key = constructObject(keyNode);
			if (keyNode.getPreComments() != null && !keyNode.getPreComments().isEmpty()) {
				if (keyNode.isTwoStepsConstruction()) {
					throw new YAMLException("Comments on 2steps-construction not yet supported.");
				}
				((MyCommentMap) mapping).valueComments.put(key,
						keyNode.getPreComments().toArray(new String[keyNode.getPreComments().size()]));
			}
		}
	}

	public class MyMapConstruct implements Construct {
		public Object construct(Node node) {
			MyCommentMap result = null;
			if (node.isTwoStepsConstruction()) {
				result = createDefaultMap();
			} else {
				result = (MyCommentMap) constructMapping((MappingNode) node);
			}
			if (node.getPreComments() != null && node.getPreComments().size() > 0) {
				result.mapLevelComments = node.getPreComments().toArray(new String[node.getPreComments().size()]);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		public void construct2ndStep(Node node, Object object) {
			if (node.isTwoStepsConstruction()) {
				constructMapping2ndStep((MappingNode) node, (Map<Object, Object>) object);
			} else {
				throw new YAMLException("Unexpected recursive mapping structure. Node: " + node);
			}
		}
	}

}