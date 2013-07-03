package si.formias.gentian.xml.messages;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.Answer;

public class Message extends Answer {
	public Message(Attributes attr) throws SAXException {
		super("Message", attr);
	}

	static final String FROM = "from";
	static final String ID = "id";
	static final String TIME = "time";

	public String getUser() {
		return get(FROM);
	}

	public long getId() {
		try {
			return Long.parseLong(get(ID));
		} catch (Exception e) {

		}
		return 0;
	}

	public long getTimeStamp() {
		try {
			long t = Long.parseLong(get(TIME));
			// System.out.println("Returning timestamp: "+t);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("Returning timestamp: 0");
		return 0;
	}

	public void setUser(String user) {
		set(FROM, user);

	}

	public void setTimeStampNow() {
		set(TIME, Long.toString(System.currentTimeMillis() / 1000));
	}

	public void setTimeStamp(long currentTimeMillis) {
		set(TIME, Long.toString(currentTimeMillis / 1000));
	}
}
