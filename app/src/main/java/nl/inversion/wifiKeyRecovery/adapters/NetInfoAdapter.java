package nl.inversion.wifiKeyRecovery.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import nl.inversion.wifiKeyRecovery.R;
import nl.inversion.wifiKeyRecovery.containers.NetInfo;

public class NetInfoAdapter extends BaseAdapter implements Filterable {

    private int sdkInt;

	private final Map<String, Integer> mAlphaIndexer;

	private List<NetInfo> mAllItems;
	private List<NetInfo> mSubItems;

	private final Context mContext;
	private String[] mSections;

	private Filter mFilter;

	public NetInfoAdapter(Context context, List<NetInfo> appsList) {
		super();

        sdkInt = android.os.Build.VERSION.SDK_INT;

		mSubItems = appsList;
		mAllItems = this.mSubItems;

		mContext = context;
		mAlphaIndexer = new HashMap<>();

		prepareIndexer();
	}

	private void prepareIndexer() {
		int size = mSubItems.size();
		String title;
		String c;

		for (int i = size - 1; i >= 0; i--) {
			title = mSubItems.get(i).getQrSsid();

			try {
				Integer.valueOf(title.substring(0, 1));
				c = "#";
			} catch (NumberFormatException e) {
				c = title.toUpperCase().substring(0, 1);
			}

			mAlphaIndexer.put(c, i);
		}

		final Set<String> keys = mAlphaIndexer.keySet();
		final Iterator<String> it = keys.iterator();
		final List<String> keyList = new ArrayList<>();

		while (it.hasNext()) {
			keyList.add(it.next());
		}

		Collections.sort(keyList);

		mSections = new String[keyList.size()];
		keyList.toArray(mSections);
	}

	public int getCount() {
		return mSubItems.size();
	}

	public NetInfo getItem(int position) {
		return mSubItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final NetInfo event = mSubItems.get(position);

		if (convertView == null) {
			final LayoutInflater vi =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (sdkInt > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // Material design list item for devices with ICS and newer
                convertView = vi.inflate(R.layout.list_item_material, null);
            } else {
                convertView = vi.inflate(R.layout.list_item, null);
            }
		}

		if (event != null) {
			final TextView text = (TextView) convertView.findViewById(R.id.text);

            if (sdkInt < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // Only background on devices older then ICS
                if (position % 2 == 0) {
                    convertView.setBackgroundResource(R.drawable.row_background_light);
                } else {
                    convertView.setBackgroundResource(R.drawable.row_background_dark);
                }
            }

			text.setText(event.toString());
			convertView.setTag(event);
		}

		return convertView;
	}

	public int getPositionForSection(int section) {
		return mAlphaIndexer.get(mSections[section]);
	}

	public int getSectionForPosition(int position) {
		return 0;
	}

	public Object[] getSections() {
		return mSections;
	}

	public Filter getFilter()
	{
		if (mFilter == null) {
			mFilter = new ProoferFilter();
		}
		return mFilter;
	}

	/**
	 * Custom Filter implementation for the items adapter.
	 *
	 */
	private class ProoferFilter extends Filter {

		@Override
		protected void publishResults(CharSequence prefix, FilterResults results) {

			// NOTE: this function is *always* called from the UI thread.
			mSubItems = (ArrayList<NetInfo>) results.values;

			notifyDataSetChanged();
		}

		protected FilterResults performFiltering(CharSequence filterString) {
			// NOTE: this function is *always* called from a background thread,
			// and
			// not the UI thread.

			final FilterResults results = new FilterResults();
			final List<NetInfo> i = new ArrayList<>();

			if (filterString != null && filterString.toString().length() > 0) {

				for (int index = 0; index < mAllItems.size(); index++) {
					final NetInfo item = mAllItems.get(index);
					if (item.getQrSsid().toLowerCase().contains(filterString.toString().toLowerCase())) {
						i.add(item);
					}

				}
				results.values = i;
				results.count = i.size();
			}
			else {
				synchronized (mAllItems) {
					results.values = mAllItems;
					results.count = mAllItems.size();
				}
			}

			return results;
		}
	}
}