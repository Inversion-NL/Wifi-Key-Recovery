package nl.inversion.wifiKeyRecovery.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import nl.inversion.wifiKeyRecovery.R;
import nl.inversion.wifiKeyRecovery.ui.MyAlertBox;

public class UsefulBits {
	final String TAG =  this.getClass().getName();
	private Context mContext;

	public UsefulBits(Context context) {
		mContext = context;
	}

    public static String getLocaleFormattedDate(Calendar calendar) {
        SimpleDateFormat df = new SimpleDateFormat(); //called without pattern
        return df.format(calendar.getTime());
    }

	public String getAppVersion(){
		PackageInfo pi;
		try {
			pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}

	}

	public boolean isActivityAvailable(String packageName, String className) {
		final PackageManager packageManager = mContext.getPackageManager();
		final Intent intent = new Intent();
		intent.setClassName(packageName, className);

		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

		if (list.size() > 0) {
			Log.d(TAG, "Activity exists:" + className);
		}

		return list.size() > 0;
	}

	public boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);

        List<ResolveInfo> resolveInfo =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo.size() > 0) {
            Log.d(TAG, "Activity exists:" + action);
			return true;
		}
		return false;
	}

	public void saveToFile(String fileName, File directory, String contents) {
		Log.d(TAG, "Saving file.");

		if (android.os.Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED)){
			try {

				if (directory.canWrite()){

					File file = new File(directory, fileName);
					FileWriter fileWriter = new FileWriter(file);
					BufferedWriter out = new BufferedWriter(fileWriter);

					out.write(contents);
					out.close();

					Log.d(TAG, "Saved to SD as '"
                            + directory.getAbsolutePath()
                            + "/"
                            + fileName + "'");
                    Toast.makeText(mContext, mContext.getString(R.string.text_saved_to_SD)
                            + " '" + directory.getAbsolutePath()
                            + "/" + fileName
                            + "'", Toast.LENGTH_LONG).show();
				} else {
                    Log.e(TAG, "Unable to write directory");
                }

			} catch (IOException e) {
                Toast.makeText(mContext, mContext.getString(R.string.text_could_not_write_file),Toast.LENGTH_LONG).show();
                Log.e(TAG, "Could not write file " + e.getMessage());
                e.printStackTrace();
            }

        } else {
            Toast.makeText(mContext, mContext.getString(R.string.text_no_SD_card_mounted),
                    Toast.LENGTH_LONG).show();
			Log.e(TAG, "No SD card is mounted.");
		}
	}

	public void showAboutDialogue(int sdkInt){
		String title = mContext.getString(R.string.app_name) + " v"+ getAppVersion();

        int textColor;
        if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
            textColor = R.color.default_text_color_dark;
        } else {
            textColor = R.color.default_text_color_light;
        }

		StringBuilder sb = new StringBuilder();

		sb.append(mContext.getString(R.string.app_changelog));

		MyAlertBox.create(mContext,
                sb.toString(),
                title,
                mContext.getString(android.R.string.ok),
                textColor)
                .show();
	}

	public void ShowAlert(String title, String text, String button) {

		if (button.equals("")) button = mContext.getString(android.R.string.ok);

		try {
			AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
			ad.setTitle(title);
			ad.setMessage(text);

			ad.setPositiveButton( button, null );
			ad.show();
		} catch (Exception e){
			Log.e(TAG, "ShowAlert()");
            e.printStackTrace();
		}
	}

	public void showApplicationMissingAlert(
            String title,
            String message,
            String button1Text,
            final String marketUri){
		if (button1Text.equals("")){button1Text = mContext.getString(android.R.string.ok);}

		try{
			// Create the dialog box
			AlertDialog.Builder alertBox = new AlertDialog.Builder(mContext);

			alertBox.setTitle(title);
			alertBox.setMessage(message);

			alertBox.setPositiveButton(button1Text, null);
			alertBox.setNegativeButton(mContext.getString(R.string.text_playStore), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(marketUri));
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error opening play store: ");
                        e.printStackTrace();
                        ShowAlert(
                                mContext.getString(R.string.text_error),
                                mContext.getString(R.string.text_could_not_go_to_play_store),
                                mContext.getString(android.R.string.ok));
                    }
                }
            });

			alertBox.show();

		} catch (Exception e){
            e.printStackTrace();
			Log.e(TAG, "showApplicationMissingAlert()");
		}
	}

	public String listToString(List<?> list) {
		StringBuilder sb = new StringBuilder();
		int cnt = 0;

        if (list != null) {
            for (Object obj : list) {
                cnt += 1;
                sb.append("#" + cnt + ":\n");
                sb.append(obj + "\n");
            }
            return sb.toString();
        } else {
            Log.e(TAG, "Could not convert list to string: List was null");
            return " ";
        }
	}

	public int dipToPixels(int dip) {
		int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				(float) dip, mContext.getResources().getDisplayMetrics());
		return value;
	}
}