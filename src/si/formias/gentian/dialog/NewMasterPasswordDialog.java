package si.formias.gentian.dialog;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;


import android.app.Dialog;
import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class NewMasterPasswordDialog {
	final int minlength=6;
	public static abstract class CallBack {
		public abstract void passwordSet(String password);
	}
	public NewMasterPasswordDialog (final GentianChat main, final CallBack callback) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.setCancelable(false);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.newpassword_title);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description.setText(String.format(main.getText(R.string.newpassword_description).toString(), minlength));
		final EditText pass = new EditText(context);
		final EditText pass2 = new EditText(context);
		PasswordTransformationMethod passTransform = new PasswordTransformationMethod();
		pass.setTransformationMethod(passTransform);
		pass .setImeOptions(pass.getImeOptions()|EditorInfo.IME_FLAG_NO_EXTRACT_UI );
		pass2.setImeOptions(pass2.getImeOptions()|EditorInfo.IME_FLAG_NO_EXTRACT_UI );
		pass2.setTransformationMethod(passTransform);
		dialogView.addView(description, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		dialogView.addView(pass, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		dialogView.addView(pass2, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
		Button okButton = new Button(context);
		
			okButton.setText(R.string.newpassword_button_set);
		
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				
				if (inputMethodManager != null)
					inputMethodManager.hideSoftInputFromWindow(pass
							.getWindowToken(), 0);
				if (!pass.getText().toString().equals(pass2.getText().toString())) {
					main.displayNotice(main.getText(R.string.newpassword_passwords_do_not_match).toString(),null);
				} else if (pass.getText().length()<minlength) {
					main.displayNotice(String.format(main.getText(R.string.newpassword_password_too_short).toString(),minlength),null);
				} else {
					inputMethodManager.hideSoftInputFromWindow(description
							.getWindowToken(), 0);
					main.removeDialog(viewDialog);
					viewDialog.dismiss();
					main.displayNotice(main.getText(R.string.newpassword_password_set).toString(),new Runnable() {
						public void run() {
							callback.passwordSet(pass.getText().toString());
						}
					});
				}
				//callback.dialogSubmit(true, name.getText().toString().trim());

			}
		});
		Button cancelButton = new Button(context);
		cancelButton.setText(R.string.buttons_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (inputMethodManager != null)
					inputMethodManager.hideSoftInputFromWindow(pass
							.getWindowToken(), 0);
				main.finish();
				main.removeDialog(viewDialog);
				viewDialog.cancel();
				

			}
		});
		LinearLayout.LayoutParams parmsButton = new LinearLayout.LayoutParams(
				0, LayoutParams.WRAP_CONTENT);
		parmsButton.weight = 1;
		buttonsLayout.addView(okButton, parmsButton);
		buttonsLayout.addView(cancelButton, parmsButton);
		dialogView.addView(buttonsLayout, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		viewDialog.setContentView(dialogView);
		main.addDialog(viewDialog);
		viewDialog.show();
	}
}
