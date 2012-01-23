package si.formias.gentian.dialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.StringTokenizer;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.Util;
import si.formias.gentian.xml.config.GentianAccount;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class NewContactAddDialog {
	EditText nickEdit;
	EditText phoneEdit;
	public static abstract class CallBack {
		public abstract void newAccountChosen(String host, int port,String nick);
		public abstract void existingAccountChosen(GentianAccount acc,String nick);
		public abstract void smsContact(GentianAccount acc, String string, String string2);
	}

	public NewContactAddDialog(final GentianChat main, final CallBack callback,List<GentianAccount> existingAccounts,final String host,final int port,String nick) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.newcontactadd_title);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description
				.setText(R.string.newcontactadd_description);
		dialogView.addView(description, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout center =new LinearLayout(context);
		center.setOrientation(LinearLayout.VERTICAL);
		ScrollView scrollCenter=new ScrollView(context);
		scrollCenter.addView(center);
		nickEdit=new EditText(context);
		phoneEdit=new EditText(context);
		phoneEdit.setInputType(InputType.TYPE_CLASS_PHONE);

		if (nick!=null) {
			nickEdit.setText(nick);
		}
		TextView nickLabel =new TextView(context);
		nickLabel.setText(R.string.text_nick);
		LinearLayout nickLayout=new LinearLayout(context);
		nickLayout.setOrientation(LinearLayout.HORIZONTAL);
		nickLayout.addView(nickLabel);
		LinearLayout.LayoutParams parmsNickEdit=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		parmsNickEdit.weight=1;
		nickLayout.addView(nickEdit,parmsNickEdit);
		dialogView.addView(nickLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		if (host.equals(GentianAccount.SMS)) {
			TextView phoneLabel =new TextView(context);
			phoneLabel.setText(R.string.text_phone);
			LinearLayout phoneLayout=new LinearLayout(context);
			phoneLayout.setOrientation(LinearLayout.HORIZONTAL);
			phoneLayout.addView(phoneLabel);
			
			
			phoneLayout.addView(phoneEdit,parmsNickEdit);
			Button browse=new Button(context);
			browse.setText(R.string.text_browse);
			if (Build.VERSION.SDK_INT>=5) {
			phoneLayout.addView(browse);
			browse.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					main.doLaunchContactPicker(NewContactAddDialog.this);
					
				}
			});
			}
			
			dialogView.addView(phoneLayout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));	
		}
		
		dialogView.addView(scrollCenter, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams parmsButton = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		parmsButton.weight = 1;
		for (final GentianAccount acc : existingAccounts) {
				

				Button okButton = new Button(context);

				okButton.setText(acc.toString());

				okButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						InputMethodManager inputMethodManager = (InputMethodManager) main
								.getSystemService(Context.INPUT_METHOD_SERVICE);

						if (inputMethodManager != null)
							inputMethodManager.hideSoftInputFromWindow(
									description.getWindowToken(), 0);
						
						main.removeDialog(viewDialog);
						viewDialog.dismiss();
						if (acc.getServer().equals(GentianAccount.SMS)) {
							callback.smsContact(acc,nickEdit.getText().toString(),phoneEdit.getText().toString());
						} else {
						callback.existingAccountChosen(acc,nickEdit.getText().toString());
						}
						//callback.newAccountChosen(name,target.substring(0,pos),Integer.parseInt(target.substring(pos+1)));
							
							
						
					}
					
				});
				if (acc.getServer().equals(GentianAccount.SMS)) {
					okButton.setText(R.string.newcontactadd_title);
					buttonsLayout.addView(okButton, parmsButton);
				} else {
					center.addView(okButton,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				}
		}
		Button createButton = new Button(context);
		createButton.setText(R.string.newcontactadd_newaccount);
		if (existingAccounts.size()>0) {
			createButton.setVisibility(View.GONE);
		}
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);

				if (inputMethodManager != null)
					inputMethodManager.hideSoftInputFromWindow(
							description.getWindowToken(), 0);
				
				main.removeDialog(viewDialog);
				viewDialog.dismiss();

				callback.newAccountChosen(host,port,nickEdit.getText().toString());
					
					
				
			}
			
		});

		Button cancelButton = new Button(context);
		cancelButton.setText(R.string.buttons_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				
				
				main.removeDialog(viewDialog);
				viewDialog.cancel();

			}
		});
		
		buttonsLayout.addView(createButton, parmsButton);
		buttonsLayout.addView(cancelButton, parmsButton);
		dialogView.addView(buttonsLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		viewDialog.setContentView(dialogView);
		main.addDialog(viewDialog);
		viewDialog.show();
	}
	public void addToNumber(String number) {
		phoneEdit.setText(number);
		System.out.println("Number set: "+number);
	}
	final char[] allowed={'0','1','2','3','4','5','6','7','8','9','+'};
		
	
	public void onContactNumberPicked(String[] number,String[] display) {
		
		System.out.println("On contact picked");
		if (number.length>0 && number[0]!=null) {
			
			StringBuilder sb=new StringBuilder();
			
			for (char c : number[0].toCharArray()) {
				for (char ok : allowed) {
					if (c==ok) {
						sb.append(c);
					}
				}
			}
			addToNumber(sb.toString());
			nickEdit.setText(display[0].trim());
		}
	
	}

}
