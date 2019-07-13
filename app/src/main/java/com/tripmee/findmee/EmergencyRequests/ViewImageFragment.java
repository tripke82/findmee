package com.tripmee.findmee.EmergencyRequests;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.EmergencyRequests.LocationFragment;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.EmergencyContact;
import com.tripmee.findmee.Models.EmergencyRequest;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.ImageUtility;
import com.tripmee.findmee.Utilities.Utility;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewImageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EmergencyRequest mRequest;
    private EmergencyContact mContact;
    private UserGlobalHandler UserHandler;
    private Button btnBack;
    private Button btnSaveImage;
    private ImageView imageView;
    private TextView lblText;
    private TextView lblMobile;
    private CircleImageView mCircleImageView;
    private OnFragmentInteractionListener mListener;

    public ViewImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewImageFragment newInstance(String param1, String param2) {
        ViewImageFragment fragment = new ViewImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserHandler = UserGlobalHandler.get_instance();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

            Bundle arguments = getArguments();
            String ID = arguments.getString(GlobalConstants.EMERGENCY_REQUEST_ID);
            mRequest = UserHandler.GetRequestByID(ID);
            mContact = UserHandler.GetEmergencyContactByID(mRequest.getUserID());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_view_image, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setTitleTextColor(Color.WHITE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);


        btnBack = (Button) rootView.findViewById(R.id.btnBack);
        btnSaveImage = (Button) rootView.findViewById(R.id.btnSaveImage);
        imageView = (ImageView) rootView.findViewById(R.id.imageViewEmergency);

        lblText = (TextView) rootView.findViewById(R.id.emergencyText);
        lblMobile = (TextView) rootView.findViewById(R.id.emergencyMobile);
        mCircleImageView = (CircleImageView) rootView.findViewById(R.id.profile_image_eitem);

        InitEmergencyContactDetails();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewLocationFragment(mRequest.getEmergencyRequestID());
            }
        });

        btnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRequest.getEmergencyImageUrl().isEmpty()){
                    Uri image = Uri.parse(mRequest.getEmergencyImageUrl());

                    Bitmap imageBitmap = null;
                    imageBitmap = ImageUtility.StringToBitMap(mRequest.getEmergencyImageUrl());
                    MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), imageBitmap, "Emergency" + mRequest.getmDateTime() , mRequest.getEmergencyMessage());
                    Utility.displayMessage(getActivity(), "Saved");
                    //Toast.makeText(, "Saved", Toast.LENGTH_LONG).show();
                }
            }
        });

        InitEmergencyImage();

        return rootView;
    }

    private void InitEmergencyContactDetails(){

        lblText.setText(mRequest.getEmergencyMessage());
        lblMobile.setText(mRequest.getMobile());

        Bitmap imageBitmap = null;

        if(mContact == null){
            return;
        }

        String sImage = mContact.get_FireBaseProfileImage();

        if (!Utility.StringIsBlankOrEmpty(sImage)) {
            imageBitmap = ImageUtility.StringToBitMap(mContact.get_FireBaseProfileImage());
            mCircleImageView.setImageBitmap(imageBitmap);
        }else{
            mCircleImageView.setImageResource(R.drawable.default_profile);
        }
    }

    private void InitEmergencyImage(){

        if (mRequest.getEmergencyImageUrl() != null){
            if (!mRequest.getEmergencyImageUrl().isEmpty()){
                Bitmap imageBitmap = null;
                imageBitmap = ImageUtility.StringToBitMap(mRequest.getEmergencyImageUrl());

                int imageWidth = imageBitmap.getWidth();
                int imageHeight = imageBitmap.getHeight();

                int newWidth = GetScreenWidth(); //this method should return the width of device screen.
                float scaleFactor = (float)newWidth/(float)imageWidth;
                int newHeight = (int)(imageHeight * scaleFactor);
                //newHeight = 500;
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, newWidth, newHeight, true);
                imageView.setImageBitmap(imageBitmap);

            }
        }
    }

    private void ViewLocationFragment(String requestID) {
        LocationFragment fragment = new LocationFragment();
        Bundle arguments = new Bundle();
        arguments.putString(GlobalConstants.EMERGENCY_REQUEST_ID, requestID);
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

    private int GetScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        return width;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        //if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
        //}
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
    //}

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAnalytics.getInstance(getActivity()).setCurrentScreen(getActivity(), this.getClass().getSimpleName(), null);
    }

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
