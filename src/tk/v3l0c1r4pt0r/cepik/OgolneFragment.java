package tk.v3l0c1r4pt0r.cepik;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link OgolneFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link OgolneFragment#newInstance} factory method
 * to create an instance of this fragment.
 * 
 */
public class OgolneFragment extends Fragment {
	private static final String ARG_REPORT = "report";

	private CarReport cr;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param cr
	 *            Car data class
	 * @return A new instance of fragment OgolneFragment.
	 */
	public static OgolneFragment newInstance(CarReport cr) {
		OgolneFragment fragment = new OgolneFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_REPORT, cr);
		fragment.setArguments(args);
		return fragment;
	}

	public OgolneFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			cr = (CarReport) getArguments().getSerializable(ARG_REPORT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_ogolne, container, false);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

		TextView tv = null;
		
		ViewChangeHelper vch = new ViewChangeHelper(getView());
		
		vch.ChangeEntryState(R.id.rejCap, R.id.rejVal, cr.getNrRejestracyjny(), true);
		
		tv = (TextView) getView().findViewById(R.id.markaVal);
		tv.setText(cr.getMarka());
		
		tv = (TextView) getView().findViewById(R.id.typVal);
		String typ = cr.getTyp();
		if(typ.length()>0)
		{
			tv.setText(typ);
			tv.setVisibility(View.VISIBLE);
		}
		else
			tv.setVisibility(View.GONE);
		
		tv = (TextView) getView().findViewById(R.id.modelVal);
		tv.setText(cr.getModel());
		
		//przebieg
		vch.ChangeEntryState(R.id.przebiegCap, R.id.przebiegVal, cr.getPrzebieg(), false);
		
		tv = (TextView) getView().findViewById(R.id.jednostkaVal);
		tv.setText(cr.getPrzebiegUnit());
		
		//rok produkcji
		vch.ChangeEntryState(R.id.rokCap, R.id.rokVal, cr.getRokProdukcji(), true);
		
		tv = (TextView) getView().findViewById(R.id.rodzajVal);
		tv.setText(cr.getRodzaj()+", "+cr.getPodrodzaj());
		
		tv = (TextView) getView().findViewById(R.id.pojemnoscVal);
		tv.setText(cr.getPojemnoscSilnika()+", "+cr.getRodzajPaliwa());
		
		vch.ChangeEntryState(R.id.vinCap, R.id.vinVal, cr.getVin(), true);

		vch.ChangeEntryState(R.id.statusCap, R.id.statusVal, cr.getStatus(), true);
		
		tv = (TextView) getView().findViewById(R.id.ocVal);
		if(cr.getOc())
			tv.setTextColor(getResources().getColor(R.color.ocOkColor));
		else
			tv.setTextColor(getResources().getColor(R.color.ocNoColor));
		
		tv = (TextView) getView().findViewById(R.id.przegladVal);
		if(cr.getPrzeglad())
			tv.setTextColor(getResources().getColor(R.color.ocOkColor));
		else
			tv.setTextColor(getResources().getColor(R.color.ocNoColor));

		tv = (TextView) getView().findViewById(R.id.kradzionyVal);
		if(cr.getKradziony())
			tv.setVisibility(TextView.VISIBLE);
		else
			tv.setVisibility(TextView.GONE);
    }

	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		public void onFragmentInteraction(Uri uri);
	}

}
