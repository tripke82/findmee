package com.tripmee.findmee.FriendRequests;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.tripmee.findmee.Handlers.UserGlobalHandler;
import com.tripmee.findmee.Models.MyContact;
import com.tripmee.findmee.Models.User;
import com.tripmee.findmee.Network.UnitOfWork;
import com.tripmee.findmee.R;
import com.tripmee.findmee.Utilities.Phone;
import com.tripmee.findmee.Utilities.Utility;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhoneContactAdapter extends ArrayAdapter<MyContact> implements Filterable {

    private ArrayList<MyContact> _Contacts;
    private ArrayList<MyContact> _OriginalList;
    private ArrayList<MyContact> _FilteredList;
    private ItemFilter mFilter = new ItemFilter();
    private int mPosition;
    private UserGlobalHandler UserHandler;
    private MyContact _MyContact;
    private UnitOfWork _UOW;
    private LayoutInflater myInflater;

    private Button btnFriendRequest;

    public PhoneContactAdapter(Context context, ArrayList<MyContact> Contacts){
        super(context, 0, Contacts);

        UserHandler = UserGlobalHandler.get_instance();
        _UOW = UnitOfWork.get_instance(UserHandler.CurrentUser);
        _Contacts = Contacts;
        _OriginalList = _Contacts;
        _FilteredList = _Contacts;

        myInflater = LayoutInflater.from(context);

    }

    public int getCount() {

        if(_FilteredList == null){
            return 0;
        }
        return _FilteredList.size();
    }

    public MyContact getItem(int position) {
        return _FilteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // for some reason, if buttons text, color or visibility change under if else logic, it doesn't work here due to memory being recycled
        // so if else statements changes the values and those variables are assigned to buttons. Thats the only way it could work.

        final ViewHolder holder;

        mPosition = position;
        _MyContact = getItem(position);

        if (convertView == null) {
            convertView = myInflater.inflate(R.layout.item_phonecontact, null);
            holder = new ViewHolder();

            holder.Name = (TextView) convertView.findViewById(R.id.name_listitem);
            holder.btnFriendRequest = (Button) convertView.findViewById(R.id.btnRequestFriend);


            holder.btnFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    holder.btnFriendRequest.setEnabled(false);
                    final int position = (int)v.getTag();
                    final MyContact contact = getItem(position);

                    String PhoneNumber = Phone.ReplaceCountryCode(contact.getMobile());

                    _UOW._RetrofitService.getUserByMobile(PhoneNumber,UserHandler.CurrentUser.getToken(), new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {

                            final User responseUser = response.body();
                            if (response.isSuccessful() && responseUser != null) {
                                Utility.displayMessage(getContext(),"User found:" + responseUser.getUserName());
                                UserHandler.ProcessFriendRequest(responseUser, getContext());
                                holder.btnFriendRequest.setText(getContext().getResources().getText(R.string.btn_Sent));
                                holder.btnFriendRequest.setBackgroundResource(R.drawable.rounded_transparent_button);
                                holder.btnFriendRequest.setTextColor(Color.BLACK);
                                MyContact contact = getItem(position);
                                contact.setFriendRequested(true);
                            } else {               //User doesn't exist

                                Toast.makeText(getContext(), getContext().getString(R.string.user_not_joined_msg), Toast.LENGTH_LONG).show();
                                holder.btnFriendRequest.setEnabled(true);
                                String Message = "Hi " + contact.getUserName()+ ", " + getContext().getString(R.string.SMS_invitation);
                                Phone.LunchSMSActivity((Activity) getContext(),Message, contact.getUserName());
                            }
                        }
                        @Override
                        public void onFailure(Call<User> call, Throwable t) {

                        }
                    });

                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.Contact = getItem(position);
        holder.Name.setText(holder.Contact.getUserName());
        holder.btnFriendRequest.setTag(position);

        String buttonText = getContext().getResources().getText(R.string.btn_AddFriend).toString();

        int Visibility = View.VISIBLE;
        int BackgroundR = R.drawable.rounded_rect_button;
        int fontColor = Color.WHITE;

        if(holder.Contact.isFriendAdded()){
            buttonText = getContext().getResources().getText(R.string.btn_Added).toString();
            BackgroundR = R.drawable.rounded_transparent_button;
            fontColor = Color.BLACK;
        }else if(holder.Contact.isFriendRequested()){
            buttonText = getContext().getResources().getText(R.string.btn_Sent).toString();
            BackgroundR = R.drawable.rounded_transparent_button;
            fontColor = Color.BLACK;
        }else if(holder.Contact.isFriendRequestReceived()){
          Visibility = View.INVISIBLE;
        }

        holder.btnFriendRequest.setVisibility(Visibility);
        holder.btnFriendRequest.setText(buttonText);
        holder.btnFriendRequest.setEnabled(holder.Contact.AllowedRequest());
        holder.btnFriendRequest.setBackgroundResource(BackgroundR);
        holder.btnFriendRequest.setTextColor(fontColor);
        return convertView;
    }

    static class ViewHolder {
        TextView Name;
        Button btnFriendRequest;
        MyContact Contact;
    }

    public Filter getFilter() {
        return mFilter;
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
                results.values = _OriginalList;
                results.count = _OriginalList.size();
            }
            else {
                // Some search copnstraint has been passed
                // so let's filter accordingly
                ArrayList<User> filteredRequests = new ArrayList<User>();

                // We'll go through all the sosrequests and see
                // if the username or the message contain the supplied string
                for (User user : _OriginalList) {
                    //if (sos.getUserName().toUpperCase().contains( constraint.toString().toUpperCase() )) {
                    if (user.getUserName().toLowerCase().contains( constraint.toString().toLowerCase() )) {
                        filteredRequests.add(user);
                    }else if(user.getUserName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredRequests.add(user);
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
            _FilteredList = (ArrayList<MyContact>) results.values;
            notifyDataSetChanged();
        }

    }

}
