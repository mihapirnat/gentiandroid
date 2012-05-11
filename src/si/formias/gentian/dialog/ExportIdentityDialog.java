package si.formias.gentian.dialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.Util;
import si.formias.gentian.xml.config.GentianAccount;

import com.google.zxing.integration.android.IntentIntegrator;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.ClipboardManager;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ExportIdentityDialog {

	public ExportIdentityDialog(final GentianChat main) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.exportidentity_title);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description
				.setText(R.string.exportidentity_description);
		dialogView.addView(description, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout center =new LinearLayout(context);
		center.setOrientation(LinearLayout.VERTICAL);
		ScrollView scrollCenter=new ScrollView(context);
		scrollCenter.addView(center);
		dialogView.addView(scrollCenter, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

		FileInputStream fin;
System.out.println("main:"+main);
System.out.println("main.config:"+main.config);
System.out.println("main.config.configData:"+main.config.configData);
		for (final GentianAccount acc : main.config.configData.accounts) {

				Button okButton = new Button(context);
				okButton.setBackgroundColor(Color.TRANSPARENT);
				ImageView icon=new ImageView(context);
				String urltext=null;
				boolean exportKey=false;
				if (acc.getServer().equals(GentianAccount.SMS)) {
					okButton.setText("SMS");
					icon.setImageResource(R.drawable.smscontact);
					exportKey=true;
				} else {
					okButton.setText(acc.getUser()+"@"+acc.getServer()+":"+acc.getPort());
					icon.setImageResource(R.drawable.torcontact);
				}
				final String url="gentian://"+acc.getServer()+":"+acc.getPort()+"/"+acc.getUser().replace("+","-").replace("/", "|")+(exportKey?"/"+acc.getCryptModulusString().replace("+","-").replace("/", "|")+"/"+acc.getCryptPublicExponentString().replace("+","-").replace("/", "|")+"/"+acc.getSignPublic().replace("+","-").replace("/", "|"):"");
				OnClickListener click =new OnClickListener() {

					@Override
					public void onClick(View v) {
						InputMethodManager inputMethodManager = (InputMethodManager) main
								.getSystemService(Context.INPUT_METHOD_SERVICE);

						if (inputMethodManager != null)
							inputMethodManager.hideSoftInputFromWindow(
									description.getWindowToken(), 0);
						
						IntentIntegrator
						.shareText(
								main,
								url);							
							
						
					}
					
				};
				okButton.setOnClickListener(click);
				View.OnLongClickListener longclick=new View.OnLongClickListener() {
					
					@Override
					public boolean onLongClick(View v) {
						 ClipboardManager ClipMan = (ClipboardManager) main.getSystemService(Context.CLIPBOARD_SERVICE);
						    ClipMan.setText(url);
						    main.displayNotice(main.getText(R.string.exportidentity_clipboard).toString());
						    return true;

					}
				};
				okButton.setOnLongClickListener(longclick);
				
				icon.setClickable(true);
				icon.setOnClickListener(click);
				icon.setOnLongClickListener(longclick);
			
				LinearLayout l = new LinearLayout(context);
				l.setOrientation(LinearLayout.HORIZONTAL);
				l.setBackgroundResource(R.drawable.buddies);
				l.addView(icon,new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
				l.addView(okButton,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				center.addView(l,new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}

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
		LinearLayout.LayoutParams parmsButton = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		parmsButton.weight = 1;
		
		buttonsLayout.addView(cancelButton, parmsButton);
		dialogView.addView(buttonsLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		viewDialog.setContentView(dialogView);
		main.addDialog(viewDialog);
		viewDialog.show();
	}
}
