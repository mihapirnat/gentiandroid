package si.formias.gentian.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class Answer extends Node {
	public Answer(String tag, Attributes attr) throws SAXException {
		super(tag, attr);
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
			sb.append(">");
			if (text != null) {
				sb.append(text.trim().replace("&", "&amp;")
						.replace("<", "&lt;").replace(">", "&gt;"));
			}
			for (Node n : children) {
				sb.append(n.toString(depth + 1));
			}
			sb.append("</");
			sb.append(tag);
			sb.append(">\n");
		}
		return sb.toString();
	}
}
