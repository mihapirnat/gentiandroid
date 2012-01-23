/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package si.formias.gentian.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author miha
 */
public interface NodeFactory {
	public Node newNode(String tag, Attributes attributes) throws SAXException;
}
