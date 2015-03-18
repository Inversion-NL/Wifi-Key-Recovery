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

	 public static AlertDialog create(
                                         Context context,
                                         String message,
                                         String title,
                                         String buttonText,
                                         int textColor) {

	  return new AlertDialog.Builder(context)
	   .setTitle(title)
	   .setCancelable(true)
	   .setIcon(R.drawable.ic_stat_info)
	   .setPositiveButton(buttonText, null)
	   .setView(LinkifyText(context, message, textColor))
	   .create();
	 }

		public static ScrollView LinkifyText(Context context, String message, int textColor)
		{
			final ScrollView svMessage = new ScrollView(context);
			final TextView tvMessage = new TextView(context);

		    final SpannableString spanText = new SpannableString(message);

		    Linkify.addLinks(spanText, Linkify.ALL);
		    tvMessage.setText(spanText);
		    tvMessage.setTextColor(context.getResources().getColor(textColor));
		    tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

		    svMessage.setPadding(14, 2, 10, 12);
		    svMessage.addView(tvMessage);

		    return svMessage;
		}

}
