package com.tripmee.findmee.Dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.FriendRequests.FriendRequestActivity;
import com.tripmee.findmee.GlobalConstants.FriendRequestConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.LoginRegister.ActivationActivity;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.Models.UserStatus;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView lblName;
    private TextView lblEmail;
    private TextView lblMobile;
    private ListView listView;
    private CircleImageView mCircleImageView;
    private Button mEditAccount;
    private Button mSetUpContact;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mContactList;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private UserGlobalHandler UserHandler;
    //private OnFragmentInteractionListener mListener;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
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
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        lblName = (TextView) rootView.findViewById(R.id.lblNameDashboard);
        lblEmail = (TextView) rootView.findViewById(R.id.lblEmailDashboard);
        lblMobile = (TextView) rootView.findViewById(R.id.lblMyPhoneNumber);
        mCircleImageView = (CircleImageView) rootView.findViewById(R.id.profile_image_dashboard);
        listView = rootView.findViewById(R.id.contacts_list_view);
        mEditAccount = (Button) rootView.findViewById(R.id.btnEditAccount);
        mSetUpContact = (Button) rootView.findViewById(R.id.btnSetupEmergencyContact);

        Init();
        //return inflater.inflate(R.layout.fragment_dashboard, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        Init();
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
    }

    private void Init(){
        UserHandler = UserGlobalHandler.get_instance();
        UserHandler.ListenEmergencyContactUpdates();
        lblName.setText(UserHandler.CurrentUser.getUserName());
        lblEmail.setText(UserHandler.CurrentUser.getEmail());
        lblMobile.setText(UserHandler.CurrentUser.getMobile());

        /*Bitmap imageBitmap = null;
        String sImage = UserHandler.CurrentUser.getProfileImageUrl();

        if (!Utility.StringIsBlankOrEmpty(sImage)) {
            imageBitmap = ImageUtility.StringToBitMap(UserHandler.CurrentUser.getProfileImageUrl());
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }*/

        Bitmap imageBitmap = ImageUtility.GetBitmapByUrl(UserHandler.CurrentUser.get_FireBaseProfileImage());

        if(imageBitmap != null){
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }

        if (UserHandler.IsEmergencyContactExist()){
            ContactsAdapter adapter = new ContactsAdapter(getActivity(), UserHandler.GetEmergencyContacts());
            adapter.SetDeleteButtonListener(new ContactsAdapter.IDeleteButtonListener() {
                @Override
                public void OnButtonClickListener(int position, EmergencyContact value) {

                }
            });
            // Attach the adapter to a ListView
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
                { }
            });
        }else{
            listView.setAdapter(null);
        }

        if (UserHandler.CurrentUser.Status == UserStatus.FREETRIAL){
            mEditAccount.setText(getResources().getString(R.string.btn_ActivateAccount));

            mEditAccount.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //startActivity(new Intent(getActivity(), EditAccountActivity.class));
                    startActivity(new Intent(getActivity(), ActivationActivity.class));
                }
            });
        }else{
            mEditAccount.setText(getResources().getString(R.string.btn_Edit));
            mEditAccount.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startActivity(new Intent(getActivity(), EditAccountActivity.class));
                }
            });
        }

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
                startActivity(new Intent(getActivity(), FriendRequestActivity.class));
            }
        });
    }
    public void RefreshList(){

        if(!UserHandler.IsEmergencyContactExist()) {
            listView.setEmptyView(null);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        //}
    }
// @Override causing crash in fragment.commit()
    // public void onAttach(Context context) {
    //super.onAttach(context);
    //if (context instanceof OnFragmentInteractionListener) {
    //mListener = (OnFragmentInteractionListener) context;
    //} else {
    //throw new RuntimeException(context.toString()
    //+ " must implement OnFragmentInteractionListener");
    //}
    //}


    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
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
