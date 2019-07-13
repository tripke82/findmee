package com.tripmee.findmee.GlobalConstants;

import static android.Manifest.permission;


public class GlobalConstants {

    public final static int PERMISSION_ALL = 1;
    public final static String[] PERMISSIONS = {permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION, permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE, permission.CAMERA, permission.CALL_PHONE};

    //reseller database columns
    public static final String RESELLERS  = "Resellers";
    public static final String STORE_ID  = "StoreID";
    public static final String STORE_NAME  = "Store_Name";
    public static final String ADDRESS  = "Address";
    public static final String MANAGER_NAME  = "Manager_Name";
    public static final String CONTACT_NUMBER  = "Phone_Number";
    //-----

    //sales database columns
    public static final String SALES  = "Sales";
    public static final String SALE_ID  = "SaleID"; //customerid is salesid
    //public static final String CUSTOMER_ID = "CustomerID";
    public static final String CUSTOMERNAME  = "Customer_Name";
    public static final String CUSTOMERPHONE = "Customer_Phone";
    public static final String PRICE = "Price";
    public static final String TRANSACTIONDATE = "Transaction_Date";
    //---
    public static final double DEFAULT_PRICE = 12000;

    public static final String EMERGENCY_MSG_SENT  = "An Emergency Message is Sent";
    //public static final String EMERGENCY_PHOTO_SENT  = "The Captured Photo is Sent";
    public static final String SIM_NOT_FOUND  = "Please insert a Sim Card";
    public static final String ERROR_OCCURED  = "ERROR OCCURED";
    public static final String APP_NOT_CONNECTED  = "This App is not connected to any phone";
    public static final String APP_CONNECTED  = "The App is connected";
    public static final String IMAGE_DIRECTORY_NAME ="Emergency";
    public static final String DATE_TIME = "DateTime";
    public static final String ACCEPT_STATUS  = "Accepted";
    public static final String USERS  = "Users";
    public static final String ERRORS  = "Errors";

    public static final String EMERGENCY_REQUEST  = "Emergency_Request";
    public static final String HANDSHAKE_REQUEST  = "HandShake_Request";
    public static final String STATUS_CHECKED  = "Status_Checked";
    public static final String STATUS_SHOWED  = "Status_Showed";

    public static final String REQUESTED_HANDSHAKES = "Requested_HandShakes"; // database fields
    //public static final String HANDSHAKE_SUCCESS  = "Hand shake has been accepted. Now you can send findmee alert to your designated contact";
    public static final String REQUESTS  = "Requests";
    public static final String MOBILE  = "Mobile";
    public static final String EMAIL  = "Email";
    public static final String IMAGE_URL  = "ImageUrl";
    public static final String USER_ID  = "UserID";
    public static final String EMERGENCY_CONTACT  = "Emergency_Contact";
    public static final String EMERGENCY_REQUEST_ID  = "RequestID";
    public static final String NAME  = "Name";
    public static final String REGISTERED_DATE  = "Registered_Date";
    public static final String ACTIVATED_DATE  = "Activated_Date";
    public static final String LATITUDE  = "Latitude";
    public static final String LONGITUDE  = "Longitude";
    public static final String TRUE  = "true";
    public static final String FALSE  = "false";
    public static final String DEFAULT_STORE_NUMBER = "09783916968";


    public static final int IMAGE_MAX_BITMAP_DIMENSION = 1024;
    public static final int LISTVIEW_IMAGE_SIZE = 504;
    public static final int CRIME_IMAGE_SIZE = 704;
    public static final int ONE_YEAR_MEMBERSHIP = 365;
    public static final int TWENTYFOUR_HOURS = 24;
    public static final int EMERGENCY_REQUEST_MIN_FIELDS = 6;
    public static final float DEFAULT_ZOOM = 12f; //16.5
    public static final int MARKER_MAX_ENTRIES = 10;
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String CRIME_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final int PICK_IMAGE_REQUEST = 111;

    public static final String EMERGENCY_NOTIFICATION_CHANNEL = "Emergency Alert";
    public static final String EMERGENCY_NOTIFICATION_CHANNEL_ID = "Emergency_Channel_01";

    public static final String SELECTED_FRAGMENT = "SelectedFragment";
    public static final String NOTIFICATION_FRAGMENT = "NotificationFragment";
    public static final String MAINFEED_FRAGMENT = "MainfeedFragment";
    public static final String VIEWIMAGE_FRAGMENT = "ViewImageFragment";
    public static final String LOCATION_FRAGMENT = "LocationFragment";
    //public static final String STORAGE_CONNECTION_STRING="DefaultEndpointsProtocol=https;AccountName=safieblobstorage777;" +
            //"AccountKey=zVhCPg/lImCfkeRf86TODYbQpTW+QQ1+nwsAMZMkdxrCIjq0YdhS8QEnPgif4cqs0sycNEqPUOH9QbkmzfPQhw==;" +
            //"EndpointSuffix=core.windows.net";

    public static final String MYANMAR_COUNTRY_CODE = "+95";

    public static final String FACEBOOK_TOKEN = "facebooktoken";

    public static final String STORAGE_CONNECTION_STRING= "DefaultEndpointsProtocol=https;AccountName=safieblobstorage777;" +
            "AccountKey=z0xU5f01ZwUxW0fZcyAIFFnTRnH3z6kjDJQAn+kip10fXb/tzzrAhQx9xweSTwrei/XGnJDqwqCP85yhYE+kKg==;" +
            "EndpointSuffix=core.windows.net";

    public static final String SMS_INVITATION = "Please download the findmee app - Safie http://bit.ly/safie. In findmee situations we can " +
            "send/ recieve SOS signals to each other. I already put you as an findmee contact.";
}
