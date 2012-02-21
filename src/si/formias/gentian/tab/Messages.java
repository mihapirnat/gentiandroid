package si.formias.gentian.tab;

import gentian.crypt.GentianCrypt;
import gentian.crypt.GentianKey;
import gentian.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Calendar;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.TopButtons;
import si.formias.gentian.Util;
import si.formias.gentian.tab.msgcrypt.CryptShort;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianBuddy;
import si.formias.gentian.xml.log.GentianLog;
import si.formias.gentian.xml.log.IncomingText;
import si.formias.gentian.xml.log.LogText;
import si.formias.gentian.xml.log.OutgoingText;
import si.formias.gentian.xml.messages.Message;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Messages extends Tab {
	GentianChat main;
	Context context;
	public static GentianChat staticmain;
	TextView secrecyStatus;
	EditText sendText;
	public Messages(GentianChat main) {
		super(main);
		this.main=main;
		this.context=main;
		staticmain=main;
	}
	private GentianBuddy currentBuddy;
	public void startConversation(final GentianBuddy buddy, GentianLog gentianLog) {
		main.topButtons.chats.setChecked(true);
		System.out.println("start conversation");
		center.removeAllViews();
		{
			TextView v = new TextView(context);
			v.setTypeface(Typeface.DEFAULT_BOLD);
			String target= buddy.getTarget();
			
			v.setText(buddy.getNick()+" <"+(target!=null?target:buddy.getUser())+">");
			v.setTextColor(Color.BLACK);
			ImageView icon=new ImageView(context);
			if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {
				icon.setImageResource(R.drawable.smscontact);	
			} else {
			icon.setImageResource(R.drawable.torcontact);
			}
			LinearLayout l = new LinearLayout(context);
			l.setOrientation(LinearLayout.HORIZONTAL);
			l.addView(icon,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
			l.addView(v);
			center.addView(l);
		}
		currentBuddy=buddy;
		sendText=new EditText(context);
	/*	sendText.setImeOptions(sendText.getImeOptions()
				| EditorInfo.IME_FLAG_NO_EXTRACT_UI);*/
		Button sendButton=new Button(context);
		sendButton.setBackgroundResource(R.drawable.custombutton);
		sendButton.setTextColor(Color.BLACK);
		sendButton.setText(R.string.buttons_send);
		LinearLayout bottom=new LinearLayout(context);
		bottom.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams parmsEdit=new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT);
		parmsEdit.weight=1;
		bottom.addView(sendText,parmsEdit);
		bottom.addView(sendButton,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		LinearLayout l = new LinearLayout(context);
		l.setOrientation(LinearLayout.VERTICAL);
		secrecyStatus=new TextView(context);
		l.addView(secrecyStatus,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		l.addView(bottom,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		addBottom(l);
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = sendText.getText().toString();
				Calendar calendar=Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				sendText.setText("");
				addMyText(text+" ["+String.format("%1$tY/%1$tm/%1$td %1$tH:%1$tM",calendar)+"]",System.currentTimeMillis());
				main.contacts.newOutgoingMessage(buddy.getUser(), text);
				main.sendText(buddy,encrypt(buddy,text));
				
			}

			
		});
		if (gentianLog!=null) {
			
			for (LogText text : gentianLog.log) {
				if (text instanceof OutgoingText) {
					addMyText(text.getText(),text.getTimeStamp());
				} else if (text instanceof IncomingText) {
					addTheirText(text.getText(),text.getTimeStamp());
				}
				
			}
		}
		updateSecrecyStatus();
		/*for (Message m : main.contacts.clearNotification(buddy)) {
			addTheirText(m.getTe,m.getTimeStamp());
		}*/
	}
	private void updateSecrecyStatus() {
		if (currentBuddy!=null) {
			if (CryptShort.forwardSecrecy(currentBuddy)) {
				secrecyStatus.setBackgroundColor(Color.GREEN);
				secrecyStatus.setTextColor(Color.WHITE);
				secrecyStatus.setText(R.string.forward_secrecy_ok);
			} else {
				secrecyStatus.setBackgroundColor(Color.YELLOW);
				secrecyStatus.setTextColor(Color.BLACK);
				secrecyStatus.setText(R.string.no_forward_secrecy);
			}
		}
		
	}
	private void addMyText(String text,long timestamp) {
		{
			String formatStr= main.getText(R.string.date_format).toString();
			TextView msgView = new TextView(context);
			msgView.setTextColor(Color.BLACK);
			msgView.setTypeface(Typeface.DEFAULT_BOLD);
			Calendar calendar=Calendar.getInstance();
			calendar.setTimeInMillis(timestamp);
			msgView.setText(main.getText(R.string.text_me).toString()+", "+ String.format(formatStr,calendar));
			center.addView(msgView);
			}
		TextView msgView = new TextView(context);
		msgView.setTextColor(Color.BLACK);
		msgView.setText(text);
		center.addView(msgView);
		scrollDown();
	}
	private void addTheirText(String text,long timestamp) {
		{
		String formatStr= main.getText(R.string.date_format).toString();
		TextView msgView = new TextView(context);
		msgView.setTextColor(Color.BLACK);
		msgView.setTypeface(Typeface.DEFAULT_BOLD);
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(timestamp*1000);
		msgView.setText(currentBuddy.getDisplayName().toString()+", "+ String.format(formatStr,calendar));
		center.addView(msgView);
		}
	TextView msgView = new TextView(context);
	msgView.setTextColor(Color.BLACK);
	msgView.setText(text);
	center.addView(msgView);
	scrollDown();
	}
	public String  newMessage(Message m) {
		System.out.println("New message from:"+m.getUser());
		if (main.topButtons.getTabIndex()==1 &&currentBuddy!=null && currentBuddy.getUser().equals(m.getUser())) {
			System.out.println("new message");
			String s =decrypt(currentBuddy,m.getText());
			
				updateSecrecyStatus();;
			
			if (s!=null) {
				addTheirText(s,m.getTimeStamp());
			} else {
				final GentianBuddy buddy=currentBuddy;
				String s1 =main.getText(R.string.cant_decode_received_sms).toString();
				addTheirText(s1,m.getTimeStamp());
				main.askYesNo(s1,new Runnable() {
					public void run() {
						buddy.unset(CryptShort.TMPKEYENCRYPT);
						buddy.unset(CryptShort.TMPKEYDECRYPT);
					}
				});
			}
			return s;
		} else {
			return null;
		}
	}
	static class MessagesState {
		GentianBuddy buddy;
		String textLine;
	}
	@Override
	public void loadState(Object o) {
		try {
			MessagesState state = (MessagesState) o;
			if (state!=null) {
				if (state.buddy!=null) {
					startConversation(state.buddy, main.contacts.getLog(state.buddy.getUser()));
				}
				if (state.textLine!=null) {
					sendText.setText(state.textLine);
				}
			}
		} catch (Exception e) {
			
		}
	}
	@Override
	public Object saveState() {
		MessagesState state = new MessagesState();
		state.buddy=currentBuddy;
		state.textLine=sendText.getText().toString();
		return state;
	}
	private String encrypt(GentianBuddy buddy, String text) {
		/*if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {*/
			String s= CryptShort.encrypt(buddy,text);
			staticmain.config.saveConfig();
			if (buddy==currentBuddy) {
				updateSecrecyStatus();;
			}			
			return s;
		/*} else {
			return CryptLong.encrypt(buddy, text);
		}*/
	}
	public static String decrypt(GentianBuddy buddy,String text) {
		/*if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {*/
			String s= CryptShort.decrypt(buddy.getAccount(),buddy,text);
			staticmain.config.saveConfig();
			
			return s;
		/*} else {
			return CryptLong.decrypt(buddy, text);
		}*/
	}
	
}
