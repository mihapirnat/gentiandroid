package si.formias.gentian.xml.log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class IncomingText extends LogText {

	public IncomingText(Attributes attributes) throws SAXException {
		super("IncomingText", attributes);
	}

}
