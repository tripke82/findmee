package com.tripmee.findmee.CrimeReport;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.CrimeReport.CrimeReport;
import com.tripmee.findmee.Models.CrimeReport.CrimeReportList;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;


import java.util.ArrayList;

public class CrimeFeedAdapter extends ArrayAdapter<CrimeReport> implements Filterable {

    private CrimeReport _CrimeReport;
    private CrimeReportList _CrimeReportList;
    private ArrayList<CrimeReport> _originalList;
    private ArrayList<CrimeReport> _filteredList;
    private Button btnShowOptions;
    private TextView UserCommentsLink;
    private int mPosition;
    private UserGlobalHandler UserHandler;
    private UnitOfWork _UOW;

    private CrimeFeedAdapter.ItemFilter mFilter = new CrimeFeedAdapter.ItemFilter();

    public CrimeFeedAdapter(Context context, CrimeReportList crimeReportList){
        super(context, 0, crimeReportList.getCrimeReports());

        UserHandler = UserGlobalHandler.get_instance();
        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);

        _CrimeReportList = crimeReportList;
        _originalList = _CrimeReportList.getCrimeReports();
        _filteredList = _originalList;
    }

    public int getCount() {

        if(_filteredList == null){
            return 0;
        }
        return _filteredList.size();
    }

    public CrimeReport getItem(int position) {
        return _filteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        mPosition = position;
        _CrimeReport = getItem(position);

        _UOW.GetComments(_CrimeReport.getID());

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_crimereport, parent, false);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ViewCrimeDetailFragment(_CrimeReport);

            }
        });
        //convertView.setFocusable(false);

        TextView Name = (TextView) convertView.findViewById(R.id.textViewUsername);
        TextView textDateTime = (TextView) convertView.findViewById(R.id.textViewDateTime);
        TextView Message = (TextView) convertView.findViewById(R.id.textviewCrimeComments);
        UserCommentsLink = (TextView)convertView.findViewById(R.id.usercommentslink);

        // Populate the data into the template view using the data object
        //Name.setText(_CrimeReport.get);
        textDateTime.setText(_CrimeReport.getDateTime().toString());
        Message.setText(_CrimeReport.getComments());
        LoadImage(convertView);
        SetUserImage(convertView);

        //ShowHideDeleteBtn(convertView, _CrimeReport); may be only show when user click to view the item

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

        Bitmap bitmap = _CrimeReport.GetImage();
        ImageView imageView = convertView.findViewById(R.id.imageviewCrime);

        if(bitmap != null) {

            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

        }else{
            imageView.setVisibility(View.GONE);
        }
    }

    private void GeCommentsCount(View convertView){

        Bitmap bitmap = _CrimeReport.GetImage();
        ImageView imageView = convertView.findViewById(R.id.imageviewCrime);

        if(bitmap != null) {

            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

        }else{
            imageView.setVisibility(View.GONE);
        }
    }

    private void SetUserImage(final View convertView){

        //CircleImageView mCircleImageView = (CircleImageView) convertView.findViewById(R.id.imageViewProfile);
        //Bitmap imageBitmap = _SOSRequestList.GetProfileImage(_SOSRequest);
        //mCircleImageView.setImageBitmap(imageBitmap);
    }

    private void ShowHideDeleteBtn(View convertView, final CrimeReport report){

        btnShowOptions = (Button) convertView.findViewById(R.id.btnShowOptions);

        if(UserHandler.CurrentUser.getUserID().equals(report.getUserID())) {
            btnShowOptions.setEnabled(true);
            btnShowOptions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Utility.displayMessage(getContext(),"Delete current sos" + _SOSRequest.getMessage() + "Delete index sos" + sos.getMessage());
                    String msg = getContext().getString(R.string.item_delete);
                    //AlertMessage(msg, report.getID());
                }
            });
        }else{
            btnShowOptions.setEnabled(false);
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
                ArrayList<CrimeReport> filteredRequests = new ArrayList<CrimeReport>();

                // We'll go through all the sosrequests and see
                // if the username or the message contain the supplied string
                for (CrimeReport report : _originalList) {
                    //if (sos.getUserName().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                    if (report.getComments().toLowerCase().contains( constraint.toString().toLowerCase() )) {
                        filteredRequests.add(report);
                    }
                    //else if(sos.getMessage().toLowerCase().contains(constraint.toString().toLowerCase())){
                        //filteredRequests.add(sos);
                    //}
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
            _filteredList = (ArrayList<CrimeReport>) results.values;
            notifyDataSetChanged();
        }

    }

    /*private void AlertMessage(String message, final Integer ID) {
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
    }*/

    /*private void ViewCrimeDetailFragment(CrimeReport report) {
        CrimeDetailFragment fragment = new CrimeDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString("UserID", report.getUserID());
        arguments.putString("ID", report.getID().toString());
        fragment.setArguments(arguments);

        try {
            android.support.v4.app.FragmentManager fragmentManager =
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } catch (Exception exception) {

            String msg = exception.getMessage();
        }
    }*/

}
