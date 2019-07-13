package com.tripmee.findmee.Mainfeed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.tripmee.findmee.CallBack.ActionCompleteCallBack;
import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.SOS.SOSRequest;
import com.tripmee.findmee.Models.SOS.SOSRequestList;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SOSRequestAdapter extends ArrayAdapter<SOSRequest> implements Filterable{
    private SOSRequest _SOSRequest;
    private SOSRequestList _SOSRequestList;
    private ArrayList<SOSRequest> _originalList;
    private ArrayList<SOSRequest> _filteredList;
    private Button btnDown;
    private int mPosition;
    private UserGlobalHandler UserHandler;
    private ItemFilter mFilter = new ItemFilter();

    public SOSRequestAdapter(Context context, SOSRequestList sosRequestList){
        super(context, 0, sosRequestList.getSOSRequestList());

        _SOSRequestList = sosRequestList;
        _originalList = _SOSRequestList.getSOSRequestList();
        _filteredList = _originalList;
    }

    public int getCount() {

        if(_filteredList == null){
            return 0;
        }
        return _filteredList.size();
    }

    public SOSRequest getItem(int position) {
        return _filteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        mPosition = position;
        _SOSRequest = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.layout_mainfeed_item, parent, false);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //SOSRequest sos = _SOSRequestAdapter.getItem(position); //_SOSRequestList.getSOSRequestList().get(position);
                ViewFeedLocationFragment(_SOSRequest);
            }
        });
        //convertView.setFocusable(false);
        UserHandler = UserGlobalHandler.get_instance();
        TextView Name = (TextView) convertView.findViewById(R.id.textViewUsername);
        TextView textDateTime = (TextView) convertView.findViewById(R.id.textViewDateTime);
        TextView Message = (TextView) convertView.findViewById(R.id.textViewEmergencyText);

        // Populate the data into the template view using the data object
        Name.setText(_SOSRequest.getUserName());
        textDateTime.setText(_SOSRequest.getDateTime().toString());
        Message.setText(_SOSRequest.getMessage());
        LoadImage(convertView);
        SetUserImage(convertView);
        ShowHideDeleteBtn(convertView, _SOSRequest);

        /*btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String msg = getContext().getString(R.string.handshake_request_accept);

                AlertMessage(msg,mUser);

            }
        });

        btnReject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UserHandler.DeleteHandShakeRequest(mUser);
                mRequests.remove(mUser);
                notifyDataSetChanged();

                if (rejectButtonListener != null)
                {
                    rejectButtonListener.OnButtonClickListener(mPosition,mUser);
                }
            }
        });*/
        // Return the completed view to render on screen
        return convertView;
    }
    static class ViewHolder {
        TextView text;
    }

    public Filter getFilter() {
        return mFilter;
    }

    private void LoadImage(View convertView){

        Bitmap bitmap = _SOSRequestList.GetSOSImage(_SOSRequest);
        ImageView imageView = convertView.findViewById(R.id.mainfeedImageView);

        if(bitmap != null) {

            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

        }else{
            imageView.setVisibility(View.GONE);
        }
    }

    private void SetUserImage(final View convertView){

        CircleImageView mCircleImageView = (CircleImageView) convertView.findViewById(R.id.imageViewProfile);
        Bitmap imageBitmap = _SOSRequestList.GetProfileImage(_SOSRequest);
        mCircleImageView.setImageBitmap(imageBitmap);
    }

    private void ShowHideDeleteBtn(View convertView, final SOSRequest sos){

        btnDown = (Button) convertView.findViewById(R.id.btnDown);

        //final SOSRequest sos= getItem(mPosition);

        if(UserHandler.CurrentUser.getUserID().equals(sos.getUserID())) {
            btnDown.setEnabled(true);
            btnDown.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Utility.displayMessage(getContext(),"Delete current sos" + _SOSRequest.getMessage() + "Delete index sos" + sos.getMessage());
                   String msg = getContext().getString(R.string.item_delete);
                   AlertMessage(msg, sos.getId());
                }
            });
        }else{
            btnDown.setEnabled(false);
        }

    }
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            // If the constraint (search string/pattern) is null
            // or its length is 0, i.e., its empty then
            // we just set the `values` property to the
            // original contacts list which contains all of them
            if (constraint == null || constraint.length() == 0) {
                results.values = _originalList;
                results.count = _originalList.size();
            }
            else {
                // Some search copnstraint has been passed
                // so let's filter accordingly
                ArrayList<SOSRequest> filteredRequests = new ArrayList<SOSRequest>();

                // We'll go through all the sosrequests and see
                // if the username or the message contain the supplied string
                for (SOSRequest sos : _originalList) {
                    //if (sos.getUserName().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                    if (sos.getUserName().toLowerCase().contains( constraint.toString().toLowerCase() )) {
                        filteredRequests.add(sos);
                    }else if(sos.getMessage().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredRequests.add(sos);
                    }
                }
                // Finally set the filtered values and size/count
                results.values = filteredRequests;
                results.count = filteredRequests.size();
            }

            // Return our FilterResults object
            return results;

        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            _filteredList = (ArrayList<SOSRequest>) results.values;
            notifyDataSetChanged();
        }

    }

    private void AlertMessage(String message, final Integer ID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                UnitOfWork UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
                UOW.DeleteSOSRequest(ID);
                UOW.deleteActionCallBack = new ActionCompleteCallBack() {
                    @Override
                    public void CallBack() {
                        //Utility.displayMessage(getContext(),"Deleted call back");
                        notifyDataSetChanged();
                    }
                };
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing

                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void ViewFeedLocationFragment(SOSRequest sosRequest) {
        Mainfeed_LocationFragment fragment = new Mainfeed_LocationFragment();
        Bundle arguments = new Bundle();
        arguments.putString("latitude", sosRequest.getLatitude().toString());
        arguments.putString("longitude", sosRequest.getLongitude().toString());
        //arguments.putString("imageurl", sosRequest.getImageUrl());
        fragment.setArguments(arguments);
        try {
            FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }
}
