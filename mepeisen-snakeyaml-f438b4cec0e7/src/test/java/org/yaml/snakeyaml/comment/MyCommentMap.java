package org.yaml.snakeyaml.comment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sample class for saving comments.
 */
final class MyCommentMap extends LinkedHashMap<Object, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961966490748341626L;

	public String[] mapLevelComments;

	public final Map<Object, String[]> valueComments = new HashMap<Object, String[]>();

}