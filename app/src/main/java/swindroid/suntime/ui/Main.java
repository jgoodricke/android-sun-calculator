package swindroid.suntime.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import swindroid.suntime.R;

import static java.lang.Double.parseDouble;


public class Main extends AppCompatActivity {

	//Fragments
	private FindSunFragment _sunF;
	private FindSunRangeFragment _sunRangeF;
	private MapsFragment _mapsF;

	private List<LocationData> locations = new ArrayList<LocationData>(); //Stores list of locations
	private List<String> locationNames = new ArrayList<String>(); //Stores list of location names, to use in the spinner
	//Name of the internal storage file for holding data
	private String filename = "locations";
	private TabLayout tabs;

	//TODO: Add method to add new location

	public List<LocationData> getLocations() {
		return locations;
	}

	public List<String> getLocationNames() {
		return locationNames;
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//Import Locations from file
		importLocations();

		// setup fragments
		_sunF = new FindSunFragment();
		String sunTabName = "Find Sunrise/Sunset";
		_sunRangeF = new FindSunRangeFragment();
		String sunRangeTabName = "Find Sunrise/Sunset Range";

		_mapsF = new MapsFragment();
		String mapsTabName = "Locations Map";

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(tb);

		tabs = (TabLayout) findViewById(R.id.tabLayout);

		tabs.addTab(tabs.newTab().setText(sunTabName));
		tabs.addTab(tabs.newTab().setText(sunRangeTabName));
		tabs.addTab(tabs.newTab().setText(mapsTabName));
		tabs.setTabGravity(TabLayout.GRAVITY_FILL);

		// declared as final to indicate these do not change (needed for use in inner class)
		final MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager(), 3);

		final ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

		pager.setAdapter(adapter);

		// listen for page changes
		pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

		// listen for tab selection
		tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				pager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
	}


	/**
	 * adds the items in action_bar_menu.xml to the action bar.
	 **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_menu, menu);
		return true;
	}

	/**
	 * Sets onClick Listener for share button on action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_share:
				shareLocation();
				return true;
		}
		return false;
	}

	/**
	 * Gets sunrise info from the current fragment and sends it
	 */
	public void shareLocation() {
		//TODO: Get share values from fragments
		String shareString = "";

		//NOTE: sharing from sunFragment is the only one that works at this point.
		switch (tabs.getSelectedTabPosition()) {
			case 0:
				shareString = _sunF.shareLocation();
				break;
			case 1:
				shareString = _sunRangeF.shareLocation();
				break;
			case 2:
				shareString = _sunRangeF.shareLocation();
				break;
			case 3:
				shareString = _mapsF.shareLocation();
				break;
		}

		//If message is not empty, send message
		if (shareString != "" && shareString != null) {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, shareString);
			startActivity(intent);
		}
		//if message is empty, let user know
		else {
			makeToast("Sharing is not available on this tab.");
		}
	}

	/**
	 * Imports the list of locations and location names from internal storage and populates the spinner
	 **/
	private void importLocations() {
		//If file doesn't exist, copy it from RAW
		if (!(getBaseContext().getFileStreamPath(filename).exists())) {
			copyToInternal();
		}

		//Import all items from internal storage into lists
		try {
			//Set input stream and buffer reader
			FileInputStream fis = openFileInput(getFilename());
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			String data;
			//While not at end of file, get data and add to lists
			while ((data = reader.readLine()) != null) {

				String[] values = data.split(",");
				getLocations().add(new LocationData(values[0], parseDouble(values[1]), parseDouble(values[2]), values[3]));
				getLocationNames().add(values[0]);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to easily make a toast
	 **/
	public void makeToast(String message) {
		Toast.makeText(Main.this, message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Writes locations to internal file if it doesn't already exist
	 **/
	private void copyToInternal() {
		try {
			Scanner scanner = new Scanner(getResources().openRawResource(R.raw.au_locations));
			scanner.useDelimiter("(\\n)");

			FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos));

			//Import all items from external file into lists
			while (scanner.hasNext()) {
				writer.println(scanner.next());
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Page Adaptor Class
	class MyPagerAdapter extends FragmentStatePagerAdapter {
		private int _numberOfTabs;

		public MyPagerAdapter(FragmentManager fm, int numberOfTabs) {
			super(fm);
			_numberOfTabs = numberOfTabs;
		}

		// dispatch appropriate fragment
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return _sunF;

				case 1:
					return _sunRangeF;

				case 2:
					return _mapsF;

				default:
					return null;
			}
		}

		public int getCount() {
			return _numberOfTabs;
		}

	}
}