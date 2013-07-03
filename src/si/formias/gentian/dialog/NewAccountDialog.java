package si.formias.gentian.dialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import si.formias.gentian.GentianChat;
import si.formias.gentian.R;
import si.formias.gentian.Util;
import si.formias.gentian.xml.config.GentianAccount;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class NewAccountDialog {

	public static abstract class CallBack {
		public abstract void serverChosen(String name, String host, int port);
	}

	public volatile DialogLauncher launcher;

	public NewAccountDialog(final GentianChat main, final CallBack callback) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle(R.string.newaccount_title);
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description.setText(R.string.newaccount_description);
		dialogView.addView(description, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout center = new LinearLayout(context);
		center.setOrientation(LinearLayout.VERTICAL);
		ScrollView scrollCenter = new ScrollView(context);
		scrollCenter.addView(center);
		dialogView.addView(scrollCenter, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);

		FileInputStream fin;
		try {
			fin = main.openFileInput("server.txt");

			String reply = Util.readStream(fin, "utf-8");
			StringTokenizer st = new StringTokenizer(reply, "\n");

			Set<String> alreadyRegistered = new LinkedHashSet<String>();
			for (GentianAccount acc : main.config.configData.accounts) {
				alreadyRegistered.add(acc.getServer() + ":" + acc.getPort());
			}

			while (st.hasMoreTokens()) {
				final String name = st.nextToken();
				final String target = st.nextToken();
				if (alreadyRegistered.contains(target))
					continue;
				Button okButton = new Button(context);

				okButton.setText(name + "\n" + target);

				okButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						InputMethodManager inputMethodManager = (InputMethodManager) main
								.getSystemService(Context.INPUT_METHOD_SERVICE);

						if (inputMethodManager != null)
							inputMethodManager.hideSoftInputFromWindow(
									description.getWindowToken(), 0);
						int pos = target.indexOf(":");
						main.removeDialog(viewDialog);
						viewDialog.dismiss();
						callback.serverChosen(name, target.substring(0, pos),
								Integer.parseInt(target.substring(pos + 1)));

					}

				});
				center.addView(okButton, new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				if (launcher!=null) main.dismissDialogLauncher(launcher);
			}
		});
		viewDialog.setOnCancelListener(new Dialog.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (launcher!=null) main.dismissDialogLauncher(launcher);
				
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
