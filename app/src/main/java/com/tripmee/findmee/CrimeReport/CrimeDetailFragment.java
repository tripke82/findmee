package com.tripmee.findmee.CrimeReport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.GlobalConstants.CrimeReportConstants;
import com.tripmee.findmee.GlobalConstants.GlobalConstants;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.Comment;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Models.CrimeReport.CrimeReportList;
import com.tripmee.findmee.Models.CrimeReport.UserComment;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CrimeDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CrimeDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CrimeDetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CrimeReport _CrimeReport;
    private CrimeReportList _CrimeReportList;
    private UserGlobalHandler UserHandler;
    private Bitmap _ImageBitmap;
    private com.tripmee.findmee.CrimeReport.CommentsAdapter _CommentsAdapter;
    public ArrayList<UserComment> _UserComments = new ArrayList<UserComment>();
    private UnitOfWork _UOW;
    final Calendar myCalendar = Calendar.getInstance();

    private TextView commentsTextView;
    private TextView datetimeTextView;
    private ImageView crimeImageView;
    private ListView commentsListView;
    private EditText commentsEditText;
    private ImageButton btnShowOptions;
    private Button btnSendComment;

    private OnFragmentInteractionListener mListener;

    public CrimeDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CrimeDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CrimeDetailFragment newInstance(String param1, String param2) {
        CrimeDetailFragment fragment = new CrimeDetailFragment();
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
        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
        _CrimeReportList = _UOW.get_CrimeReportList();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            String userID = getArguments().getString("UserID");
            String sID = getArguments().getString("ID");
            _CrimeReport = GetReport(userID,sID);
            //LoadImage(crimeReport.ImageUrl());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_crime_detail, container, false);

        commentsTextView = (TextView)rootView.findViewById(R.id.commentsTextView);
        datetimeTextView = (TextView)rootView.findViewById(R.id.datetimeTextView);
        crimeImageView = (ImageView)rootView.findViewById(R.id.crimeImageView);

        commentsTextView.setText(GetComment());
        datetimeTextView.setText(_CrimeReport.getDateTime());

        ShowHideOptionBtn(rootView,_CrimeReport);

        LoadImage();

        Init_Comments(rootView);

        return rootView;
    }

    private CrimeReport GetReport(String userID, String id){
        ArrayList<CrimeReport> crimeReports = _UOW.get_CrimeReportList().getCrimeReports();

        for (CrimeReport r: crimeReports) {
            if(r.getUserID().equals(userID) && r.getID() == Integer.valueOf(id)){
                return r;
            }
        }
        return null;
    }


    private void LoadImage(){

        Bitmap bitmap = _CrimeReportList.GetImage(_CrimeReport);

        if(bitmap != null) {

            crimeImageView.setImageBitmap(bitmap);
            crimeImageView.setVisibility(View.VISIBLE);

        }else{
            crimeImageView.setVisibility(View.GONE);
        }
    }

    private void Init_Comments(View rootView){

        _UOW.GetComments(_CrimeReport.getID());
        commentsListView = (ListView)rootView.findViewById(R.id.listViewComment);
        commentsEditText = (EditText)rootView.findViewById(R.id.editTextComment);

        _UOW.foundCommentsCallBack = new ActionCompleteCallBack() {
            @Override
            public void CallBack() {
                //_UserComments = UOW.get_UserCommentList().get_UserComments();
                _CommentsAdapter = new com.tripmee.findmee.CrimeReport.CommentsAdapter(getActivity(), _UOW.get_UserCommentList());
                commentsListView.setAdapter(_CommentsAdapter);
            }
        };

        btnSendComment = (Button)rootView.findViewById(R.id.btnSendComment);

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = commentsEditText.getText().toString();

                if(Utility.StringIsBlankOrEmpty(text)){ return; }

                String sdate = Utility.FormatDateToString(myCalendar.getTime(), GlobalConstants.DATE_FORMAT);
                String commentID = Utility.random(8);
                User user = UserHandler.CurrentUser;
                Comment c = new Comment(_CrimeReport.getID(),user.getUserID(), commentID, user.getUserName(), text,sdate);
                _UOW.PostComment(c);
                //UserComment userComment = new UserComment(UserHandler.CurrentUser, c);
                //_UserComments.add(userComment);

                _UOW.postCommentsCallBack = new ActionCompleteCallBack() { //when data is loaded
                    public void CallBack() {

                        //_UserComments.clear();
                        _UserComments = _UOW.get_UserCommentList().get_UserComments();
                        commentsEditText.getText().clear();
                        _CommentsAdapter.notifyDataSetChanged();
                        hideSoftKeyboard();
                    }
                };
            }
        });

    }

    private void ShowHideOptionBtn(View convertView, final CrimeReport report){

        btnShowOptions = (ImageButton) convertView.findViewById(R.id.btnShowOptions);

        if(UserHandler.CurrentUser.getUserID().equals(report.getUserID())) {
            btnShowOptions.setVisibility(View.VISIBLE);
            btnShowOptions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    PopupMenu popup = new PopupMenu(getActivity(), btnShowOptions);
                    //Inflating the Popup using xml file

                    popup.getMenuInflater()
                            .inflate(R.menu.options_menu, popup.getMenu());

                    setForceShowIcon(popup);

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.edit_option:
                                    ViewCrimeTypeFragment(report);
                                    return true;
                                case R.id.delete_option:
                                    //Utility.displayMessage(getContext(),"Delete current sos" + _SOSRequest.getMessage() + "Delete index sos" + sos.getMessage());
                                    String msg = getContext().getString(R.string.item_delete);
                                    AlertMessage(msg, report);
                                    return true;
                                default:
                                    return true;
                            }
                            //return true;
                        }
                    });

                    popup.show();

                }
            });
        }else{
            btnShowOptions.setVisibility(View.GONE);
        }


        /*if(UserHandler.CurrentUser.getUserID().equals(report.getUserID())) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Utility.displayMessage(getContext(),"Delete current sos" + _SOSRequest.getMessage() + "Delete index sos" + sos.getMessage());
                    String msg = getContext().getString(R.string.item_delete);
                    AlertMessage(msg, report.getID());
                }
            });
        }else{
            btnDelete.setVisibility(View.GONE);
        }*/

    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper
                            .getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod(
                            "setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private void AlertMessage(String message, final CrimeReport report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                _CrimeReportList.Delete(report.getID());
                _UOW.DeleteCrimeReport(report.getID(), report.getImageUrl());
                ViewCrimeMapFragment();

            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private String GetComment(){

        if (!Utility.StringIsBlankOrEmpty(_CrimeReport.getDisplayName())){
            return _CrimeReport.getDisplayName() + ": " + _CrimeReport.getComments();
        }
        return "Anonymous user: " + _CrimeReport.getComments();
    }

    private void ViewCrimeTypeFragment(CrimeReport report) {
        com.tripmee.findmee.CrimeReport.CrimeTypeFragment fragment = new com.tripmee.findmee.CrimeReport.CrimeTypeFragment();
        Bundle arguments = new Bundle();
        arguments.putString(CrimeReportConstants.EDITED, GlobalConstants.TRUE);
        arguments.putString(CrimeReportConstants.REPORTID, String.valueOf(report.getID()));
        arguments.putString(CrimeReportConstants.CRIMETYPE, report.getCrimeType());
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
            //mListener.onFragmentInteraction(uri);
        }
    }

    /*@Override
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

    private void ViewCrimeMapFragment() {
        com.tripmee.findmee.CrimeReport.CrimeMapFragment fragment = new com.tripmee.findmee.CrimeReport.CrimeMapFragment();
        Bundle arguments = new Bundle();
        //arguments.putString(GlobalConstants.EMERGENCY_REQUEST_ID, requestID);
        //fragment.setArguments(arguments);

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

    private void hideSoftKeyboard(){
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
