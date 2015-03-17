package nl.inversion.wifiKeyRecovery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import nl.inversion.wifiKeyRecovery.adapters.NetInfoAdapter;
import nl.inversion.wifiKeyRecovery.containers.NetInfo;
import nl.inversion.wifiKeyRecovery.containers.SavedData;
import nl.inversion.wifiKeyRecovery.ui.MyAlertBox;
import nl.inversion.wifiKeyRecovery.util.ExecTerminal;
import nl.inversion.wifiKeyRecovery.util.ExecuteThread;
import nl.inversion.wifiKeyRecovery.util.UsefulBits;

public class MainActivity extends Activity implements OnItemClickListener {

    private static final Boolean debug          = true;
    private static final String CLIPBOARD_LABEL = "WifiCode";

    private Drawable mIconOpenSearch;
    private Drawable mIconCloseSearch;
    private MenuItem mSearchActionMenuItem;
    private EditText mSearchEt;
    private String mSearchQuery;
    private Boolean mSearchOpened   = false;
    private Menu menu;

    private static final int ID_COPY_PASSWORD	= 0;
	private static final int ID_COPY_ALL   		= 1;
	private static final int ID_SHOW_QR_CODE    = 2;

	private static final int DIALOG_GET_PASSWORDS = 1;

    private static final String INTENT_EXPORT_NAME_INFO = "info";
    private static final String INTENT_EXPORT_NAME_TIME = "time";

	final String TAG =  this.getClass().getName();
    int sdkInt;

    // Setting the background color at runtime for lollipop devices
    private int MATERIAL_TEXT_BACKGROUND_COLOR = R.color.material_grey_300;

	private Bundle mThreadBundle;
	private EditText mEditFilter;
	private ExecuteThread mExecuteThread;
	private ListView mList;
	private NetInfoAdapter mNiAdapter;
	private ProgressDialog mExecuteDialog;
	private String mTimeDate="";
	private TextView mLabelDevice;
	private TextView mLabelTimeDate;
	private TextView mTextViewResultCount;
	private UsefulBits mUsefulBits;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mUsefulBits = new UsefulBits(this);

        //setup GUI
        mList = (ListView) findViewById(R.id.list);
        mLabelTimeDate = (TextView) findViewById(R.id.tvTime_value);
        mLabelDevice = (TextView) findViewById(R.id.tvDevice_value);
        mTextViewResultCount = (TextView) findViewById(R.id.tvResults);
        mEditFilter = (EditText) findViewById(R.id.edit_search);

        mList.setFastScrollEnabled(true);
        mList.setDivider( null );
        mList.setDividerHeight(mUsefulBits.dipToPixels(1));
        mList.setOnItemClickListener(this);

        sdkInt = android.os.Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {

            mIconOpenSearch = getDrawable(R.drawable.ic_action_search);
            mIconCloseSearch = getDrawable(R.drawable.ic_delete);

            // Change background color for lollipop devices
            TableLayout mTop_bar = (TableLayout) findViewById(R.id.top_bar);
            RelativeLayout mBottom_bar = (RelativeLayout) findViewById(R.id.bottom_bar);

            mTop_bar.setBackgroundColor(getResources().getColor(MATERIAL_TEXT_BACKGROUND_COLOR));
            mBottom_bar.setBackgroundColor(getResources().getColor(MATERIAL_TEXT_BACKGROUND_COLOR));

        } else if (sdkInt > Build.VERSION_CODES.HONEYCOMB) {

            // Use getDrawable which is deprecated since Lollipop
            // Only used for devices with Honeycomb or newer
            mIconOpenSearch = getResources().getDrawable(R.drawable.ic_action_search);
            mIconCloseSearch = getResources().getDrawable(R.drawable.ic_delete);

        } else if (sdkInt < Build.VERSION_CODES.HONEYCOMB) {


        }

        populateInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditFilter.removeTextChangedListener(filterTextWatcher);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mEditFilter != null){
            mEditFilter.addTextChangedListener(filterTextWatcher);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditFilter.removeTextChangedListener(filterTextWatcher);
    }

    @Override
    public void onBackPressed() {

        // First close search, if open, when the back key is pressed
        if (mSearchOpened) closeSearchBar();
        else super.onBackPressed();
    }

    /** Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);

        // Used for changing menu items programmatically
        this.menu = menu;

        if (sdkInt < Build.VERSION_CODES.HONEYCOMB) {

            // Devices older than Honeycomb use a search view between header and list view
            hideMenuOption(R.id.action_search);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchActionMenuItem = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    /** Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:
                mUsefulBits.showAboutDialogue();
                return true;

            case R.id.menu_export:
                Intent myIntent = new Intent();
                String export_text = "";

                export_text += getString(R.string.label_wifi_passwords) + "\n";
                export_text += mUsefulBits.listToString((List<?>) mList.getTag()) + "\n\n";
                export_text += mTextViewResultCount.getText();

                myIntent.putExtra(INTENT_EXPORT_NAME_INFO, export_text);
                myIntent.putExtra(INTENT_EXPORT_NAME_TIME, mTimeDate);
                myIntent.setClass(this, ExportActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.menu_refresh:
                refreshInfo();
                return true;

            case R.id.action_search:
                if (mSearchOpened) {
                    closeSearchBar();
                } else {
                    openSearchBar(mSearchQuery);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void openSearchBar(String queryText) {

        // Taken from: http://blog.lovelyhq.com/implementing-a-live-list-search-in-android-action-bar/

        if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
            // Set custom view on action bar.
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(R.layout.search_bar);

            // Search edit text field setup.
            mSearchEt = (EditText) actionBar.getCustomView().findViewById(R.id.etSearch);
            mSearchEt.addTextChangedListener(filterTextWatcher);
            mSearchEt.setText(queryText);
            mSearchEt.requestFocus();
            mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        hideSoftKeyboard();
                        return true;
                    }
                    return false;
                }
            });

            // Change search icon accordingly.
            mSearchActionMenuItem.setIcon(mIconCloseSearch);
            mSearchOpened = true;

            showSoftKeyboard();

        } else {
            // Should be called since the search button is hidden for devices
            // older then Honeycomb
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void closeSearchBar() {

        if (sdkInt >= Build.VERSION_CODES.HONEYCOMB) {
            // Remove custom view.
            getActionBar().setDisplayShowCustomEnabled(false);

            // Change search icon accordingly.
            mSearchActionMenuItem.setIcon(mIconOpenSearch);
            mSearchOpened = false;
            onClearSearchClick();
        }
    }

    private void hideMenuOption(int id) {

        MenuItem item = menu.findItem(id);
        item.setVisible(false);

    }

    /** Retrieves and displays info */
    private void populateInfo(){
        final Object data = getLastNonConfigurationInstance();

        if (data == null) { // We need to do everything from scratch!

            Calendar calendar = Calendar.getInstance();
            mTimeDate = mUsefulBits.getLocaleFormattedDate(calendar);
            mLabelTimeDate.setText(mTimeDate);
            getPasswords();

        } else {

            final SavedData saved = (SavedData) data;
            mTimeDate = saved.getDateTime();
            mSearchQuery = saved.getSearchQuery();
            mSearchOpened = saved.getIsSearchBarOpen();
            mSearchActionMenuItem = saved.getSearchActionMenuItem();
            if (mSearchOpened) openSearchBar(mSearchQuery);
            // else closeSearchBar();

            mLabelTimeDate.setText(mTimeDate);
            populateList(saved.getWifiPasswordList());
            mList.setTag(saved.getWifiPasswordList());
        }
        mLabelDevice.setText(
                android.os.Build.PRODUCT + ", " +
                        android.os.Build.DEVICE + ", " +
                        android.os.Build.MODEL);
    }

    private void populateList(List<NetInfo> netInfoList){

        if(netInfoList.size() > 0 ){

            // Only set visible for devices older than Honeycomb
            if (sdkInt < Build.VERSION_CODES.HONEYCOMB)
                findViewById(R.id.filter_segment).setVisibility(View.VISIBLE);

            mNiAdapter = new NetInfoAdapter(this, netInfoList);
            mTextViewResultCount.setText(String.valueOf(netInfoList.size()));
            mList.setAdapter(mNiAdapter);
            mEditFilter.addTextChangedListener(filterTextWatcher);

        } else {

            mTextViewResultCount.setText("0");

            // Only hide for devices older than Honeycomb
            // since it's hidden by default in xml so no need to set visibility
            // to gone for these devices
            if (sdkInt < Build.VERSION_CODES.HONEYCOMB)
                findViewById(R.id.filter_segment).setVisibility(View.GONE);
        }
    }

    /** Convenience function combining clearInfo and getInfo */
    public void refreshInfo() {
        clearInfo();
        populateInfo();
    }

    /** Clears the table and field contents */
	public void clearInfo() {
		mLabelTimeDate.setText("");
	}

	private void getPasswords(){
		LockScreenRotation();
		ExecTerminal et = new ExecTerminal();

		if(et.checkSu()){
			showDialog(DIALOG_GET_PASSWORDS);
		} else {

			AlertDialog dlg = MyAlertBox.create(this, getString(R.string.root_needed), getString(R.string.app_name), getString(android.R.string.ok));
			dlg.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					if (!debug) MainActivity.this.finish();
                    else showDebugWarningDialog();
				}
			});
			dlg.show();
		}
	}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void copyStringToClipboard(String text){

        if (text.length() > 0) {

            String msgText;
            if (text.length()>150) {
                msgText = text.substring(0, 150) + "...";
            } else {
                msgText = text;
            }
            String message = "'" + msgText + "' " + getString(R.string.text_copied);

            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard =
                        (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setText(text);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            } else {
                android.content.ClipboardManager clipMan =
                        (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(CLIPBOARD_LABEL, text);
                clipMan.setPrimaryClip(clip);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Sets screen rotation as fixed to current rotation setting
	private void LockScreenRotation(){
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation)
		{
		case Configuration.ORIENTATION_PORTRAIT:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		}
	}

	public void onClearSearchClick(){
		mEditFilter.setText("");
	}

    private void showDebugWarningDialog() {
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
        actionDialog.setTitle("Debugging")
                .setMessage("Debugging is enabled in code!")
                .setPositiveButton(android.R.string.ok, null);

        AlertDialog alert = actionDialog.create();
        alert.show();
    }

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_GET_PASSWORDS:
			mExecuteDialog = new ProgressDialog(this);
			mExecuteDialog.setMessage(getString(R.string.dialogue_text_please_wait));

			mExecuteThread = new ExecuteThread(handler, this, mThreadBundle);
			mExecuteThread.start();
			return mExecuteDialog;
		default:
			return null;
		}
	}

    /**
     * This shows the soft keyboard
     */
    private void showSoftKeyboard() {

        // Start soft keyboard
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * This hides the soft keyboard
     */
    private void hideSoftKeyboard() {

        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mSearchEt.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    String text;
	public void onItemClick(AdapterView<?> l, final View v, int position, long id){

        AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
        actionDialog.setTitle("Action");
        actionDialog.setItems(R.array.singleItemActions, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {

                final NetInfo ni = (NetInfo) v.getTag();

                switch (position) {
                    case ID_COPY_ALL:
                        copyStringToClipboard(ni.toString());
                        break;
                    case ID_COPY_PASSWORD:
                        copyStringToClipboard(ni.getPassword());
                        break;
                    case ID_SHOW_QR_CODE:
                        text = ni.getQrcodeString();

                        if (text.length() > 0) {
                            if (mUsefulBits.isIntentAvailable(MainActivity.this, "com.google.zxing.client.android.ENCODE")){
                                Intent i = new Intent();
                                i.setAction("com.google.zxing.client.android.ENCODE");
                                i.putExtra ("ENCODE_TYPE", "TEXT_TYPE");
                                i.putExtra ("ENCODE_DATA", text);
                                startActivity(i);
                            } else {
                                mUsefulBits.showApplicationMissingAlert(
                                        getString(R.string.component_missing),
                                        getString(R.string.you_need_the_barcode_scanner_application),
                                        getString(R.string.dismiss),
                                        getString(R.string.zxing_market_url));
                            }
                        }
                        break;
                }
            }

        });
        AlertDialog alert = actionDialog.create();
        alert.show();

	}

    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {}

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

            // Save search query for orientation changes
            mSearchQuery = s.toString();

            if(mNiAdapter != null){
                mNiAdapter.getFilter().filter(s);
            } else {
                Log.w(TAG, "TextWatcher: Adapter is null!");
            }
        }
    };

    final Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch(msg.what){

                case ExecuteThread.WORK_COMPLETED:
                    Log.d(TAG, "Worker Thread: WORK_COMPLETED");
                    List<NetInfo> l = new ArrayList<NetInfo>();

                    l = (ArrayList<NetInfo>) msg.getData().getSerializable("passwords");

                    if (l != null){
                        Collections.sort(l, new NetInfoComperator());
                        populateList(l);
                        mList.setTag(l);
                    }

                    mExecuteThread.setState(ExecuteThread.STATE_DONE);
                    removeDialog(DIALOG_GET_PASSWORDS);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                case ExecuteThread.WORK_INTERUPTED:
                    mExecuteThread.setState(ExecuteThread.STATE_DONE);
                    removeDialog(DIALOG_GET_PASSWORDS);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    break;
            }

        }
    };

	@SuppressWarnings("unchecked")
	@Override
	public Object onRetainNonConfigurationInstance() {

		Log.d(TAG, "onRetainNonConfigurationInstance()");

        // TODO create non deprecated method to retain config data
        // like used here http://blog.lovelyhq.com/implementing-a-live-list-search-in-android-action-bar/

		final SavedData saved = new SavedData();

		if(mList.getTag() != null){
			saved.setWiFiPasswordList((List<NetInfo>) mList.getTag());
		}

		saved.setDateTime(mTimeDate);
        saved.setSearchQuery(mSearchQuery);
        saved.setIsSearchBarOpen(mSearchOpened);
        saved.setSearchActionMenuItem(mSearchActionMenuItem);

		return saved;
	}

	public class NetInfoComperator implements Comparator<NetInfo> {
		@Override
		public int compare(NetInfo o1, NetInfo o2) {
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	}
}