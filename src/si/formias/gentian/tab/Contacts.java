package si.formias.gentian.tab;

import gentian.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import si.formias.gentian.CountryPhone;
import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.TopButtons;
import si.formias.gentian.dialog.EditContactDialog;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianBuddy;
import si.formias.gentian.xml.log.GentianLog;
import si.formias.gentian.xml.log.IncomingText;
import si.formias.gentian.xml.log.OutgoingText;
import si.formias.gentian.xml.messages.Message;

import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Contacts extends Tab {
	GentianChat main;
	Context context;

	public Contacts(GentianChat main) {
		super(main);
		this.main = main;
		this.context = main;

	}

	Map<String, BuddyStructure> buddyStruct = Collections
			.synchronizedMap(new HashMap<String, BuddyStructure>());

	static class BuddyStructure {
		GentianBuddy buddy;
		Button button;
		int messagesWaiting = 0;
		List<Message> messages = new ArrayList<Message>();

		private BuddyStructure(GentianBuddy buddy, Button b) {
			this.buddy = buddy;
			this.button = b;
		}

		private String getText() {
			return buddy.getDisplayName()
					+ (messagesWaiting > 0 ? " (" + messagesWaiting + ")" : "");
		}
	}

	public void updateContacts() {
		center.removeAllViews();

		List<GentianBuddy> buddies = new ArrayList<GentianBuddy>();
		LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		for (GentianAccount acc : main.config.configData.accounts) {
			{
				/*
				 * TextView v = new TextView(context);
				 * v.setTypeface(Typeface.DEFAULT_BOLD);
				 * v.setText(acc.getUser()+
				 * "@"+acc.getServer()+":"+acc.getPort()+
				 * " ("+acc.buddies.size()+")"); center.addView(v);
				 */
			}
			for (GentianBuddy buddy : acc.buddies) {
				buddies.add(buddy);
				if (GentianChat.CONVERTPHONENUMBERS) {

					if (buddy.getAccount().getServer().equals(GentianAccount.SMS) && buddy.getTarget()!=null && !buddy.getTarget().trim().startsWith("+")) {
						System.out.println("converting to international");
						TelephonyManager manager = (TelephonyManager) main
								.getSystemService(Context.TELEPHONY_SERVICE);
						/*System.out.println("Sim country:"
								+ manager.getSimCountryIso()+" Sim operator:"+manager.getSimOperator());*/
						buddy.setTextOf(GentianBuddy.TARGET, CountryPhone.prefixForOperator(manager.getSimOperator())+buddy.getTarget().trim().substring(1));
						//System.out.println(CountryPhone.prefixForOperator(manager.getSimOperator())+buddy.getTarget().substring(1));
						main.config.saveConfig();
					}
				}
			}
		}
		Collections.sort(buddies, new Comparator<GentianBuddy>() {

			@Override
			public int compare(GentianBuddy object1, GentianBuddy object2) {
				return object1.getDisplayName().compareTo(
						object2.getDisplayName());
			}

		});

		for (final GentianBuddy buddy : buddies) {
			Button v = new Button(context);
			String user = buddy.getUser();
			BuddyStructure struct = buddyStruct.get(user);
			
			if (struct == null) {
			
				struct = new BuddyStructure(buddy, v);
				buddyStruct.put(user, struct);
			} else {
				struct.buddy = buddy;
				struct.button=v;
			}
			struct.messagesWaiting = buddy.getWaiting();
			v.setTypeface(Typeface.DEFAULT);
			v.setBackgroundResource(Color.TRANSPARENT);
			v.setTextColor(Color.BLACK);
			v.setGravity(Gravity.LEFT);
			struct.button.setText(struct.getText());
			final BuddyStructure s = struct;
			OnClickListener click = new OnClickListener() {

				@Override
				public void onClick(View v) {
					s.messagesWaiting=0;
					updateContactsWaiting();
					s.buddy.setWaiting(s.messagesWaiting);
					//System.out.println("click setting messages waiting to:"+s.messagesWaiting);
					s.button.setText(s.getText());
					main.messages.startConversation(buddy, getLog(buddy
							.getUser()));

					
					main.config.saveConfig();
				}
			};
			OnLongClickListener longClick=new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					new EditContactDialog(main, buddy);
				    return true;

				}
			};
			v.setOnClickListener(click);
			v.setOnLongClickListener(longClick);
			ImageView icon = new ImageView(context);
			if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {
				icon.setImageResource(R.drawable.smscontact);
			} else {
				icon.setImageResource(R.drawable.torcontact);
			}
			icon.setClickable(true);
			icon.setOnClickListener(click);
			icon.setOnLongClickListener(longClick);
			LinearLayout l = new LinearLayout(context);
			l.setOrientation(LinearLayout.HORIZONTAL);
			l.setBackgroundResource(R.drawable.buddies);
			l.addView(icon, new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
			l.addView(v, parms);
			center.addView(l, parms);
		}
	}

	public void newMessage(Message m, String decrypted) {
		boolean waiting = decrypted == null;
		String user = m.getUser();
		BuddyStructure struct = buddyStruct.get(user);
		System.out.println("new message contcts");
		if (struct != null && waiting) {
			System.out.println("new message update waiting..contacts");
			struct.messagesWaiting++;
			struct.buddy.setWaiting(struct.messagesWaiting);
			struct.messages.add(m);
			updateContactsWaiting();
			// String enc=main.config.encodeFilename(m.getUser());
			struct.button.setText(struct.getText());
			main.config.saveConfig();
		}
		if (struct != null) {

			String s = waiting ? Messages.decrypt(struct.buddy, m.getText())
					: decrypted;
			if (s != null) {
				GentianLog log = getLog(user);
				IncomingText text;
				try {
					text = new IncomingText(null);
					text.setText(s);
					text.setTimeStamp(m.getTimeStamp());
					log.add(text);
					main.config.saveNodeToFile(log, getLogFile(user));
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (struct.buddy != null)
					struct.buddy.getAccount().setTail(m.getId());
			}
		}
	}

	private void updateContactsWaiting() {
		int waiting=0;
		for (BuddyStructure struct:buddyStruct.values()) {
			waiting+=struct.messagesWaiting;
		}
		main.topButtons.updateContactsWaiting(waiting);
	}

	public void newOutgoingMessage(String user, String text) {
		GentianLog log = getLog(user);
		OutgoingText logText;
		try {
			logText = new OutgoingText(null);
			logText.setTimeStamp(System.currentTimeMillis());
			logText.setText(text);
			log.add(logText);
			main.config.saveNodeToFile(log, getLogFile(user));
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public synchronized void updateConfig() {
		main.config.saveConfig();
		System.out.println("contacts.updateConfig");

	}

	public List<Message> clearNotification(GentianBuddy buddy) {
		String user = buddy.getUser();
		BuddyStructure struct = buddyStruct.get(user);
		List<Message> received = struct.messages;
		struct.messages = new ArrayList<Message>();
		struct.messagesWaiting = 0;
		struct.button.setText(struct.getText());
		return received;

	}

	Map<String, GentianLog> logCache = Collections
			.synchronizedMap(new LinkedHashMap<String, GentianLog>());

	public GentianLog getLog(String user) {
		GentianLog log = logCache.get(user);
		if (log == null) {
			File f = getLogFile(user);
			if (f.exists()) {
				try {
					log = (GentianLog) main.config.loadNodeFile(f);
				} catch (Exception e) {

				}
			}
			if (log == null) {
				try {
					log = new GentianLog(null);
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return log;
	}

	Map<String, File> logFileCache = Collections
			.synchronizedMap(new LinkedHashMap<String, File>());

	public File getLogFile(String user) {
		if (logFileCache.containsKey(user)) {
			return logFileCache.get(user);
		}
		try {
			File logdir = new File(main.config.gentian, "log");
			if (!logdir.exists()) {
				logdir.mkdir();
			}
			File f = new File(logdir, main.config.encodeFilename(user));
			logFileCache.put(user, f);
			return f;
		} catch (Exception e) {
			return null;
		}
	}

	static class ContactsState {
		Map<String, GentianLog> logCache;
		Map<String, File> logFileCache;
	}

	@Override
	public Object saveState() {
		ContactsState state = new ContactsState();
		state.logCache = logCache;
		state.logFileCache = logFileCache;
		return state;
	}

	@Override
	public void loadState(Object o) {
		try {
			ContactsState state = (ContactsState) o;
			state.logCache = logCache;
			state.logFileCache = logFileCache;
		} catch (Exception e) {

		}

	}
}
