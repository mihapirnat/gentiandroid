package si.formias.gentian.xml.log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class OutgoingText extends LogText {

	public OutgoingText(Attributes attributes) throws SAXException {
		super("OutgoingText", attributes);
	}
}
