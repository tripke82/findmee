package com.tripmee.findmee.FriendRequests;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CallBack.DataChangedCallBack;
import com.tripmee.findmee.CrimeReport.CrimeTypeFragment;
import com.tripmee.findmee.Dashboard.DashboardFragment;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FriendRequestListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FriendRequestListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendRequestListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView listView;
    private TextView emptyTextView;
    private UserGlobalHandler UserHandler;

    private FloatingActionButton reportButton;
    private CircleImageView ProfileImageView;
    //private OnFragmentInteractionListener mListener;

    public FriendRequestListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HandshakeListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendRequestListFragment newInstance(String param1, String param2) {
        FriendRequestListFragment fragment = new FriendRequestListFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
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
        final View rootView = inflater.inflate(R.layout.fragment_friendrequest_list, container, false);

        UserHandler = UserGlobalHandler.get_instance();
        listView = (ListView)rootView.findViewById(R.id.handshake_listview);
        emptyTextView = (TextView)rootView.findViewById(R.id.emptyHSElement);

        //Init_ProfileImage(rootView);
        reportButton =(FloatingActionButton)rootView.findViewById(R.id.reportButton);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ReportCrimeFragment();
                loadFragment(new CrimeTypeFragment());
            }
        });

        InitReceivedFriendRequests();
        UserHandler.FriendRequestsCallBack = new DataChangedCallBack() {
            @Override
            public void CallBack() {
                InitReceivedFriendRequests();
            }
        };



        if(UserHandler.isFireBaseDataLoaded()){

            Init_ProfileImage(rootView);

        }else{

            UserHandler.InitUserCompletedCallBack = new DataChangedCallBack() {
                @Override
                public void CallBack() {
                    Init_ProfileImage(rootView);

                }
            };
        }

        Button mSetUpContact = (Button) rootView.findViewById(R.id.btnSetupEmergencyContact);

        if(UserHandler.GetEmergencyContactsCount() >= FriendRequestConstants.MAX_CONNECTIONS){
           mSetUpContact.setEnabled(false);
        }else {
            mSetUpContact.setEnabled(true);
        }

        mSetUpContact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(UserHandler.GetEmergencyContactsCount() >= FriendRequestConstants.MAX_CONNECTIONS){
                    String msg = getString(R.string.Number_Of_friends_exceeded);
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                    return;
                }
                //startActivity(new Intent(getActivity(), ConnectActivity.class));
                startActivity(new Intent(getActivity(), FriendRequestActivity.class));
            }
        });
        //return inflater.inflate(R.layout.fragment_dashboard, container, false);

        return rootView;
    }

    private void Init_ProfileImage(View rootView){
        ProfileImageView = (CircleImageView) rootView.findViewById(R.id.profile_photo);

        ProfileImageView.setImageResource(R.drawable.default_profile);

        if(!Utility.StringIsBlankOrEmpty(UserHandler.CurrentUser.get_FireBaseProfileImage())){
            ProfileImageView.setImageBitmap(ImageUtility.StringToBitMap(UserHandler.CurrentUser.get_FireBaseProfileImage()));
        }

        ProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new DashboardFragment());
            }
        });
    }

    private void InitReceivedFriendRequests(){
        if(UserHandler.FriendRequestReceivedExist()) {
            FriendRequestAdapter adapter = new FriendRequestAdapter(getActivity(), UserHandler.getOtherFriendRequests());
            listView.setAdapter(adapter);
        }else{
            listView.setEmptyView(emptyTextView);
        }
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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

    public void RefreshList(){

        if(UserHandler.FriendRequestReceivedExist()) {

        }else{
            listView.setEmptyView(emptyTextView);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {

            //mListener.onFragmentInteraction(uri);
        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
    }

    //@Override
    //public void onAttach(Context context) {
        //super.onAttach(context);
        //if (context instanceof OnFragmentInteractionListener) {
            //mListener = (OnFragmentInteractionListener) context;
        //} else {
            //throw new RuntimeException(context.toString()
                    //+ " must implement OnFragmentInteractionListener");
        //}
    ///}

    //@Override
    //public void onDetach() {
        //super.onDetach();
        //mListener = null;
    //}

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
    //public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    //}
}
