package si.formias.gentian;

import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.xml.sax.SAXException;

import com.google.android.gcm.GCMRegistrar;

import si.formias.gentian.R;
import si.formias.gentian.http.HttpMagic;
import si.formias.gentian.service.AccountThread;
import si.formias.gentian.xml.Parser;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianConfig;
import si.formias.gentian.xml.messages.MessagesReply;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class GentianService extends Service {
	/** For showing and hiding our notification. */
	NotificationManager mNM;
	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	/** Holds last value set by a client. */
	int mValue = 0;

	byte[] check;

	Map<String, AccountThread> threadMap = Collections
			.synchronizedMap(new LinkedHashMap<String, AccountThread>());
	Map<String, AccountThread> aliasMap = Collections
			.synchronizedMap(new LinkedHashMap<String, AccountThread>());

	/**
	 * Command to the service to register a client, receiving callbacks from the
	 * service. The Message's replyTo field must be a Messenger of the client
	 * where callbacks should be sent.
	 */
	static final int MSG_REGISTER_CLIENT = 1;

	/**
	 * Command to the service to unregister a client, ot stop receiving
	 * callbacks from the service. The Message's replyTo field must be a
	 * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
	 */
	static final int MSG_UNREGISTER_CLIENT = 2;

	/**
	 * Command to service to set a new value. This can be sent to the service to
	 * supply a new value, and will be sent by the service to any registered
	 * clients with the new value. MSG_SEND_GCM_REG
	 */
	static final int MSG_SET_VALUE = 3;

	static final int MSG_REGISTERED_WELCOME = 4;

	static final int MSG_SET_CONFIG = 5;

	static final int MSG_SEND_MESSAGE = 6;

	static final int MSG_MESSAGE_REPLY = 7;
	public static final int MSG_SEND_GCM_REG = 8;
	public static final int MSG_REFRESHNOW = 9;
	public static final int MSG_CANNOT_SENDCHECK_MESSAGE = 10;
	GentianConfig config;

	void initConnections() {

	}

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				try {
					Bundle bundle = msg.getData();
					byte[] check = bundle.getByteArray("check");
					if (GentianService.this.check == null
							|| Util.equal(GentianService.this.check, check)) {
						if (bundle.getBoolean("register", true)) {
							mClients.add(msg.replyTo);
						}
						if (GentianService.this.check == null) {
							GentianService.this.check = check;
							initConnections();
						}
						msg.replyTo.send(Message.obtain(null,
								MSG_REGISTERED_WELCOME, 0, 0));
						System.out
								.println("Gentian Service: Client registered, current clients:"
										+ mClients.toString());

						if (waitingNumberMessages > 0) {
							for (String user : waitingMessages.keySet()) {
								List<si.formias.gentian.xml.messages.Message> list = waitingMessages
										.get(user);
								MessagesReply reply = new MessagesReply(null);
								reply.setUser(user);
								reply.children().addAll(list);
								Message msg1 = Message.obtain(null,
										MSG_MESSAGE_REPLY, mValue, 0);
								Bundle bundle1 = new Bundle();
								bundle1.putString("reply", reply.toString(0));
								msg1.setData(bundle1);
								// System.out.println("Sent message reply to client..");
								for (int i = mClients.size() - 1; i >= 0; i--) {
									try {

										mClients.get(i).send(msg1);
									} catch (RemoteException e) {
										// The client is dead. Remove it from
										// the list;
										// we are going through the list from
										// back to front
										// so this is safe to do inside the
										// loop.
										mClients.remove(i);
									}
								}
							}

						}
						if (mClients.size() > 0) {
							if (waitingMessages != null) {
								waitingMessages.clear();
							}
							waitingNumberMessages = 0;
							mNM.cancel(R.drawable.icon);
						}
					}
				} catch (Exception e) {

				}
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				System.out
						.println("Gentian Service: Client gone current clients:"
								+ mClients.toString());
				break;
			case MSG_SET_VALUE:
				mValue = msg.arg1;
				for (int i = mClients.size() - 1; i >= 0; i--) {
					try {
						mClients.get(i).send(
								Message.obtain(null, MSG_SET_VALUE, mValue, 0));
					} catch (RemoteException e) {
						// The client is dead. Remove it from the list;
						// we are going through the list from back to front
						// so this is safe to do inside the loop.
						mClients.remove(i);
					}
				}
				break;
			case MSG_SET_CONFIG: {
				Bundle bundle = msg.getData();
				String configString = bundle.getString("config");
				Parser p = new Parser();
				try {
					p.parse(new ByteArrayInputStream(configString
							.getBytes("utf-8")));
					// System.out.println("Gentian Service: Config set.");
					config = (GentianConfig) p.root;
					Set<String> accountKeys = new LinkedHashSet<String>();
					for (GentianAccount acc : config.accounts) {
						String id = acc.toString();
						accountKeys.add(id);
						if (!threadMap.containsKey(id)) {
							AccountThread t = new AccountThread(acc,
									GentianService.this);
							threadMap.put(id, t);
							aliasMap.put(acc.getAliasId(), t);

						}
					}
					List<String> removeList = new ArrayList<String>();
					for (String key : threadMap.keySet()) {
						if (!accountKeys.contains(key)) {
							removeList.add(key);
						}
					}
					for (String threadId : removeList) {
						AccountThread t = threadMap.remove(threadId);
						t.alive = false;
						aliasMap.remove(t.getAccount().getAliasId());
					}
					for (AccountThread t : threadMap.values()) {
						t.interrupt();
					}
					new Thread() {
						public void run() {

							initGCM();
						}
					}.start();
					mNM.cancel(R.string.tormessagebutnoservice);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				break;
			case MSG_SEND_MESSAGE: {
				Bundle bundle = msg.getData();
				String acc = bundle.getString("account");
				// System.out.println("looking for thread: "+acc);
				AccountThread thread = threadMap.get(acc);
				if (thread != null) {
					// System.out.println("thread found: "+thread);
					thread.sendMessage(bundle.getString("target"),
							bundle.getString("msg"));

				}
			}
				break;
			case MSG_SEND_GCM_REG:
				gcmReg(msg);
				break;
			case MSG_REFRESHNOW: {
				String alias = msg.getData().getString("alias");
				String server = msg.getData().getString("server");
				String key = alias + "@" + server;
				AccountThread t = aliasMap.get(key.replace(" ", "+"));
				if (t != null) {
					t.hasMessage = true;
					t.interrupt();
				} else {
					Log.d("GentianService", "Can't find server thread: " + key
							+ " alias keys:" + aliasMap.keySet());
					if (config == null) {
						notifyCannotCheckAccountNoService();
					}
				}
			}

				break;
			default:
				super.handleMessage(msg);
			}
		}

		private void gcmReg(final Message msg) {
			new Thread() {
				public void run() {
					Log.d("Gentianservice", "gcmReg: "
							+ msg.getData().getString("gcmregid"));
					registerGCMKeyToServer(msg.getData().getString("gcmregid"));
				}
			}.start();

		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		new Thread() {
			public void run() {

				initGCM();
			}
		}.start();
		// Display a notification about us starting.
		showNotification();
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(R.string.remote_service_started);
		for (String key : threadMap.keySet()) {
			threadMap.get(key).alive = false;
		}
		threadMap.clear();
		// Tell the user we stopped.
		Toast.makeText(this, R.string.remote_service_stopped,
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = getText(R.string.remote_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, GentianChat.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text,
				contentIntent);

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		// / mNM.notify(R.string.remote_service_started, notification);
	}

	volatile int waitingNumberMessages = 0;
	Map<String, List<si.formias.gentian.xml.messages.Message>> waitingMessages = Collections
			.synchronizedMap(new LinkedHashMap<String, List<si.formias.gentian.xml.messages.Message>>());

	public void newMessageReply(GentianAccount account, MessagesReply reply) {
		if (mClients.size() > 0) {
			Message msg = Message.obtain(null, MSG_MESSAGE_REPLY, mValue, 0);
			Bundle bundle = new Bundle();
			bundle.putString("reply", reply.toString(0));
			msg.setData(bundle);
			// System.out.println("Sent message reply to client..");
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {

					mClients.get(i).send(msg);
				} catch (Exception e) {
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
				}
			}
		} else {
			if (reply.messages.size() > 0) {
				for (si.formias.gentian.xml.messages.Message m : reply.messages) {
					String user = account.getUser();
					List<si.formias.gentian.xml.messages.Message> messages = waitingMessages
							.get(user);
					if (messages == null) {
						messages = new ArrayList<si.formias.gentian.xml.messages.Message>();
						waitingMessages.put(user, messages);
					}
					messages.add(m);
					waitingNumberMessages++;
				}
			}
			if (waitingNumberMessages > 0) {
				String text = String.format(getText(R.string.waiting_messages)
						.toString(), waitingNumberMessages);
				// System.out.println("gentian service notification: "+text);
				Notification notification = new Notification(R.drawable.icon,
						text, System.currentTimeMillis());

				// The PendingIntent to launch our activity if the user selects
				// this notification
				PendingIntent contentIntent = PendingIntent.getActivity(this,
						0, new Intent(this, GentianChat.class), 0);

				notification.setLatestEventInfo(this,
						getText(R.string.app_name), text, contentIntent);
				mNM.notify(R.drawable.icon, notification);
			} else {
				mNM.cancel(R.drawable.icon);
			}
		}

	}

	private void initGCM() {
		if (config == null)
			return;
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, GCM.GCM_REGISTER);
		} else {
			Log.v("KatalogService", "Already registered: " + regId);
			registerGCMKeyToServer(regId);

		}

	}

	private void registerGCMKeyToServer(String regId) {
		if (regId == null) {
			Log.d("GentianService", "registerGCMKeyToServer null: " + regId
					+ ", later...");
			return;
		}
		gcmRegisteredOnce = true;
		Log.d("GentianService", "registerGCMKeyToServer: " + regId);

		StringBuilder sb = new StringBuilder();

		int i = 0;
		if (config != null) {
			for (GentianAccount acc : config.accounts) {
				sb.append("&alias" + i + "=" + acc.getAlias() + "@"
						+ acc.getServer() + ":" + acc.getPort());
				i++;
				if (i > 100)
					break;

			}

		}

		long retry = 10000;
		boolean notSend = true;
		OrbotHelper helper = new OrbotHelper(this);
		while (notSend) {
			try {
				String url = (helper.isOrbotRunning() ? GCM.getTorBase() : GCM
						.getBase())
						+ "register?gcmkey="
						+ regId
						+ sb.toString();
				if (GCM.isDebug()) {
					Log.d("GentianService", "Registering GCM " + url);
				}
				if (helper.isOrbotRunning()) {

					Map<String, String> postMap = new LinkedHashMap<String, String>();
					HttpMagic magic = new HttpMagic("utf-8");
					HttpEntity entity;
					entity = magic.postURL(url, magic.getPostData(postMap),
							null);
					String s = Util.readStream(entity.getContent());
				} else {
					new URL(url).openStream().close();
				}

				notSend = false;

			} catch (Exception e) {
				e.printStackTrace();
				Log.d("Gentianervice",
						"Failed to register GCM to server,will retry in:"
								+ (retry / 1000) + "s");
				try {
					Thread.sleep(retry);
				} catch (Exception e1) {

				}

			}
		}

	}

	volatile boolean gcmRegisteredOnce;

	public void notifyCannotCheckAccount() {
		if (mClients.size() > 0) {
			Message msg = Message.obtain(null, MSG_CANNOT_SENDCHECK_MESSAGE,
					mValue, 0);
			Bundle bundle = new Bundle();

			msg.setData(bundle);
			// System.out.println("Sent message reply to client..");
			for (int i = mClients.size() - 1; i >= 0; i--) {
				try {

					mClients.get(i).send(msg);
				} catch (Exception e) {
					// The client is dead. Remove it from the list;
					// we are going through the list from back to front
					// so this is safe to do inside the loop.
					mClients.remove(i);
				}
			}
		} else {
			CharSequence text = getText(R.string.tormessagebutnonetwork);

			// Set the icon, scrolling text and timestamp
			Notification notification = new Notification(R.drawable.torcontact,
					text, System.currentTimeMillis());

			// The PendingIntent to launch our activity if the user selects this
			// notification
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, GentianChat.class), 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this, getText(R.string.app_name),
					text, contentIntent);
			mNM.notify(R.string.tormessagebutnonetwork, notification);
		}

	}

	public void cancelNotifyCannotCheckAccount() {
		mNM.cancel(R.string.tormessagebutnonetwork);
	}

	public void notifyCannotCheckAccountNoService() {

		CharSequence text = getText(R.string.tormessagebutnoservice);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.torcontact,
				text, System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, GentianChat.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.app_name), text,
				contentIntent);
		mNM.notify(R.string.tormessagebutnoservice, notification);

	}
}