package com.main.android.activium;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.protobuf.StringValue;
import com.main.android.activium.Adapters.ChatAdapter;
import com.main.android.activium.dialogs.FeedbackDialog;
import com.main.android.activium.dialogs.UpdateDetailsDialog;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateOptions;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import com.twilio.chat.CallbackListener;
import com.twilio.chat.Channel;
import com.twilio.chat.ChannelListener;
import com.twilio.chat.ChatClient;
import com.twilio.chat.ErrorInfo;
import com.twilio.chat.Message;
import com.twilio.chat.StatusListener;

import org.bson.Document;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import eu.amirs.JSON;


public class MessagesActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener,
        FeedbackDialog.InterFeedBack, UpdateDetailsDialog.UpdateDetailsInterface{

    private static final String TAG = "MessagesActivity";

    private ArrayList<Message> messagesArrayList;
    private EditText editText;
    private ListView list;
    private ChatAdapter arrayAdapter;
    private TextView chateeName;
    private Toolbar toolbar;
    private DrawerLayout drawer;

    private Intent menuIIntent;
    // shareIntent intent for app sharing
    private Intent shareIntent;

    private boolean authenticated = false;

    // MongoDB variables
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    private RemoteMongoClient mongoDBClient;
    private RemoteMongoCollection usersCollection;
    private RemoteMongoCollection subscriptionsCollection;
    final StitchUser user = stitchClient.getAuth().getUser();
    private SharedPreferences chkPoint;

    // Drawer variables
    private String uId;
    private String email;
    private TextView userEmailTextView; // drawer user email txt view
    private ImageView profilePhoto; // drawer user profile photo
    private String profileImgUrl;
    private NavigationView navigationView;
    private View headerView;

    // update_details drawer click check
    private boolean isUpdateDialog;

    /// Server url
    final static String SERVER_TOKEN_URL = "token_server_url";

    // channel channel id
    private String CHANNEL_ID;

    // Update this identity for each individual user, for instance after they login
    private String mIdentity;

    private Channel mChannel;

    private SpinKitView loading_anim;

    private int userDocAttempts = 0;
    private int channelIdSaveAttempts = 0;


    /**User authentication ON APP launch*/
    // check if the user is logged in when the app is launched
    @Override
    public void onStart() {
        super.onStart();
        if(!authenticated) {
            authenticated = chkAuth();
        }
    }
    // check if the user is logged in when the resumes
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(!authenticated) {
            authenticated = chkAuth();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Get chatee name
        /*intent = getIntent();
        String contactName = intent.getStringExtra("chateeName");*/

        toolbar = findViewById(R.id.toolbar_messages);
        drawer = findViewById(R.id.messages_layout); //references messages activity
        navigationView = findViewById(R.id.nav_view_messages_activity);
        headerView = navigationView.getHeaderView(0);
        userEmailTextView = headerView.findViewById(R.id.user_email);
        profilePhoto = headerView.findViewById(R.id.profile_pc);
        editText = findViewById(R.id.chatBox);
        list = findViewById(R.id.list);
        loading_anim = findViewById(R.id.chat_loading_progr);

        setSupportActionBar(toolbar);
        // setup toolbar and drawer
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.chat_toolbar, null);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        if (profileImgUrl != null){
            // set drawer profile image
            Glide.with(this).load(profileImgUrl).apply(new RequestOptions()
                    .override(150, 150)).apply(RequestOptions.circleCropTransform())
                    .into(profilePhoto);
        }else {
            // load default image or ask user for profile image
        }
        if(email != null){
            // set drawer user email
            userEmailTextView.setText(email);
        }else {
            Log.e("MainActivity",
                    "error setting use email to drawer.value: "  + userEmailTextView);
        }
        // menu hamburger button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_black_36);
            getSupportActionBar().setCustomView(mCustomView);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
        }
        //set up drawer_menu Navigation listener
        setNavigationViewListener();
        // Drawer state change listener
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override public void onDrawerOpened(View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
            @Override
            public void onDrawerClosed(View drawerView) {
                //Set your new fragment here
                if (menuIIntent != null && menuIIntent.resolveActivity(getPackageManager()) != null && shareIntent == null) {
                    finish();
                    startActivity(menuIIntent);
                    menuIIntent = null;
                }else if (menuIIntent != null && menuIIntent.resolveActivity(getPackageManager()) != null && shareIntent != null){
                    finish();
                    startActivity(shareIntent);
                    shareIntent = null;
                }
                if(isUpdateDialog){
                    UpdateDetailsDialog updateDetailsDialog = new UpdateDetailsDialog();
                    updateDetailsDialog.show(getSupportFragmentManager(), "Update Details");
                    isUpdateDialog = false;
                }
            }
        });

        messagesArrayList = new ArrayList<Message>();
        arrayAdapter = new ChatAdapter(this, messagesArrayList,uId);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        /*list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(android.view.ActionMode mode, int position, long id, boolean checked) {
                if(checked){
                    count = count + 1;
                }
                else
                    count =count-1;
                mode.setTitle(count+"");
                mode.setSubtitle(null);
                mode.setTag(false);
                mode.setTitleOptionalHint(false);
            }

            *//*@Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                MenuInflater inflater=mode.getMenuInflater();
                inflater.inflate(R.menu.chat_cab, menu);
                return true;
            }*//*

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            *//*@Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.reply:
                        return true;
                    case R.id.star_message:
                        return true;
                    case R.id.info:
                        return true;
                    case R.id.delete:
                        return true;
                    case R.id.copy:
                        return true;
                    case R.id.forward:
                        return true;
                    default:
                        return false;
                }
            }*//*

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {

            }
        });*/

        /*chateeName = mCustomView.findViewById(R.id.name);
        chateeName.setText(contactName);*/

        // install newest security provider
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        }catch(Exception e){
            Log.e(TAG, "Error, google play service not available");
        }

        // get channel id
        getChatChannelId();
    }

    // Check client callback
    private  CallbackListener<ChatClient> mChatClientCallback = new CallbackListener<ChatClient>() {
        @Override
        public void onSuccess(ChatClient chatClient) {
            /*Log.d(TAG, "Success creating Twilio Chat Client");*/
            if (CHANNEL_ID == null) {
                createChannel(chatClient);
            }else{
                loadChannels(chatClient);
            }

        }
        @Override
        public void onError(ErrorInfo errorInfo) {
            Log.e(TAG,"Error creating Twilio Chat Client: " + errorInfo.getMessage());
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        return true;
    }

    public void sendMessage(View v){
        if (mChannel != null) {
            String message = editText.getText().toString();
            JSONObject user = new JSONObject();
            try {
                user.put("uId", uId);
            }catch(Exception e){
                Log.e(TAG, "Error adding user attribute to message");
            }
            Message.Options options = Message.options().withBody(message).withAttributes(user);
            // Clear edit text box
            editText.setText("");
            // TODO: show sending animation/text
            mChannel.getMessages().sendMessage(options, new CallbackListener<Message>() {
                @Override
                public void onSuccess(Message message) {
                    /*Log.d(TAG, "message sent: " + message.getMessageBody());*/
                    MessagesActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // TODO: hide sending animation/text
                        }
                    });
                }
            });
        }else{
            Log.e(TAG,"mChannel is null!");
            Toast.makeText(this, "Loading messages...", Toast.LENGTH_LONG).show();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            /*case R.id.chat_viewcontact:
                startActivity(new Intent(this,ViewContact.class));
                return true;*/
            case R.id.chat_media:
                return true;
            case R.id.chat_search:
                return true;
            case R.id.chat_mute:
                //TODO add custom dialog box
                return true;
            case R.id.chat_wallpaper:
                return true;
            case R.id.chat_block:
                //TODO add custom dialog box
                return true;
            case R.id.chat_clearchat:
                return true;
            case R.id.chat_emailchat:
                return true;
            case R.id.chat_addshortcut:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    public void backPress(View v){
        super.onBackPressed();
    }

    /*public void profileClick(View v){
        startActivity(new Intent(this, ViewContact.class));
    }*/

    private boolean chkAuth(){
        // Check if user is signed in (true). //mongoDB
        if (!stitchClient.getAuth().isLoggedIn()) {
            finish();
            //send user to login layout
            startActivity(new Intent(this, LoginActivity.class));
            return  false;
        }else{
            // TODO: check subscription
           /* // check intro is hsown and check subscription
            showIntroAndSubchk();*/
            // verify subscription with google play billing
            /*if(validSub()){
                Log.d(TAG, "User subscription is valid");
            }else{
                Log.d(TAG, "User subscription is invalid");
            }*/
            if(user != null){
                uId = user.getId();
                mIdentity = uId;
                /*Log.d("MainActivity", "user Id: " + uId);*/
                email = user.getProfile().getEmail();
                profileImgUrl = user.getProfile().getPictureUrl();
                return  true;
            }else{
                Log.e("MainActivity", "Iser is authenticated but user value is null");
                stitchClient.getAuth().logout(); // log the user out
                //send user to login layout
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return  false;
            }
        }
    }

    private void setNavigationViewListener() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        /*int id = item.getItemId();*/
        switch (item.getItemId()) {
            case R.id.nav_home:
                menuIIntent = new Intent(this, MainActivity.class);
                break;
            case R.id.nav_meal_plans:
                menuIIntent = new Intent(this, MealPlansActivity.class);
                break;
            case R.id.nav_shopping_list:
                menuIIntent = new Intent(this, ShoppingListActivity.class);
                break;
            /*case R.id.nav_ml_record:
                menuIIntent = new Intent(this, MealsRecordActivity.class);
                break;*/
            /*case R.id.nav_dt_stats:
                menuIIntent = new Intent(this, DietStatsActivity.class);
                break;*/
            case R.id.nav_counselling:
                menuIIntent = new Intent(this, ConsultationActivity.class);
                break;
            case R.id.nav_update_details:
                isUpdateDialog = true;
                break;
            case R.id.nav_settings:
                menuIIntent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.nav_share:
                menuIIntent = new Intent(Intent.ACTION_SEND);
                menuIIntent.setType("text/plain");
                menuIIntent.putExtra(Intent.EXTRA_SUBJECT, "Activium");
                String shareMessage= "\nHey! you might wanna try this out.\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
                menuIIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                String title = getResources().getString(R.string.chooser_title);
                shareIntent = Intent.createChooser(menuIIntent, title);
                break;
            case R.id.nav_feedback:
                FeedbackDialog feedBackDialog = new FeedbackDialog();
                feedBackDialog.show(getSupportFragmentManager(), "feedBack");
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void triggerSuccessToast() {
        Toast.makeText(MessagesActivity.this, "FeedBack Sent. Thank you",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void triggerFailureToast() {
        Toast.makeText(MessagesActivity.this, "Failed sending feedback!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboard() {
        hideSoftKeyboard(MessagesActivity.this);
    }

    public static void hideSoftKeyboard(Activity activity) {
        try{
            InputMethodManager inputMethodManager = (InputMethodManager)
                    activity.getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getWindowToken(), 0);
        }catch(Exception e){
            Log.e(TAG, "Hide soft keyboard error: " + e);
        }
    }

    @Override
    public void detailsSuccessUpdate() {
        Toast.makeText(MessagesActivity.this, "Details updated successfully",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void detailsFailureUpdate() {
        Toast.makeText(MessagesActivity.this, "Failed updating details!",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideKeyboardUpdate() {
        hideSoftKeyboard(MessagesActivity.this);
    }

    /*private void retrieveAccessTokenfromServer() {

        // Check this link to load SSL certificate https://developer.android.com/training/articles/security-ssl

        JsonObject queryObject = new JsonObject();
        queryObject.addProperty("identity", mIdentity);
        queryObject.addProperty("chat", true);

        Ion.with(this)
                .load(SERVER_TOKEN_URL)
                .setJsonObjectBody(queryObject)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e == null) {
                            Log.d(TAG, "Server response: " + result.toString());

                            String accessToken = result.get("token").getAsString();

                            //Log.d(TAG, "Retrieved access token from server: " + accessToken);

                            ChatClient.Properties.Builder builder = new ChatClient
                                    .Properties.Builder();
                            ChatClient.Properties props = builder.createProperties();
                            ChatClient.create(MessagesActivity.this,accessToken,props
                                    ,mChatClientCallback);

                        } else {
                            Log.e(TAG,"Error retrieving access token from server" +
                                    e.getMessage(),e);
                            Toast.makeText(MessagesActivity.this,
                                    R.string.error_retrieving_access_token, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }*/

    private void retrieveAccessTokenfromServer() {

        // Create URL object
        URL url = makeUrl(SERVER_TOKEN_URL);

        // handle asynchronously
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String jsonResponse = "";
                HttpURLConnection urlConnection= null;
                InputStream inputStream = null;

                // If the URL is null, then return early.
                if (url == null) {
                    return jsonResponse;
                }

                // build json object with  query parameters
                String jsonString = "{\"identity\": \"" + mIdentity + "\", " +
                        "\"chat\": \"" + true + "\"}";

                /*Log.d("MainActiviy", "uriString: " + jsonString);*/
                JSON jsonParam = new JSON(jsonString);

                try{
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000 /* milliseconds */);
                    urlConnection.setConnectTimeout(15000 /* milliseconds */);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    Log.i(MainActivity.class.toString(), jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();
                    // If the request was successful (response code 200),
                    // then read the input stream and parse the response.
                    if (urlConnection.getResponseCode() == 200) {
                        inputStream = urlConnection.getInputStream();
                        jsonResponse = readFromStream(inputStream);
                        /*Log.d(TAG, "jsonResponse: " + jsonResponse);*/
                    } else {
                        Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Problem retrieving the access token.", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }catch(Exception e){
                            Log.e(TAG, "inputStream error: " + e);
                        }
                    }
                }
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                JSON jsonResponse = new JSON(response);
                String accessToken;
                try {
                    accessToken = jsonResponse.getJsonObject().getString("token");
                }catch (Exception e){
                    accessToken = null;
                    Log.e(TAG, "Error processing server json response");
                }
                /*Log.d(TAG, "server response: " + responseString);*/
                if (accessToken != null) {
                    /*Log.d(TAG, "Server response: " + jsonResponse.toString());
                    Log.d(TAG, "Retrieved access token from server: " + accessToken);*/

                    ChatClient.Properties.Builder builder = new ChatClient
                            .Properties.Builder();
                    ChatClient.Properties props = builder.createProperties();
                    ChatClient.create(MessagesActivity.this,accessToken,props
                            ,mChatClientCallback);

                } else {
                    Log.e(TAG,"Error retrieving access token from server");
                    Toast.makeText(MessagesActivity.this,
                            R.string.error_retrieving_access_token, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private static URL makeUrl(String urlString){
        URL url = null;
        try {
            url = new URL(urlString);
        }catch(MalformedURLException e){
            Log.e(TAG, "Problem making URL object from string", e);
        }
        return url;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    private void loadChannels(ChatClient mChatClient) {
        mChatClient.getChannels().getChannel(CHANNEL_ID, new CallbackListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) {
                channel.addListener(mDefaultChannelListener);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG, "Error retrieving channel: " + errorInfo.getMessage());
            }

        });
    }

    private void loadRecentMessages(Channel channel){
        // Load recent messages
        try {
            channel.getMessages().getLastMessages(50, new CallbackListener<List<Message>>() {
                @Override
                public void onSuccess(List<Message> messages) {
                    if (messages.size() > 0) {
                        for (Message message: messages) {
                            // add messages
                            messagesArrayList.add(message);
                            arrayAdapter.notifyDataSetChanged();
                            loading_anim.setVisibility(View.GONE);
                            list.setVisibility(View.VISIBLE);
                            // Enable send messages button
                            mChannel = channel;
                        }
                    } else {
                        /*Log.d(TAG, "no messages found");*/
                        loading_anim.setVisibility(View.GONE);
                        list.setVisibility(View.VISIBLE);
                        // Enable send messages button
                        mChannel = channel;
                    }
                }

                @Override
                public void onError(ErrorInfo errorInfo) {
                    super.onError(errorInfo);
                    Log.e(TAG, "Error retrieving recent messages " + errorInfo);
                    loading_anim.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                    // Enable send messages button
                    mChannel = channel;
                }
            });
        }catch(Exception e){
            Log.e(TAG, "Exception retrieving recent messages: " + e);
            loading_anim.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
            // Enable send messages button
            mChannel = channel;
        }
    }


    private void joinChannel(Channel channel, String channelId, ChatClient chatClient) {
        channel.join(new StatusListener() {
            @Override
            public void onSuccess() {
                /*Log.d(TAG, "Joined channel");*/
                saveChannelId(channelId, chatClient);
            }

            @Override
            public void onError(ErrorInfo errorInfo) {
                Log.e(TAG,"Error joining channel: " + errorInfo.getMessage());
            }
        });
    }

    private void createChannel(ChatClient chatClient){
        chatClient.getChannels().createChannel(uId,
                Channel.ChannelType.PUBLIC, new CallbackListener<Channel>() {
                    @Override
                    public void onSuccess(Channel channel) {
                        if (channel != null) {
                            joinChannel(channel, channel.getSid(), chatClient);
                        }else{
                            Log.e(TAG, "Error, channel create is null");
                            Toast.makeText(MessagesActivity.this, "Error starting" +
                                            " messages", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onError(ErrorInfo errorInfo) {
                        Log.e(TAG,"Error creating channel: " + errorInfo.getMessage());
                        Toast.makeText(MessagesActivity.this, "Error starting messages",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private ChannelListener mDefaultChannelListener = new ChannelListener() {
        @Override
        public void onMemberAdded(com.twilio.chat.Member member) {
            /*Log.d(TAG, "Member added: " + member.getIdentity());*/
        }
        @Override
        public void onMemberUpdated(com.twilio.chat.Member member,
                                    com.twilio.chat.Member.UpdateReason updateReason) {
            /*Log.d(TAG, "Member updated: " + member.getIdentity());*/
        }
        @Override
        public void onMemberDeleted(com.twilio.chat.Member member) {
            /*Log.d(TAG, "Member deleted: " + member.getIdentity());*/
        }
        @Override
        public void onTypingStarted(Channel channel, com.twilio.chat.Member member) {
            /*Log.d(TAG, "Started Typing: " + member.getIdentity());*/
        }
        @Override
        public void onTypingEnded(Channel channel, com.twilio.chat.Member member) {
            /*Log.d(TAG, "Ended Typing: " + member.getIdentity());*/
        }
        @Override
        public void onMessageAdded(final Message message) {
            /*Log.d(TAG, "Message added");*/
            MessagesActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // need to modify user interface elements on the UI thread
                    messagesArrayList.add(message);
                    arrayAdapter.notifyDataSetChanged();
                    list.setSelection(list.getAdapter().getCount()-1);
                }
            });
        }
        @Override
        public void onMessageUpdated(Message message, Message.UpdateReason updateReason) {
            /*Log.d(TAG, "Message updated: " + message.getMessageBody());*/
        }
        @Override
        public void onMessageDeleted(Message message) {
            //Log.d(TAG, "Message deleted");
        }
        @Override
        public void onSynchronizationChanged(Channel channel) {
            if (channel.getSynchronizationStatus() == Channel.SynchronizationStatus.ALL){
                loadRecentMessages(channel);
            }else{
                Log.e(TAG, "Channel synchronization failed, sync status: " +
                        channel.getSynchronizationStatus().getValue());
            }
        }
    };


    private void getChatChannelId(){
        //initialize mongoDB remote stitchClient
        mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                "project/app_name");
        usersCollection = mongoDBClient.getDatabase("database_name")
                .getCollection("collection_name");

        Task <Document> findTask = usersCollection.find().limit(1).first();
        findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
            @Override
            public void onComplete(@NonNull Task <Document> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null) {
                        // Find user document
                        if (userDocAttempts < 6) {
                            getChatChannelId();
                            userDocAttempts += 1;
                        }else {
                            Toast.makeText(MessagesActivity.this, "Connection error",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Could not find user document");
                        }
                    } else {
                        try {
                            CHANNEL_ID = task.getResult().getString("chatChannelId");
                        }catch(Exception e){
                            CHANNEL_ID = null;
                            /*Log.d(TAG, "chat channel id not found in user document: "  + e);*/
                        }
                        /*Log.d(TAG, "Success finding user document, chat channel id: " + CHANNEL_ID);*/
                        // get access token from server
                        retrieveAccessTokenfromServer();

                    }
                } else {
                    Log.e(TAG, "failed to find user document with: ", task.getException());
                }
            }
        });

    }

    // create user document and diet document
    private void saveChannelId(String channelId, ChatClient chatClient){

        Document filterDoc = new Document("uId", uId);
        Document updateDoc = new Document()
                .append("$set", new Document()
                        .append("chatChannelId", channelId)
                        .append("lastModified", Calendar.getInstance().getTime()));

        RemoteUpdateOptions options = new RemoteUpdateOptions().upsert(true);

        final Task <RemoteUpdateResult> updateTask = usersCollection.updateOne(
                filterDoc, updateDoc, options);
        updateTask.addOnCompleteListener(new OnCompleteListener <RemoteUpdateResult> () {
            @Override
            public void onComplete(@NonNull Task <RemoteUpdateResult> task) {
                if (task.isSuccessful()) {
                    // get user access token and load chat channel
                    CHANNEL_ID = channelId;
                    loadChannels(chatClient);
                    /*Log.d(TAG, "channel id successfully saved");*/
                } else {
                    if(channelIdSaveAttempts < 6){
                        saveChannelId(channelId, chatClient);
                    }else{
                        Log.e(TAG, "failed saving channel id");
                        Toast.makeText(MessagesActivity.this, "Error starting" +
                                " messages", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }


}
