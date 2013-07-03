package si.formias.gentian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import si.formias.gentian.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.gsm.SmsMessage;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver

// http://mobiforge.com/developing/story/sms-messaging-android
{
	boolean debug = false;
	final static Map<String, List<String>> messageQueue = Collections
			.synchronizedMap(new LinkedHashMap<String, List<String>>());

	public SmsReceiver() {
		super();
		if (debug)
			System.out.println("New sms receiver");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReceive(Context context, Intent intent) {
		// if (debug) System.out.println("SMS RECEIVER...onReceive");
		// ---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String str = "";
		if (bundle != null) {
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				String source = msgs[i].getOriginatingAddress();
				String body = msgs[i].getMessageBody().toString();
				// if (!body.startsWith("gentian")) continue;
				try {
					// int frame=((body.charAt(0)-'A')%5)+1;
					// int count=((body.charAt(0)-'A')/5)+1;
					int frame = -1;
					int count = -1;
					try {
						frame = Integer.parseInt(Character.toString(body
								.charAt(0))) + 1;
						count = Integer.parseInt(Character.toString(body
								.charAt(1))) + 1;
					} catch (Exception e) {
						continue;
					}
					// int count=((body.charAt(0)-'A')/5)+1;
					if (frame > count)
						continue;
					if (frame < 1 || count < 1 || frame > 9 || count > 9)
						continue;
				} catch (Exception e) {
					continue;
				}
				List<String> list = messageQueue.get(source);
				if (list == null) {
					list = Collections
							.synchronizedList(new ArrayList<String>());
					messageQueue.put(source, list);
				}
				list.add(body);

			}
			if (debug)
				System.out.println(messageQueue);
			// ---display the new SMS message---
			// if (str.length()>0)Toast.makeText(context, str,
			// Toast.LENGTH_SHORT).show();

			for (String phone : messageQueue.keySet()) {
				List<String> list = messageQueue.get(phone);

				int index = 0;
				int lastmsg = -1;
				String[] assembleString = null;
				int count = 0;
				for (String s : list) {
					index++;
					if (debug)
						System.out.println("list index:" + index + " s=" + s);
					// int frame=((s.charAt(0)-'A')%5)+1;
					// int totalcount=((s.charAt(0)-'A')/5)+1;
					int frame = -1;
					int totalcount = -1;
					try {
						frame = Integer
								.parseInt(Character.toString(s.charAt(0))) + 1;
						totalcount = Integer.parseInt(Character.toString(s
								.charAt(1))) + 1;
					} catch (Exception e) {
						continue;
					}
					if (frame < 1 || totalcount < 1 || frame > 9
							|| totalcount > 9)
						continue;
					if (debug)
						System.out.println("frame: " + frame + " total:"
								+ totalcount);
					if (frame == 1) {
						assembleString = new String[totalcount];

						count = 0;
					} else if (assembleString != null
							&& assembleString.length != totalcount) {

						assembleString = new String[totalcount];

						count = 0;
					}
					if (assembleString != null) {
						if (debug)
							System.out.println("assemble:");
						for (int i = 0; i < assembleString.length; i++) {
							if (debug)
								System.out.println("[" + i + "]"
										+ assembleString[i]);
						}
					}
					if (assembleString == null || frame > assembleString.length) {
						if (debug)
							System.out.println("wrong sms sequence: frame="
									+ frame);
						lastmsg = index;
						index = 0;

						assembleString = null;
					} else {
						// String part = s.substring(1);
						String part = s.substring(2);
						assembleString[frame - 1] = part;
						count++;
						boolean ok = true;
						for (String sx : assembleString) {
							if (sx == null) {
								ok = false;
								break;
							}

						}
						if (ok) {
							StringBuilder smsbuilder = new StringBuilder();
							for (String p : assembleString) {
								smsbuilder.append(p);
							}
							assembleString = null;
							count = 0;
							lastmsg = index;
							index = 0;
							if (debug)
								System.out.println("complete msg... notifying");
							try {
								String t = smsbuilder.toString();
								byte[] b = Base91.decode(t.substring(0,
										t.length() - 1).getBytes("latin1"));
								if (b != null) {
									// at least encoding is valid, might be...
									notifyClient(context, phone, t);
								}
							} catch (Exception e) {

							}
						}

					}
				}
				if (lastmsg > 0) {
					for (int i = 0; i < lastmsg; i++) {
						list.remove(0);
					}
				}
			}

		}
	}

	NotificationManager mNM;

	private void notifyClient(Context context, String phone, String string) {
		if (debug)
			System.out.println("smsreceiver new sms from: " + phone);
		/*
		 * Intent intent = new Intent(context, GentianChat.class);
		 * intent.putExtra("smsphone", phone); intent.putExtra("smstext",
		 * string); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
		 * FLAG_ACTIVITY_SINGLE_TOP);
		 * 
		 * 
		 * if (debug) System.out.println("Notifying activity about sms :D");
		 * context.startActivity(intent);
		 */
		if (GentianChat.activeChat != null) {
			if (debug)
				System.out.println("SmsReceiver: sending to current activity");
			GentianChat.activeChat.receiveSms(phone,
					System.currentTimeMillis(), string);
		} else {
			if (debug)
				System.out.println("SmsReceiver: no activity, add to wait");
			File f = new File(Config.gentian, "smswaiting");
			ArrayList<String> lines = new ArrayList<String>();
			if (f.exists()) {
				try {
					BufferedReader r = new BufferedReader(
							new InputStreamReader(new FileInputStream(f),
									"utf-8"));
					String line;

					while ((line = r.readLine()) != null) {
						lines.add(line);
					}
					r.close();

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			try {
				lines.add(phone + ":" + System.currentTimeMillis() + ":"
						+ string);
				PrintWriter w = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(f), "utf-8"));
				for (String l : lines) {
					w.println(l);
				}
				w.close();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Set the icon, scrolling text and timestamp
			String text = String.format(
					context.getText(R.string.waiting_messages).toString(),
					lines.size());
			Notification notification = new Notification(R.drawable.smscontact,
					text, System.currentTimeMillis());

			// The PendingIntent to launch our activity if the user selects this
			// notification
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					new Intent(context, GentianChat.class), 0);

			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(context,
					context.getText(R.string.app_name), text, contentIntent);
			if (mNM == null) {
				mNM = (NotificationManager) context
						.getSystemService(Context.NOTIFICATION_SERVICE);
			}
			mNM.notify(R.drawable.smscontact, notification);
		}
	}

}