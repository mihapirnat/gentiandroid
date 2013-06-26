package si.formias.gentian.dialog;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
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

public class OpenMasterPasswordDialog {
	public static abstract class CallBack {
		public abstract void passwordSet(String password, Runnable onSuccess,
				Runnable onFail);
	}

	final Dialog viewDialog;

	public OpenMasterPasswordDialog(final GentianChat main,
			final CallBack callback) {
		viewDialog = new Dialog(main);
		viewDialog.setCancelable(false);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.openpassword_title);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description.setText(R.string.openpassword_description);
		final EditText pass = new EditText(context);
		pass.setImeOptions(pass.getImeOptions()
				| EditorInfo.IME_FLAG_NO_EXTRACT_UI);
		PasswordTransformationMethod passTransform = new PasswordTransformationMethod();
		pass.setTransformationMethod(passTransform);
		final Handler handler = new Handler();
		dialogView.addView(description, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		dialogView.addView(pass, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));

		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
		final Button okButton = new Button(context);
		final Button cancelButton = new Button(context);

		okButton.setText(R.string.buttons_ok);

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(
						description.getWindowToken(), 0);
				okButton.setEnabled(true);
				cancelButton.setEnabled(true);
				pass.setEnabled(false);
				new Thread() {
					public void run() {

						callback.passwordSet(pass.getText().toString(),
								new Runnable() {
									public void run() {
										handler.post(new Runnable() {
											public void run() {
												main.removeDialog(viewDialog);
												viewDialog.dismiss();
											}
										});
									}
								}, new Runnable() {
									public void run() {
										handler.post(new Runnable() {
											public void run() {
												okButton.setEnabled(true);
												cancelButton.setEnabled(true);
												pass.setEnabled(true);
											}
										});
									}
								});

					}
				}.start();

			}

		});

		cancelButton.setText(R.string.buttons_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (inputMethodManager != null)
					inputMethodManager.hideSoftInputFromWindow(
							pass.getWindowToken(), 0);
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