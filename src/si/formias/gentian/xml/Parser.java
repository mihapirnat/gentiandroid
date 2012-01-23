/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.formias.gentian.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author miha
 */
public class Parser extends DefaultHandler {

	List<Node> nodeStack;
	public Node root;
	AbstractNodeFactory factory = new AbstractNodeFactory();

	@Override
	public void startDocument() throws SAXException {

		nodeStack = new ArrayList<Node>();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		String nametext = name;
		if (localName.length() > name.length())
			nametext = localName;
		Node n = factory.newNode(nametext, attributes);
		if (nodeStack.size() > 0) {
			nodeStack.get(nodeStack.size() - 1).add(n);
		}

		nodeStack.add(n);
		/*
		 * if (localName.equalsIgnoreCase(ITEM)){ this.currentMessage = new
		 * Message(); }
		 */
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		Node n = nodeStack.get(nodeStack.size() - 1);
		if (n.text == null)
			n.text = new String(ch, start, length);
		else
			n.text += new String(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		Node n = nodeStack.remove(nodeStack.size() - 1);
		n.buildFinished();
		if (nodeStack.size() == 0) {
			root = n;
		}
	}

	public void parse(InputStream in) throws SAXException,
			ParserConfigurationException, IOException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		// http://www.mkyong.com/java/how-to-read-utf-8-xml-file-in-java-sax-parser/
		Reader reader = new InputStreamReader(in, "UTF-8");

		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");

		parser.parse(is, this);
		reader.close();
		System.gc();
		// System.out.println(root);
	}
}
