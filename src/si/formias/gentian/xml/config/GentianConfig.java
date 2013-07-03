package si.formias.gentian.xml.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import si.formias.gentian.xml.Node;

public class GentianConfig extends Node {
	public final List<GentianAccount> accounts = Collections
			.synchronizedList(new ArrayList<GentianAccount>());

	public GentianConfig(Attributes attributes) throws SAXException {
		super("GentianConfig", attributes);
	}

	@Override
	public void add(Node n) {
		super.add(n);
		if (n instanceof GentianAccount) {
			accounts.add((GentianAccount) n);
		}
	}

	public List<GentianAccount> getAccounts() {
		return new ArrayList<GentianAccount>(accounts);
	}

	public GentianAccount getAccount(String host, int port, String user) {
		for (GentianAccount acc : accounts) {
			if (acc.getServer().equals(host) && acc.getPort() == port
					&& acc.getUser().equals(user)) {
				return acc;
			}
		}
		return null;
	}

	public GentianBuddy getContact(String host, int port, String user) {
		for (GentianAccount acc : accounts) {
			if (acc.getServer().equals(host) && acc.getPort() == port) {
				System.out.println("Found server match");
				for (GentianBuddy buddy : acc.buddies) {
					System.out.println("Comparing " + buddy.getUser() + " and "
							+ user);
					if (buddy.getUser().equals(user)) {
						System.out.println("Found buddy match");
						return buddy;
					}
				}
			}
		}
		return null;
	}

	public List<GentianAccount> getAccounts(String host, int port) {
		List<GentianAccount> list = new ArrayList<GentianAccount>();
		for (GentianAccount acc : accounts) {
			if (acc.getServer().equals(host) && acc.getPort() == port) {
				list.add(acc);
			}
		}
		return list;
	}

}
