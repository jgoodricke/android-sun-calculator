package swindroid.suntime.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import swindroid.suntime.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MapsFragment extends Fragment {
	public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	MapView mMapView;


	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	private View rootView;
	private GoogleMap googleMap;
	private OnFragmentInteractionListener mListener;

	public MapsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment MapsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static MapsFragment newInstance(String param1, String param2) {
		MapsFragment fragment = new MapsFragment();
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
		rootView = inflater.inflate(R.layout.fragment_maps, container, false);

		mMapView = (MapView) rootView.findViewById(R.id.mapView);

		mMapView.onCreate(savedInstanceState);

		mMapView.onResume(); // needed to get the map to display immediately

		try {
			MapsInitializer.initialize(getActivity().getApplicationContext());
		} catch (Exception e) {
			e.printStackTrace();
		}

		mMapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap mMap) {
				googleMap = mMap;

				googleMap.getUiSettings().setMyLocationButtonEnabled(false);

				//Check for permission - required from API 26 onwards
				if (ContextCompat.checkSelfPermission((Main) getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
						PackageManager.PERMISSION_GRANTED &&
						ContextCompat.checkSelfPermission((Main) getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
								PackageManager.PERMISSION_GRANTED) {
					googleMap.setMyLocationEnabled(true);
					googleMap.getUiSettings().setMyLocationButtonEnabled(true);
				} else {
					requestPermissions(new String[]{
									Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_COARSE_LOCATION},
							MY_PERMISSIONS_REQUEST_LOCATION);
				}

				//Add markers to each location
				for (LocationData L : ((Main) getActivity()).getLocations()) {
					mMap.addMarker(new MarkerOptions()
							.position(new LatLng(L.get_latitude(), L.get_longitude()))
							.title(L.get_cityName())
							.snippet(L.get_timeZone()));
				}
				//Centre the map on Australia
				LatLng australia = new LatLng(-25.060203, 134.611387);
				CameraPosition cameraPosition = new CameraPosition.Builder().target(australia).zoom(3).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		});

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mMapView.onLowMemory();
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
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
