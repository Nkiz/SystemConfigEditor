/**
 * Copyright (c) 2008, http://www.snakeyaml.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yaml.snakeyaml.comment;

import static org.junit.Assert.assertArrayEquals;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import junit.framework.TestCase;

public class CommentOnMapEntriesNonIntendedTest extends TestCase {

	/** the yaml string to test. */
	private static final String YAML_STRING =
			"# this is a top level comment.\n"
			+ "foo:\n"
			+ "# some nested comment\n"
			+ "  bar: 123\n"
			+ "# second nested comment\n"
			+ "  baz: 234\n";

	/**
	 * Tests the parsing with skipped comments
	 */
	@SuppressWarnings("unchecked")
	public static void testWithSkippedComments() {
		final Yaml yml = new Yaml();
		final Object obj = yml.load(YAML_STRING);
		assertTrue(obj instanceof Map);
		final Object foo = ((Map<Object, Object>) obj).get("foo");
		assertTrue(foo instanceof Map);
		final Object bar = ((Map<Object, Object>) foo).get("bar");
		assertTrue(bar instanceof Integer);
		assertEquals(123, ((Integer) bar).intValue());
		final Object baz = ((Map<Object, Object>) foo).get("baz");
		assertTrue(baz instanceof Integer);
		assertEquals(234, ((Integer) baz).intValue());
	}

	/**
	 * Tests the parsing and returning of comments is not influencing the return
	 * value.
	 */
	@SuppressWarnings("unchecked")
	public static void testCommentParsingIgnored() {
		final Yaml yml = new Yaml();

		// defaults to true
		assertTrue(yml.isSkipComments());

		// change to false
		yml.setSkipComments(false);
		assertFalse(yml.isSkipComments());

		final Object obj = yml.load(YAML_STRING);
		assertTrue(obj instanceof Map);
		final Object foo = ((Map<Object, Object>) obj).get("foo");
		assertTrue(foo instanceof Map);
		final Object bar = ((Map<Object, Object>) foo).get("bar");
		assertTrue(bar instanceof Integer);
		assertEquals(123, ((Integer) bar).intValue());
		final Object baz = ((Map<Object, Object>) foo).get("baz");
		assertTrue(baz instanceof Integer);
		assertEquals(234, ((Integer) baz).intValue());
	}

	/**
	 * Tests the parsing and returning of comments caugth by custom constructor.
	 */
	public static void testCommentParsing() {
		final Yaml yml = new Yaml(new MyConstructor());
		yml.setSkipComments(false);

		final Object obj = yml.load(YAML_STRING);
		assertTrue(obj instanceof MyCommentMap);
		final Object foo = ((MyCommentMap) obj).get("foo");
		assertTrue(foo instanceof MyCommentMap);
		final Object bar = ((MyCommentMap) foo).get("bar");
		assertTrue(bar instanceof Integer);
		assertEquals(123, ((Integer) bar).intValue());
		final Object baz = ((MyCommentMap) foo).get("baz");
		assertTrue(baz instanceof Integer);
		assertEquals(234, ((Integer) baz).intValue());

		assertArrayEquals(new String[] { "# this is a top level comment." }, ((MyCommentMap) obj).mapLevelComments);
		assertArrayEquals(new String[] { "# some nested comment" }, ((MyCommentMap) foo).mapLevelComments);
		assertArrayEquals(new String[] { "# second nested comment" }, ((MyCommentMap) foo).valueComments.get("baz"));
	}

}
