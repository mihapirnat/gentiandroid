package si.formias.gentian.dialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import si.formias.gentian.CountryPhone;
import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.Util;
import si.formias.gentian.dialog.NewAccountDialog.CallBack;
import si.formias.gentian.xml.config.GentianAccount;
import si.formias.gentian.xml.config.GentianBuddy;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class EditContactDialog {
	EditText nickEdit;
	EditText phoneEdit;

	public EditContactDialog(final GentianChat main, final GentianBuddy buddy) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.editcontact);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description.setText(""/* R.string.newaccount_description */);
		dialogView.addView(description, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout center = new LinearLayout(context);
		center.setOrientation(LinearLayout.VERTICAL);
		ScrollView scrollCenter = new ScrollView(context);
		scrollCenter.addView(center);

		nickEdit = new EditText(context);
		phoneEdit = new EditText(context);
		phoneEdit.setInputType(InputType.TYPE_CLASS_PHONE);

		nickEdit.setText(buddy.getNick());

		TextView nickLabel = new TextView(context);
		nickLabel.setText(R.string.text_nick);
		LinearLayout nickLayout = new LinearLayout(context);
		nickLayout.setOrientation(LinearLayout.HORIZONTAL);
		nickLayout.addView(nickLabel);
		LinearLayout.LayoutParams parmsNickEdit = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmsNickEdit.weight = 1;
		nickLayout.addView(nickEdit, parmsNickEdit);
		dialogView.addView(nickLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		dialogView.addView(scrollCenter, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

		Button okButton = new Button(context);
		okButton.setText(R.string.buttons_ok);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				buddy.setTextOf(GentianBuddy.TARGET, phoneEdit.getText()
						.toString());
				buddy.setNick(nickEdit.getText().toString());
				if (GentianChat.CONVERTPHONENUMBERS
						&& buddy.getAccount().getServer()
								.equals(GentianAccount.SMS)
						&& buddy.getTarget() != null
						&& !buddy.getTarget().trim().startsWith("+")) {
					System.out.println("converting to international");
					TelephonyManager manager = (TelephonyManager) main
							.getSystemService(Context.TELEPHONY_SERVICE);
					/*
					 * System.out.println("Sim country:" +
					 * manager.getSimCountryIso
					 * ()+" Sim operator:"+manager.getSimOperator());
					 */
					buddy.setTextOf(
							GentianBuddy.TARGET,
							CountryPhone.prefixForOperator(manager
									.getSimOperator())
									+ buddy.getTarget().trim().substring(1));
					// System.out.println(CountryPhone.prefixForOperator(manager.getSimOperator())+buddy.getTarget().substring(1));

				}

				main.config.saveConfig();
				main.contacts.updateContacts();
				main.removeDialog(viewDialog);
				viewDialog.cancel();

			}
		});
		if (buddy.getAccount().getServer().equals(GentianAccount.SMS)) {
			TextView phoneLabel = new TextView(context);
			phoneLabel.setText(R.string.text_phone);
			phoneEdit.setText(buddy.textOf(GentianBuddy.TARGET));
			LinearLayout phoneLayout = new LinearLayout(context);
			phoneLayout.setOrientation(LinearLayout.HORIZONTAL);
			phoneLayout.addView(phoneLabel);

			phoneLayout.addView(phoneEdit, parmsNickEdit);
			Button browse = new Button(context);
			browse.setText(R.string.text_browse);
			if (Build.VERSION.SDK_INT >= 5) {
				// phoneLayout.addView(browse);
				browse.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// main.doLaunchContactPicker(NewContactAddDialog.this);

					}
				});
			}

			dialogView.addView(phoneLayout, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
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
		Button deleteButton = new Button(context);
		deleteButton.setText(R.string.buttons_delete);
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				main.askYesNo(main.getText(R.string.confirm_delete_contact)
						.toString(), new Runnable() {
					public void run() {
						buddy.getAccount().remove(buddy);
						main.config.saveConfig();
						main.contacts.updateContacts();
						InputMethodManager inputMethodManager = (InputMethodManager) main
								.getSystemService(Context.INPUT_METHOD_SERVICE);

					}
				});
				main.removeDialog(viewDialog);
				viewDialog.cancel();

			}
		});
		LinearLayout.LayoutParams parmsButton = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmsButton.weight = 1;
		buttonsLayout.addView(okButton, parmsButton);
		buttonsLayout.addView(cancelButton, parmsButton);
		buttonsLayout.addView(deleteButton, parmsButton);
		dialogView.addView(buttonsLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		viewDialog.setContentView(dialogView);
		main.addDialog(viewDialog);
		viewDialog.show();
	}
}
