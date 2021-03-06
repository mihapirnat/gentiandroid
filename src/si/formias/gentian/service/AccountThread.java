package si.formias.gentian.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.xml.sax.SAXException;

import android.util.Log;

import si.formias.gentian.Compatibility;
import si.formias.gentian.GentianService;
import si.formias.gentian.Util;
import si.formias.gentian.http.HttpMagic;
import si.formias.gentian.xml.Parser;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.messages.Message;
import si.formias.gentian.xml.messages.MessagesReply;

public class AccountThread extends Thread {
	public boolean alive = true;
	final int n;
	static int nextId = 0;
	final HttpMagic magic;
	final Parser parser = new Parser();
	final GentianAccount account;
	long tail = 0;
	final List<SendMessage> outQueue = Collections
			.synchronizedList(new ArrayList<SendMessage>());
	private GentianService service;
	public volatile boolean hasMessage;
	volatile boolean interrupted;

	public AccountThread(GentianAccount acc, GentianService service) {
		this.magic = new HttpMagic("utf-8",service);
		this.account = acc;
		this.n = ++nextId;
		this.service = service;
		this.tail = acc.getTail();
		start();
	}

	public GentianAccount getAccount() {
		return account;
	}

	@Override
	public void run() {
		if (account.getServer().equals(GentianAccount.SMS)) {
			return;
		}
		while (alive) {
			/*
			 * System.out.println("Gentian Service Account Thread " + n + ": " +
			 * account.getUser() + " running.");
			 */
			interrupted = interrupted | Thread.interrupted()
					| outQueue.size() > 0;
			if (Compatibility.isScreenOn(service)) {
				Map<String, String> postMap = new LinkedHashMap<String, String>();
				postMap.put("user", account.getUser());
				postMap.put("password", account.getPassword());
				int size = outQueue.size();
				for (int i = 0; i < size; i++) {
					SendMessage send = outQueue.get(i);
					postMap.put("to" + i, send.target);
					postMap.put("msg" + i, send.msg);
				}
				postMap.put("tail", Long.toString(tail));

				/*
				 * System.out.println(account.getUser() + " sendmap:" +
				 * postMap);
				 */
				HttpEntity entity;
				try {
					String url = "http://" + account.getServer() + ":"
							+ account.getPort() + "/messages/";
					Log.d("AccountThread", "Checking url: " + url);
					entity = magic.postURL(url, magic.getPostData(postMap),
							null,true);
					hasMessage = false;
					interrupted = false;
					try {
						/*
						 * String textreply =
						 * Util.readStream(entity.getContent());
						 * System.out.println("Server reply: " + textreply);
						 * parser.parse(new ByteArrayInputStream(textreply
						 * .getBytes("utf-8")));
						 */
						// Log.d("AccountThread",Util.readStream(entity.getContent()));
						String s= Util.readStream(entity.getContent());
						Log.d("AccountThread",s);
						parser.parse(new ByteArrayInputStream(s.getBytes("utf-8")));

						MessagesReply reply = (MessagesReply) parser.root;
						for (Message msg : reply.messages) {
							tail = Math.max(tail, msg.getTimeStamp());
						}
						service.cancelNotifyCannotCheckAccount();
						service.newMessageReply(account, reply);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}
					for (int i = size - 1; i >= 0; i--) {
						outQueue.remove(i);
					}

				} catch (IOException e1) {
					try {
						if (hasMessage) {
							hasMessage = false;
							interrupted = true;
							service.notifyCannotCheckAccount();
						}
						Thread.sleep(15000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO Auto-generated catch block
					// e1.printStackTrace();
				}
			}
			try {
				if (!hasMessage && !interrupted)
					Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("END Gentian Service Account Thread: "
				+ account.getUser() + ".");
	}

	public void sendMessage(String account, String message) {
		outQueue.add(new SendMessage(account, message));
		interrupt();
	}

	private class SendMessage {
		String target, msg;

		SendMessage(String target, String msg) {
			this.target = target;
			this.msg = msg;
		}

		public String toString() {
			return account + "  to " + target + ": " + msg;
		}
	}
}
