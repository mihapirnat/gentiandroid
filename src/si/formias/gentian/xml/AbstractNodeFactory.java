/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.formias.gentian.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.config.*;
import si.formias.gentian.xml.log.*;
import si.formias.gentian.xml.messages.*;

/**
 * 
 * @author miha
 */
public class AbstractNodeFactory implements NodeFactory {

	final Map<String, NodeFactory> tag2factory = new HashMap<String, NodeFactory>();

	class NormalNodeFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new Answer(tag, attributes);
		}
	}

	class GentianConfigFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new GentianConfig(attributes);
		}
	}

	class GentianAccountFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new GentianAccount(attributes);
		}
	}

	class GentianBuddyFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new GentianBuddy(attributes);
		}
	}

	class MessagesReplyFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new MessagesReply(attributes);
		}
	}

	class MessageFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new Message(attributes);
		}
	}

	class GentianLogFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new GentianLog(attributes);
		}
	}

	class IncomingTextFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new IncomingText(attributes);
		}
	}

	class OutgoingTextFactory implements NodeFactory {

		public Node newNode(String tag, Attributes attributes)
				throws SAXException {
			return new OutgoingText(attributes);
		}
	}

	public AbstractNodeFactory() {
		tag2factory.put("GentianConfig", new GentianConfigFactory());
		tag2factory.put("GentianAccount", new GentianAccountFactory());
		tag2factory.put("GentianBuddy", new GentianBuddyFactory());

		tag2factory.put("MessagesReply", new MessagesReplyFactory());
		tag2factory.put("Message", new MessageFactory());

		tag2factory.put("GentianLog", new GentianLogFactory());
		tag2factory.put("IncomingText", new IncomingTextFactory());
		tag2factory.put("OutgoingText", new OutgoingTextFactory());

	}

	final NodeFactory normalNodeFactory = new NormalNodeFactory();

	public Node newNode(String tag, Attributes attributes) throws SAXException {
		NodeFactory factory = tag2factory.get(tag);
		// System.out.println("getting factory for: " + tag);
		if (factory != null) {
			return factory.newNode(tag, attributes);
		} else {
			return normalNodeFactory.newNode(tag, attributes);
		}
	}
}
