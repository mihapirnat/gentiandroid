package si.formias.gentian;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.xml.sax.SAXException;
import si.formias.gentian.R;
import si.formias.gentian.GentianChat.MyThread;
import si.formias.gentian.dialog.ExitDialog;
import si.formias.gentian.dialog.ExportIdentityDialog;
import si.formias.gentian.dialog.NewAccountDialog;
import si.formias.gentian.http.HttpMagic;
import si.formias.gentian.xml.config.GentianAccount;

import com.google.zxing.integration.android.IntentIntegrator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TopButtons extends LinearLayout {
	public ToggleButton log,contacts,chats;
	CharSequence contactsText,messagesText;
	GentianChat chat;
	public Button createAccount;
	public TopButtons(final GentianChat gentianChat) {
		super(gentianChat);
		this.chat=gentianChat;
		Context context= gentianChat;
		setOrientation(LinearLayout.VERTICAL);
		LinearLayout first = new LinearLayout(context);
		first.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout.LayoutParams parms=new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT);
		parms.weight=1;
		LinearLayout.LayoutParams parmsExit=new LinearLayout.LayoutParams(0,LayoutParams.FILL_PARENT);
		parmsExit.weight=1;
		
		createAccount = new Button(context);
		if (gentianChat.landscape) {
			createAccount.setText(gentianChat.getText(R.string.top_create_account).toString().replace("\n"," "));
		} else {
			createAccount.setText(R.string.top_create_account);
		}
		createAccount.setTextColor(Color.WHITE);
		createAccount.setBackgroundResource(R.drawable.bluebutton);
		createAccount.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			  new NewAccountDialog(gentianChat, new NewAccountDialog.CallBack() {
				
				@Override
				public void serverChosen(String name,final String host, final int port) {
				gentianChat.registerAccount(host,port,null);
					
				}
			});
				
			}
		});
		Button addContact = new Button(context);
		addContact.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				IntentIntegrator.initiateScan(gentianChat, gentianChat.getText(R.string.app_name), "Add contact", "Yes", "No");
				
			}
		});
		if (gentianChat.landscape) {
			addContact.setText(gentianChat.getText(R.string.top_add_contact).toString().replace("\n"," "));
		} else {
			addContact.setText(R.string.top_add_contact);
		}
		addContact.setTextColor(Color.WHITE);
		addContact.setBackgroundResource(R.drawable.bluebutton);
		Button exportIdentity = new Button(context);

		if (gentianChat.landscape) {
			exportIdentity.setText(gentianChat.getText(R.string.top_export_identity).toString().replace("\n"," "));
		} else {
			exportIdentity.setText(R.string.top_export_identity);
		}
		exportIdentity.setTextColor(Color.WHITE);
		exportIdentity.setBackgroundResource(R.drawable.bluebutton);
		exportIdentity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			  new ExportIdentityDialog(gentianChat);
				
			}
		});
		
		
		Button exit = new Button(context);
		if (gentianChat.landscape) {
			exit.setText(gentianChat.getText(R.string.top_exit).toString().replace("\n"," "));
		} else {
			exit.setText(R.string.top_exit);
		}
		exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//new ExitDialog(gentianChat);
				gentianChat.wipe();
				gentianChat.finish();
				
			}
		});
		exit.setTextColor(Color.WHITE);
		exit.setBackgroundResource(R.drawable.bluebutton);
		first.addView(createAccount,parms);
		first.addView(addContact,parms);
		first.addView(exportIdentity,parms);
		first.addView(exit,parmsExit);
		
		addView(first,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		LinearLayout second=new LinearLayout(context);
		final List<ToggleButton> toggles=new ArrayList<ToggleButton>();
		contacts =new ToggleButton(context);
		contacts.setBackgroundResource(R.drawable.tabtoggle);
		contactsText=gentianChat.getText(R.string.top_contacts);
		contacts.setText(contactsText);
		contacts.setTextOn(contactsText);
		contacts.setTextOff(contactsText);
		contacts.setTextColor(Color.BLACK);
		contacts.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					for (ToggleButton t : toggles) {
						t.setChecked(false);
						t.setTypeface(Typeface.DEFAULT);
					}
					toggles.clear();
					toggles.add(contacts);
					gentianChat.center.addView(gentianChat.contacts);
					contacts.setEnabled(false);
					contacts.setTypeface(Typeface.DEFAULT_BOLD);
					
				} else {
					gentianChat.center.removeView(gentianChat.contacts);
					contacts.setEnabled(true);

				}
				
			}
		});
		second.addView(contacts,parms);
		messagesText=gentianChat.getText(R.string.top_messages);
		chats =new ToggleButton(context);
		chats.setBackgroundResource(R.drawable.tabtoggle);
		chats.setText(messagesText);
		chats.setTextOn(messagesText);
		chats.setTextOff(messagesText);
		chats.setTextColor(Color.BLACK);
		chats.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					for (ToggleButton t : toggles) {
						t.setChecked(false);
						t.setTypeface(Typeface.DEFAULT);
					}
					toggles.clear();
					toggles.add(chats);
					gentianChat.center.addView(gentianChat.messages);
					chats.setEnabled(false);
					chats.setTypeface(Typeface.DEFAULT_BOLD);
				} else {
					gentianChat.center.removeView(gentianChat.messages);
					chats.setEnabled(true);
				}
				
			}
		});
		second.addView(chats,parms);
		CharSequence logText=gentianChat.getText(R.string.top_log);
		log =new ToggleButton(context);
		log.setBackgroundResource(R.drawable.tabtoggle);
		log.setText(logText);
		log.setTextOn(logText);
		log.setTextOff(logText);
		log.setTextColor(Color.BLACK);
		log.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					for (ToggleButton t : toggles) {
						t.setChecked(false);
						t.setTypeface(Typeface.DEFAULT);
					}
					toggles.clear();
					toggles.add(log);
					gentianChat.center.addView(gentianChat.logScroll);
					log.setEnabled(false);
					log.setTypeface(Typeface.DEFAULT_BOLD);
				} else {
					gentianChat.center.removeView(gentianChat.logScroll);
					log.setEnabled(true);
				}
				
			}
		});
		second.addView(log,parms);
		
		contacts.setChecked(true);
		
		addView(second,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		// only sms for now :)
		createAccount.setVisibility(View.GONE);
		log.setVisibility(View.GONE);
	}
	public int getTabIndex() {
		if (contacts.isChecked()) {
			return 0;
		} else if (chats.isChecked()) {
			return 1;
		} else if (log.isChecked()) {
			return 2;
		}
		return -1;
	}
	public void setTabIndex(int index) {
		if (index==0) {
			contacts.setChecked(true);
		} else if (index==1) {
			chats.setChecked(true);
		} else if (index==2) {
			log.setChecked(true);
		}
	}
	public void updateContactsWaiting(int waiting) {
		String contactsText=chat.getText(R.string.top_contacts).toString();
		if (waiting>0) contactsText=contactsText+" ("+waiting+")";
		contacts.setText(contactsText);
		contacts.setTextOn(contactsText);
		contacts.setTextOff(contactsText);
		
	}
}
