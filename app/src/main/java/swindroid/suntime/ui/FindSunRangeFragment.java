package swindroid.suntime.ui;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import swindroid.suntime.R;
import swindroid.suntime.calc.AstronomicalCalendar;
import swindroid.suntime.calc.GeoLocation;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FindSunRangeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FindSunRangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindSunRangeFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	static int id = 1;
	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	//Used for accessing current view
	private View rootView;
	private OnFragmentInteractionListener mListener;

	private Spinner location_spinner; //Spinner of lcation names
	private DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	//Selected date variables
	private int startYear;
	private int startMonth;

	private int startDay;
	private int endYear;
	private int endMonth;
	private int endDay;
	OnDateChangedListener startDateChangeHandler = new OnDateChangedListener() {
		public void onDateChanged(DatePicker dp, int year, int month, int day) {
			startYear = year;
			startMonth = month;
			startDay = day;
			updateTime(startYear, startMonth, startDay, endYear, endMonth, endDay);
		}
	};
	OnDateChangedListener endDateChangeHandler = new OnDateChangedListener() {
		public void onDateChanged(DatePicker dp, int year, int month, int day) {
			endYear = year;
			endMonth = month;
			endDay = day;
			updateTime(startYear, startMonth, startDay, endYear, endMonth, endDay);
		}
	};

	public FindSunRangeFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment FindSunRangeFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static FindSunRangeFragment newInstance(String param1, String param2) {
		FindSunRangeFragment fragment = new FindSunRangeFragment();
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
		rootView = inflater.inflate(R.layout.fragment_find_sun_range, container, false);
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

	// Returns a valid id that isn't in use
	public int findId() {
		View v = rootView.findViewById(id);
		while (v != null) {
			v = rootView.findViewById(++id);
		}
		return id++;
	}

	private void initializeUI() {
		//Populate the Spinner
		location_spinner = (Spinner) rootView.findViewById(R.id.location_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(((Main) getActivity()), android.R.layout.simple_spinner_item, ((Main) getActivity()).getLocationNames());
		location_spinner.setAdapter(adapter);

		//Set date spinners to current date
		Calendar cal = Calendar.getInstance();
		startYear = cal.get(Calendar.YEAR);
		startMonth = cal.get(Calendar.MONTH);
		startDay = cal.get(Calendar.DAY_OF_MONTH);

		endYear = cal.get(Calendar.YEAR);
		endMonth = cal.get(Calendar.MONTH);
		endDay = cal.get(Calendar.DAY_OF_MONTH);

		DatePicker dp1 = (DatePicker) rootView.findViewById(R.id.startDatePicker);
		DatePicker dp2 = (DatePicker) rootView.findViewById(R.id.endDatePicker);
		dp1.init(startYear, startMonth, startDay, startDateChangeHandler); // setup initial values and reg. handler
		dp2.init(endYear, endMonth, endDay, endDateChangeHandler); // setup initial values and reg. handler
		updateTime(startYear, startMonth, startDay, endYear, endMonth, endDay);

		setLocationListener();
	}

	//private void updateTime(int year, int monthOfYear, int dayOfMonth) {
	private void updateTime(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
		//create date objects
		String startDateString = Integer.toString(startYear) + "-" + Integer.toString(startMonth) + "-" + Integer.toString(startDay);
		String endDateString = Integer.toString(endYear) + "-" + Integer.toString(endMonth) + "-" + Integer.toString(endDay);

		Date startDate;
		Date endDate;

		//Create date objects from input
		try {
			startDate = sdf.parse(startDateString);
			endDate = sdf.parse(endDateString);
		} catch (Exception e) {
			//Note, this point should never be reached, as the input is coming from a date spinner.
			//Log.d( "FindSunFragment", "Error passing dates.");
			throw new RuntimeException("findSunRangeFragment: Error passing dates.");
		}

		//check if the start date is after the end date
		//TODO: Figure out why this doesn't work if the end day is 1 day before the start day
		if (startDate.getTime() <= endDate.getTime()) {
			Date currentDate = startDate;

			//Get Location
			LocationData loc = getLocation();
			TimeZone tz = TimeZone.getTimeZone(loc.get_timeZone());
			GeoLocation geolocation = new GeoLocation(loc.get_cityName(), loc.get_latitude(), loc.get_longitude(), tz);

			SimpleDateFormat sunDF = new SimpleDateFormat("HH:mm");

			//Used to get integers for year, month and day
			Calendar cal = Calendar.getInstance();
			int year;
			int month;
			int day;

			//get output table and remove all existing rows
			TableLayout resultTable = (TableLayout) rootView.findViewById(R.id.resultTable);
			resultTable.removeAllViews();

			//Create Header Rows
			TextView dateHTxtView = new TextView(rootView.getContext());
			dateHTxtView.setId(findId());
			dateHTxtView.setTypeface(null, Typeface.BOLD);
			dateHTxtView.setTextSize(18);
			dateHTxtView.setText("Date");

			TextView sRiseHTxtView = new TextView(rootView.getContext());
			sRiseHTxtView.setId(findId());
			sRiseHTxtView.setTypeface(null, Typeface.BOLD);
			sRiseHTxtView.setTextSize(18);
			sRiseHTxtView.setText("Sunrise");

			TextView sSetHTxtView = new TextView(rootView.getContext());
			sSetHTxtView.setId(findId());
			sSetHTxtView.setTypeface(null, Typeface.BOLD);
			sSetHTxtView.setTextSize(18);
			sSetHTxtView.setText("Sunset");

			//Add views to Table
			TableRow Hrow = new TableRow(rootView.getContext());
			Hrow.addView(dateHTxtView);
			Hrow.addView(sRiseHTxtView);
			Hrow.addView(sSetHTxtView);
			resultTable.addView(Hrow);

			//output all dates from start date to end date
			while (currentDate.getTime() <= endDate.getTime()) {
				//Get the current year, month and day
				cal.setTime(currentDate);
				year = cal.get(Calendar.YEAR);
				month = cal.get(Calendar.MONTH);
				day = cal.get(Calendar.DAY_OF_MONTH);

				//Get Sunrise and Sunset times
				AstronomicalCalendar ac = new AstronomicalCalendar(geolocation);

				ac.getCalendar().set(year, month, day);
				Date srise = ac.getSunrise();
				Date sset = ac.getSunset();

				//Create Text Views
				TextView dateTxtView = new TextView(rootView.getContext());
				dateTxtView.setId(findId());
				dateTxtView.setTextSize(18);
				dateTxtView.setText(sdf.format(currentDate));

				TextView sRiseTxtView = new TextView(rootView.getContext());
				sRiseTxtView.setId(findId());
				sRiseTxtView.setTextSize(18);
				sRiseTxtView.setText(sunDF.format(srise));

				TextView sSetTxtView = new TextView(rootView.getContext());
				sSetTxtView.setId(findId());
				sSetTxtView.setTextSize(18);
				sSetTxtView.setText(sunDF.format(sset));

				//Add views to Table
				TableRow tbrow = new TableRow(rootView.getContext());
				tbrow.addView(dateTxtView);
				tbrow.addView(sRiseTxtView);
				tbrow.addView(sSetTxtView);
				resultTable.addView(tbrow);

				//Add 1 day to current time (NOTE: 86400000 is 24 hours)
				currentDate.setTime(currentDate.getTime() + 86400000);
			}

		}
		//If start time is greater than end time, throw an error
		else {
			((Main) getActivity()).makeToast("The start date has to be before the end date.");
		}
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
		//Note, this section is just a compiler formality and should never be reached - the two lists are imported from the same source, so the entry should exist in both.
		Log.e("MainActivity", "location object not found in list of location names.");
		return null;
	}

	private void setLocationListener() {
		//Set Listener
		location_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				updateTime(startYear, startMonth, startDay, endYear, endMonth, endDay);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
				Log.e("MainActivity", "Spinner selection is empty, that shouldn't even be possible...");
			}
		});
	}

	//TODO: Finish This
	public String shareLocation() {
		return null;
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
}
