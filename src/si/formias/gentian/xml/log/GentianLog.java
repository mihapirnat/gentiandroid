package si.formias.gentian.xml.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.Node;

public class GentianLog extends Node {
	public final List<LogText> log =Collections.synchronizedList(new ArrayList<LogText>());
	
	public GentianLog(Attributes attributes) throws SAXException {
		super("GentianLog", attributes);
	}
	@Override
	public void add(Node n) {
		super.add(n);
		if (n instanceof LogText) {
			log.add((LogText)n);
		}
	}

}
