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
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class ExitDialog {

	public ExitDialog(final GentianChat main) {
		final Dialog viewDialog = new Dialog(main);
		viewDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		viewDialog.setTitle("Exit Gentian");
		Context context = main;
		LinearLayout dialogView = new LinearLayout(context);
		dialogView.setOrientation(LinearLayout.VERTICAL);
		final TextView description = new TextView(context);
		description
				.setText("Do you want to keep service running? This way you will receive notifications of new messages.");
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
		final CheckBox keepService = new CheckBox(context);
		keepService.setText("Keep service running");
		keepService.setChecked(true);
		center.addView(keepService);
		Button okButton = new Button(context);
		okButton.setText(R.string.buttons_ok);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager inputMethodManager = (InputMethodManager) main
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (!keepService.isChecked()) {
					main.stopService();
				}
				main.finish();
				main.removeDialog(viewDialog);
				viewDialog.cancel();

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