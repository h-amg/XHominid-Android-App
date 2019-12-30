package com.main.android.activium;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;

import org.bson.Document;

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
import java.util.Date;
import java.util.List;

import eu.amirs.JSON;

public class SubscriptionActivity extends AppCompatActivity {

    private static String TAG = "Subscription Activity";

    private BillingClient billingClient;
    private SkuDetails mSkuDetails;
    private TextView mPrice;
    private TextView not_supported;
    private SpinKitView progBar;
    private LinearLayout content;

    // mongodb
    final StitchAppClient stitchClient = Stitch.getDefaultAppClient();
    final StitchUser user = stitchClient.getAuth().getUser();
    private String uId;

    // Connectivity status
    private ConnectivityManager cm;
    private NetworkInfo activeNetwork;
    private boolean isConnected;

    // Subscription verification attempts
    private int subVerifyAttempt = 0;

    // server billing api url
    final static String uri = "server_url";

    @Override
    protected void onStart() {
        super.onStart();
        chkAuth();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_screen);

        // check network status and set UI data observer
        cm = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        //Get the network status info
        activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        // verify user is authenticated
        chkAuth();

        // declare button view
        Button subBtn;

        // link views
        subBtn = findViewById(R.id.sub_btn);
        mPrice = findViewById(R.id.price);
        not_supported = findViewById(R.id.not_supported);
        progBar = findViewById(R.id.sub_progress_bar);
        content = findViewById(R.id.card_content);


