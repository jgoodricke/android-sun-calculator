package swindroid.suntime.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import swindroid.suntime.R;
import swindroid.suntime.calc.AstronomicalCalendar;
import swindroid.suntime.calc.GeoLocation;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindSunFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindSunFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindSunFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	private View rootView;


	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	public FindSunFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment FindSunFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static FindSunFragment newInstance(String param1, String param2) {
		FindSunFragment fragment = new FindSunFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		rootView = inflater.inflate(R.layout.fragment_find_sun, container, false);
		initializeUI();
		return rootView;
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}


	/**
	 * --------------
	 * FUNCTIONAL CODE
	 * --------------
	 **/
	private Spinner location_spinner; //Spinner of lcation names
	OnDateChangedListener dateChangeHandler = new OnDateChangedListener() {
		public void onDateChanged(DatePicker dp, int year, int monthOfYear, int dayOfMonth) {
			updateTime(year, monthOfYear, dayOfMonth);
		}
	};
	//Selected date variables
	private int year;
	private int month;
	private int day;

	private void initializeUI() {
		//Populate the Spinner
		location_spinner = (Spinner) rootView.findViewById(R.id.location_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(((Main) getActivity()), android.R.layout.simple_spinner_item, ((Main) getActivity()).getLocationNames());
		location_spinner.setAdapter(adapter);

		DatePicker dp = (DatePicker) rootView.findViewById(R.id.datePicker);
		Calendar cal = Calendar.getInstance();
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		day = cal.get(Calendar.DAY_OF_MONTH);
		dp.init(year, month, day, dateChangeHandler); // setup initial values and reg. handler
		updateTime(year, month, day);

		setLocationListener();

		//Set Button Onclick Listener
		Button saveButton = (Button) rootView.findViewById(R.id.save_button);
		saveButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View view) {
						processInput();
					}
				});
	}

	private void updateTime(int year, int monthOfYear, int dayOfMonth) {
		//TimeZone tz = TimeZone.getDefault();
		//GeoLocation geolocation = new GeoLocation("Melbourne", -37.50, 145.01, tz);

		LocationData loc = getLocation();
		TimeZone tz = TimeZone.getTimeZone(loc.get_timeZone());
		GeoLocation geolocation = new GeoLocation(loc.get_cityName(), loc.get_latitude(), loc.get_longitude(), tz);

		AstronomicalCalendar ac = new AstronomicalCalendar(geolocation);
		ac.getCalendar().set(year, monthOfYear, dayOfMonth);
		Date srise = ac.getSunrise();
		Date sset = ac.getSunset();

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		TextView sunriseTV = (TextView) rootView.findViewById(R.id.sunriseTimeTV);
		TextView sunsetTV = (TextView) rootView.findViewById(R.id.sunsetTimeTV);
		Log.d("SUNRISE Unformatted", srise + "");

		sunriseTV.setText(sdf.format(srise));
		sunsetTV.setText(sdf.format(sset));
	}

	/**
	 * Gets the Location object that corresponds to the selected location in the spinner
	 */
	private LocationData getLocation() {
		String locStr = location_spinner.getSelectedItem().toString();

		//for (LocationData L : getActivity().getLocations()) {
		for (LocationData L : ((Main) getActivity()).getLocations()) {
			if (L.get_cityName() == locStr) {
				return L;
			}
		}
		//Note, this section is just a compiler formality and should never be reached - the two lists are imported from the same source so the entry should exist in both.
		Log.e("MainActivity", "location object not found in list of location names.");
		return null;
	}

	private void setLocationListener() {
		//Set Listener
		location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				updateTime(year, month, day);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				Log.e("MainActivity", "Spinner selection is empty, that shouldn't even be possible...");
			}
		});
	}

	/**
	 * Used to get input from user, add it to the lists and save it to internal storage
	 **/
	private void processInput() {
		//Get Text boxes and values
		EditText locationText = (EditText) rootView.findViewById(R.id.location_editText);
		EditText latText = (EditText) rootView.findViewById(R.id.lat_editText);
		EditText lonText = (EditText) rootView.findViewById(R.id.lon_editText);
		EditText timeText = (EditText) rootView.findViewById(R.id.time_editText);

		String name = locationText.getText().toString();
		String LatString = latText.getText().toString();
		String lonString = lonText.getText().toString();
		String tz = timeText.getText().toString();

		//If some fields are empty, show a message
		if (name.equals("") || LatString.equals("") || lonString.equals("") || tz.equals("")) {
			((Main) getActivity()).makeToast("Some fields are blank");
		}
		//Check if timezone is valid
		else if (!(Arrays.asList(TimeZone.getAvailableIDs()).contains(tz))) {
			((Main) getActivity()).makeToast("The timezone you entered is invalid");
		}
		//If no errors, process data.
		else {
			addLocation(name, LatString, lonString, tz);
		}
	}

	public void addLocation(String name, String LatString, String lonString, String tz) {
		//Concatenate data
		String output = name + "," + LatString + "," + lonString + "," + tz;

		//Write data to file
		try {
			//TODO: Get Import
			FileOutputStream fos = getContext().openFileOutput(((Main) getActivity()).getFilename(), Context.MODE_PRIVATE);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos));
			//Write existing data
			for (LocationData L : ((Main) getActivity()).getLocations()) {
				String s = L.get_cityName() + "," + L.get_latitude() + "," + L.get_longitude() + "," + L.get_timeZone();
				writer.println(s);
			}

			//write new data
			writer.println(output);
			writer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		//Convert strings to Doubles
		double lat = Double.parseDouble(LatString);
		double lon = Double.parseDouble(lonString);

		//TODO: Check if this actually works (seems like it's breaking the rules of encapusulation)
		//Add data to lists
		((Main) getActivity()).getLocations().add(new LocationData(name, lat, lon, tz));
		((Main) getActivity()).getLocationNames().add(name);

		((Main) getActivity()).makeToast("New Location Added");
	}

	public String shareLocation() {
		//Get sunrise and sunset times
		TextView sunriseTV = (TextView) rootView.findViewById(R.id.sunriseTimeTV);
		TextView sunsetTV = (TextView) rootView.findViewById(R.id.sunsetTimeTV);
		String sunset = sunriseTV.getText().toString();
		String sunrise = sunsetTV.getText().toString();

		//Get date
		String date = Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year);

		//Get Location
		String location = location_spinner.getSelectedItem().toString();

		//return string
		return "Location: " + location + "\nDate" + date + "\nSunrise: " + sunrise + "\nSunset: " + sunset;
	}
}