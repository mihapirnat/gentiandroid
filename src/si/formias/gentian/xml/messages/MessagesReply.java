package si.formias.gentian.xml.messages;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.tab.Messages;
import si.formias.gentian.xml.Node;

public class MessagesReply extends Node {
	public final List<Message> messages= new ArrayList<Message>();
	public MessagesReply(Attributes attributes) throws SAXException {
		super("MessagesReply", attributes);
	}
	@Override
	public void add(Node n) {
		super.add(n);
		if (n instanceof Message) {
			messages.add((Message)n);
		}
	}
	
	public void setUser(String user) {
		set("user",user);
		
	}

}
