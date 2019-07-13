package com.tripmee.findmee.CrimeReport;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CrimeReport.CrimeReportFragment;
import com.tripmee.findmee.GlobalConstants.CrimeReportConstants;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Utility;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrimeTypeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrimeTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrimeTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String _CrimeType;
    private String _ReportID;

    private boolean _IsEdited = false;

    private FloatingActionButton btnSexualAssault;
    private FloatingActionButton btnVerbalAbuse;
    private FloatingActionButton btnOtherAbuse;

    private TextView TextViewSexualAbuse;
    private TextView TextViewVerbalAbuse;
    private TextView TextViewOtherAbuse;


    private OnFragmentInteractionListener mListener;

    public CrimeTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CrimeTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CrimeTypeFragment newInstance(String param1, String param2) {
        CrimeTypeFragment fragment = new CrimeTypeFragment();
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
            _CrimeType = getArguments().getString(CrimeReportConstants.CRIMETYPE);
            _ReportID = getArguments().getString(CrimeReportConstants.REPORTID);

            if(!Utility.StringIsBlankOrEmpty(_ReportID)){
                _IsEdited = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_crime_type, container, false);

        TextViewSexualAbuse = (TextView) rootView.findViewById(R.id.physicalabusetextView);
        TextViewVerbalAbuse = (TextView) rootView.findViewById(R.id.verbalabusetextView);
        TextViewOtherAbuse = (TextView) rootView.findViewById(R.id.othertextView);

        btnSexualAssault = (FloatingActionButton)rootView.findViewById(R.id.btnSexualAssualt);

        btnSexualAssault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportCrimeFragment(CrimeReportConstants.SEXUAL_ASSAULT);
            }
        });

        btnVerbalAbuse = (FloatingActionButton)rootView.findViewById(R.id.btnVerbalAbuse);
        btnVerbalAbuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportCrimeFragment(CrimeReportConstants.VERBAL_ABUSE);
            }
        });

        btnOtherAbuse = (FloatingActionButton)rootView.findViewById(R.id.btnOther);
        btnOtherAbuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportCrimeFragment(CrimeReportConstants.OTHER_ABUSE);
            }
        });

        if (_IsEdited){
            SetSelectedOption(_CrimeType);
        }

        return rootView;
    }

    private void SetSelectedOption(String crimeType){

         if(crimeType.equals(CrimeReportConstants.SEXUAL_ASSAULT)){
             TextViewSexualAbuse.setTextColor(ContextCompat.getColor(getContext(), R.color.SelectedButtonTextColor));
             //btnSexualAssault.setFocusedByDefault(true);
         }else if(crimeType.equals(CrimeReportConstants.VERBAL_ABUSE)){
             TextViewVerbalAbuse.setTextColor(ContextCompat.getColor(getContext(), R.color.SelectedButtonTextColor));
             //btnVerbalAbuse.setFocusedByDefault(true);
         }else if(crimeType.equals(CrimeReportConstants.OTHER_ABUSE)){
             TextViewOtherAbuse.setTextColor(ContextCompat.getColor(getContext(), R.color.SelectedButtonTextColor));
             //btnOtherAbuse.setFocusedByDefault(true);
         }
    }

    private void ReportCrimeFragment(String crimeType) {
        CrimeReportFragment fragment = new CrimeReportFragment();
        Bundle arguments = new Bundle();
        arguments.putString("Type", crimeType);

        if(_IsEdited){
            arguments.putString(CrimeReportConstants.REPORTID, _ReportID);
        }

        fragment.setArguments(arguments);

        try {
            android.support.v4.app.FragmentManager fragmentManager = getFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/


    @Override
    public void onResume() {
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
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
}
