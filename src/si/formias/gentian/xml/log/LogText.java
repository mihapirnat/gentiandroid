package si.formias.gentian.xml.log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.Answer;

public abstract class LogText extends Answer {
	static String TIME = "time";

	public LogText(String tag, Attributes attributes) throws SAXException {
		super(tag, attributes);
	}

	public void setTimeStamp(long time) {
		set(TIME, Long.toString(time));
	}

	public long getTimeStamp() {
		try {
			return Long.parseLong(get(TIME));
		} catch (Exception e) {
			return 0;
		}
	}
}
