package nl.inversion.wifiKeyRecovery.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.inversion.wifiKeyRecovery.R;

public class MyAlertBox {

	 public static AlertDialog create(Context context, String text, String title, String button) {
	  return new AlertDialog.Builder(context)
	   .setTitle(title)
	   .setCancelable(true)
	   .setIcon(android.R.drawable.ic_dialog_info)
	   .setPositiveButton(button, null)
	   .setView(LinkifyText(context, text))
	   .create();
	 }

		public static ScrollView LinkifyText(Context context, String message)
		{
			final ScrollView svMessage = new ScrollView(context);
			final TextView tvMessage = new TextView(context);

		    final SpannableString spanText = new SpannableString(message);

		    Linkify.addLinks(spanText, Linkify.ALL);
		    tvMessage.setText(spanText);
		    tvMessage.setTextColor(context.getResources().getColor(R.color.default_text_color_light));
		    tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

		    svMessage.setPadding(14, 2, 10, 12);
		    svMessage.addView(tvMessage);

		    return svMessage;
		}

}
