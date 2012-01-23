/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.formias.gentian.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author miha
 */
public class Node {

	public final String tag;
	Node parent;
	protected final List<Node> children = Collections.synchronizedList(new ArrayList<Node>());
	public final Map<String, String> attributes;
	protected String text;

	public List<Node> children() {
		return children;
	}

	public Node(final String tag, Attributes attributes) throws SAXException {
		this.tag = tag;
		this.attributes = Collections.synchronizedMap(new LinkedHashMap<String, String>());
		if (attributes != null) {
			int len = attributes.getLength();
			for (int i = 0; i < len; i++) {
				String nametext = attributes.getQName(i);
				String localname = attributes.getLocalName(i);
				if (localname.length() > nametext.length())
					nametext = localname;
				this.attributes.put(nametext, attributes.getValue(i));

			}
		}
	}

	static Map<Integer, String> tabCache = new HashMap<Integer, String>();

	protected static String tabs(int depth) {
		String tab = tabCache.get(depth);
		if (tab == null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < depth; i++) {
				sb.append(" ");
			}
			tab = sb.toString();
			tabCache.put(depth, tab);
		}
		return tab;
	}

	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		String tab = tabs(depth);
		sb.append(tab + "<");
		sb.append(tag);

		printAttributes(sb);
		if (text == null && children.size() == 0) {
			sb.append(" />\n");
		} else {
			sb.append(">\n");
			if (text != null) {
				sb.append(text.trim());
			}
			for (Node n : children) {
				sb.append(n.toString(depth + 1));
			}
			sb.append(tab + "</");
			sb.append(tag);
			sb.append(">\n");
		}
		return sb.toString();
	}

	transient Map<String, Node> tagNodes = new HashMap<String, Node>();

	public Node getNodeByTag(String tag) {
		if (tagNodes.containsKey(tag)) {
			return tagNodes.get(tag);
		}
		for (Node n : children) {
			if (n.tag.equals(tag)) {
				tagNodes.put(tag, n);
				return n;
			}
		}
		for (Node n : children) {
			Node n1 = n.getNodeByTag(tag);
			if (n1 != null) {
				// tagNodes.put(tag, n);
				return n1;
			}
		}

		return null;
	}

	public List<Node> getNodesByTag(String tag) {
		List<Node> nodes = new ArrayList<Node>();
		for (Node n : children) {
			if (n.tag.equals(tag)) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	public void add(Node n) {
		children.add(n);
		n.parent = this;
	}

	public boolean remove(Node n) {
		return children.remove(n);
	}



	public String textOf(String tag) {
		Node n = getNodeByTag(tag);
		if (n != null) {
			return n.text;
		}
		return null;
	}
	public void setTextOf(String tag,String text) {
		Node n = getNodeByTag(tag);
		if (n == null) {
			try {
				n=new Answer(tag,null);
				add(n);
			} catch (SAXException e) {
				e.printStackTrace();
			}
			
		}
		n.setText(text);
	}
	public String get(String key) {
		return attributes.get(key);
	}

	public void set(String key, String value) {
		attributes.put(key, value);
	}

	public String getText() {
		return text; // text!=null?text.replace("&amp;","&").replace("&lt;","<").replace("&gt;",">"):text;
	}

	public void setText(String text) {
		this.text = text;
	}

	protected void printAttributes(StringBuilder sb) {
		for (String key : attributes.keySet()) {
			sb.append(" ");
			sb.append(key);
			sb.append("=\"");
			sb.append(attributes.get(key));
			sb.append("\"");
		}
	}

	/**
	 * Now it's good time to make cache...
	 * 
	 */
	public void buildFinished() {
	}

	public void cleanTree() {
		for (Node n : children) {
			n.cleanTree();
		}
		children.clear();
	}
	public void unset(String key) {
		attributes.remove(key);
	}
}