        //initialize billing client
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
                /*Log.d("sub",
                        "purchases is updated");*/
                // if billing response code is ok and purchase is updated
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (!purchase.isAcknowledged()){
                            AcknowledgePurchaseParams params = AcknowledgePurchaseParams
                                    .newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                            billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
                                @Override
                                public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                                    /*Log.d("sub",
                                            "billing response is ok, purchase is acknowledged");*/
                                    handlePurchase(purchase);
                                }
                            });
                        }else{
                            // if purchase is not acknowledged
                            /*Log.d("sub",
                                    "billing response is ok but purchase is not acknowledged");*/
                            /*handlePurchase(purchase);*/
                        }

                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    Toast.makeText(SubscriptionActivity.this,
                            "Error with payment processing", Toast.LENGTH_LONG).show();
                    Log.e("SubscriveActivity",
                            "Error processing payment: " + billingResult.getDebugMessage());
                } else {
                    // Handle any other error codes.
                    Toast.makeText(SubscriptionActivity.this,
                            "Error with payment processing", Toast.LENGTH_LONG).show();
                    Log.e("SubscriveActivity",
                            "Error processing payment: " + billingResult.getDebugMessage());
                }
            }
        }).build();

        // start connection with google plan
        startConnection();

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSkuDetails != null) {
                    purchase(mSkuDetails);
                }else{
                    Log.e("SubscribeActivity", "Error mSkuDetails: " + mSkuDetails);
                }
            }
        });

    }

    // start connection with google play
    private void startConnection(){
        /*Log.d("Sub", "connection triggered");*/
        // start connection with google play
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    /*Log.d("Sub", "BillingSetupFinished response OK");*/
                    // check that the device supports the products.
                    BillingResult supported = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
                    if (supported.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        /*Log.d("Sub", "Device supported");*/
                        // Get product details
                        queryProdDetails();
                    }else{
                        progBar.setVisibility(View.GONE);
                        not_supported.setVisibility(View.VISIBLE);
                        Log.e("SubscribeActivity",
                                "Billing is not supported for this device. Code: " + supported.getResponseCode());
                    }
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play
                /*Log.d("sub",
                        "Billing service disconnected");*/
                startConnection();
            }
        });
    }

    // get product localized details
    private void queryProdDetails(){
        /*Log.d("Sub", "querryDetails triggered");*/
        List<String> skuList = new ArrayList<>(); // sku is if your productId
        skuList.add("fgdgdfg");
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        // handle result
                        if (billingResult.getResponseCode()
                                == BillingClient.BillingResponseCode.OK && skuDetailsList != null){
                            /*Log.d("Sub", "querryDetails reponseOK");
                            Log.d("Sub", "subscriptions list: " + skuDetailsList);*/
                            // loop over products
                            for (SkuDetails skuDetails : skuDetailsList) {

                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                /*String introPrice = skuDetails.getIntroductoryPrice();
                                String introPriceCycle = skuDetails.getIntroductoryPriceCycles();
                                String introPriceperiod = skuDetails.getIntroductoryPricePeriod();
                                String subPeriod = skuDetails.getSubscriptionPeriod();
                                String trialPeriod = skuDetails.getFreeTrialPeriod();*/

                                /*Log.d("Sub", "subscription: " + sku);*/

                                // set UI view for sub product
                                if ("product_id".equals(sku)) {
                                    /*Log.d("sub",
                                            "activium equals product id");*/
                                    // set price on UI
                                    mSkuDetails = skuDetails;
                                    mPrice.setText(price);
                                    progBar.setVisibility(View.GONE);
                                    content.setVisibility(View.VISIBLE);
                                }
                            }
                        }else{
                            Log.e("SubscriptionActivity", "Error retrieving products details");
                            Toast.makeText(SubscriptionActivity.this, "Error occurred," +
                                            " please check your connection", Toast.LENGTH_LONG).show();
                            queryProdDetails();
                        }
                    }
                });
    }

    private void handlePurchase(Purchase purchase){
        /*Log.d("sub",
                "handle purchase triggered");*/
        content.setVisibility(View.GONE);
        progBar.setVisibility(View.VISIBLE);
        String purchaseToken = purchase.getPurchaseToken();
        String orderId = purchase.getOrderId();
        String purchaseName = purchase.getPackageName();
        Date purchaseDate = new Date(purchase.getPurchaseTime());
        String prdouctId = purchase.getSku();
        uId = user.getId(); // set user id
        makeServerCall(uId, purchaseToken, orderId, purchaseName,
                prdouctId, purchaseDate);

        // TODO: verify purchase sever side and send user to main activity
        // TODO: and update user entitlement status
        // TODO: UPDATE USER OURCHASE DOCUEMT WITH PURCHASE DETAILS

        // TODO: r, you must acknowledge all purchases within three days. Failure to properly
        //  acknowledge purchases results in those purchases being refunded.
        // For products that aren't consumed, use acknowledgePurchase(), found in the client API.
        // A new acknowledge() method is also available in the server API.
        // or subscriptions, you must acknowledge any purchase that contains a new purchase token.
        // To determine if a purchase needs acknowledgment, you can check the acknowledgement field in the purchase
        //The Purchase object includes an isAcknowledged() method that indicates whether a purchase has been acknowledged.
        //n addition, the server-side API includes acknowledgement boolean values for Product.purchases.get()
        // and Product.subscriptions.get(). Before acknowledging a purchase, use these methods to determine whether
        // the purchase has already been acknowledged.
    }

    // Start billing flow
    private void purchase(SkuDetails skuDetails){
        /*Log.d("sub",
                "purchase triggered");*/
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(this, flowParams );
    }

    // Query backend server
    public void makeServerCall(String uId, String purchaseToken, String orderId, String purchaseName,
                               String prdouctId, Date purchaseDate) {
        /*Log.d("sub",
                "make server call triggered triggered");*/

        // Create URL object
        URL url = makeUrl(uri);

        // Perform HTTP request to the URL and receive a JSON response back
        makeHTTPRequest(url, uId, purchaseToken, orderId, purchaseName, prdouctId, purchaseDate);

    }

    /*public static String extractResponse (String jsonResponse) {
        String response =  null;
        // If the JSON string is empty or null, then return null value.
        if (!TextUtils.isEmpty(jsonResponse)) {
            // Try parsing the json response
            try {
                String simpleJsonString = jsonResponse;
                JSON json = new JSON(simpleJsonString);
                response = json.getJsonObject().getString("purchase");
            } catch (Exception e) {
                Log.e("MainActiviy", "Problem parsing the JSON results", e);
            }
        }
        return response;
    }*/

    private static URL makeUrl(String urlString){
        URL url = null;
        try {
            url = new URL(urlString);
        }catch(MalformedURLException e){
            Log.e("MainActiviy", "Problem making URL object from string", e);
        }
        return url;
    }

    private String makeHTTPRequest(URL url, String uId, String purchaseToken, String orderId, String purchaseName,
                                   String prdouctId, Date purchaseDate) {
        // handle asynchronously
        new AsyncTask<Void, Void, JsonObject>(){
            @Override
            protected JsonObject doInBackground(Void... voids) {
                JsonObject jsonResponse = null;
                HttpURLConnection urlConnection= null;
                InputStream inputStream = null;

                // If the URL is null, then return early.
                if (url == null) {
                    return jsonResponse;
                }

                // build json object with  query parameters
                String jsonString = "{\"request\": \"\"}";

                //Log.d("SubscribeActivity", "jsonString: " + jsonString);
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
                    //Log.i(MainActivity.class.toString(), jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();
                    // If the request was successful (response code 200),
                    // then read the input stream and parse the response.
                    if (urlConnection.getResponseCode() == 200) {
                        inputStream = urlConnection.getInputStream();
                        /*JsonParser jsonParser = new JsonParser();
                        jsonResponse = jsonParser.parse( new InputStreamReader(inputStream,
                                "UTF-8"));*/
                        try {
                            JsonElement element = new JsonParser().parse(
                                    new InputStreamReader(inputStream)
                            );
                            jsonResponse = element.getAsJsonObject();
                        } catch (Exception e) {
                            Log.e("SubscribtionActivity", "Error extracting " +
                                    "json response from input stream");
                        }
                        /*Log.d("SubscribeActivity", "jsonResponse: " + jsonResponse);*/
                    } else {
                        Log.e("SubscribeActivity", "Error response code: " + urlConnection.getResponseCode());
                    }
                } catch (IOException e) {
                    Log.e("SubscribeActivity", "Problem retrieving the earthquake JSON results.", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }catch(Exception e){
                            Log.e("SubscribeActivity", "inputStream error: " + e);
                        }
                    }
                }
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(JsonObject response) {
                super.onPostExecute(response);
                if (response != null){
                    if (response.get("verified").getAsBoolean() && response.get("fulfilled").getAsBoolean()){
                        sendToMainActivity();
                    }else{
                        Log.e("SubscribeActivity", "Server reponse eroor: " + response.get("message").getAsString());
                    }

                }else{
                    Log.e("SubscriptionActivity", "Error verifying payment");
                }
                //Log.d("MainActiviy", "purchase response: " + response);
            }
        }.execute();

        return null;
    }

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

    private void sendToMainActivity(){
        // if billing is completed and fulfilled successfully send user to main activity
        finish();
        startActivity(new Intent(SubscriptionActivity.this, MainActivity.class));
    }

    private boolean chkAuth(){
        // Check if user is signed in (true). //mongoDB stitch
        if (!stitchClient.getAuth().isLoggedIn()) {
            // user is not signed
            finish();
            //send user to login layout
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return false;
        }else{
            //user is signed in
            return true;
        }
    }

    private void verifySubscription(){
        if(isConnected) {
            //initialize mongoDB remote stitchClient
            RemoteMongoClient mongoDBClient = stitchClient.getServiceClient(RemoteMongoClient.factory,
                    "project/app_name");
            RemoteMongoCollection subscriptionsCollection = mongoDBClient.getDatabase("collection_name")
                    .getCollection("collection_name");

            Document filterDoc = new Document().append("uId", uId);
            Task<Document> findTask = subscriptionsCollection.find(filterDoc).limit(1).first();
            findTask.addOnCompleteListener(new OnCompleteListener<Document>() {
                @Override
                public void onComplete(@NonNull Task<Document> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() == null) {
                            Log.e("MainActivity",
                                    "Could not find any matching subscription document to get" +
                                            " subscription status for user: " + uId);
                            // if verification is attempted 5 times
                            // send to subscription activity on the 6th time
                            if (subVerifyAttempt == 2){
                                // send to subscription screen
                                Toast.makeText(SubscriptionActivity.this, "You don't" +
                                                " have a valid subscription",
                                        Toast.LENGTH_LONG).show();
                                startConnection();
                            }else{
                                // increase attempt count and try again
                                subVerifyAttempt += 1;
                                verifySubscription();
                            }
                        } else {
                            Date subExpiry = task.getResult().getDate("subexpiry");
                            Boolean valid = task.getResult().getBoolean("valid", false);// subscription validity (not cancelled)
                            if (subExpiry != null && valid) {
                                if (subExpiry.compareTo(Calendar.getInstance().getTime()) < 0) {
                                    /*Log.d("subchk", "subscription invalid, subExpiry:" +
                                            " " + subExpiry + " currDay: " + Calendar.getInstance().getTime());*/
                                    // Subscription is invalid start google play billing connection
                                    startConnection();
                                }
                            } else {
                                Toast.makeText(SubscriptionActivity.this, "Error" +
                                        " verifying your subscription", Toast.LENGTH_LONG).show();
                                Log.e(TAG,
                                        "Eroor subExpiry: " + subExpiry + "validity: " + valid);
                            }
                        }
                    } else {
                        Log.e(TAG, "failed to find subscription document with: ", task.getException());
                        // if verification is attempted 5 times
                        // send to subscription activity on the 6th time
                        if (subVerifyAttempt == 6){
                            // send to subscription screen
                            finish();
                            Toast.makeText(SubscriptionActivity.this, "Couldn't" +
                                            " verify your subscription, please restart the app",
                                    Toast.LENGTH_LONG).show();
                        }else{
                            // increase attempt count and try again
                            subVerifyAttempt += 1;
                            verifySubscription();
                        }
                    }
                }
            });
        }else{
            Toast.makeText(this, "No connection found", Toast.LENGTH_LONG).show();
            verifySubscription();
        }
    }
}
