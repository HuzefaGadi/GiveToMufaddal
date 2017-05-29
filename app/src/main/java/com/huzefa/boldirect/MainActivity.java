package com.huzefa.boldirect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends Activity implements CustomWebView.Listener {
    private String url = "https://app.boldirect.com";
    private CustomWebView mWebView;
    RestApi restApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://dashboard.boldirect.com/")
                .build();

        restApi = retrofit.create(RestApi.class);

        mWebView = (CustomWebView) findViewById(R.id.webview);
        mWebView.setListener(this, this);
        mWebView.loadUrl(url);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 0);
        }

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // ...
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        // ...
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        // ...
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
        // ...
    }

    @Override
    public void onBackPressed() {
        return;
        /*if (!mWebView.onBackPressed()) {
            return;
        }
        // ...
        super.onBackPressed();*/
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {
        String authCode = getCookie("https://app.boldirect.com", "BOL-auth");
        if(authCode!=null) {
            new BackgroundService().execute(authCode);
        }
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    class BackgroundService extends AsyncTask<String, Void, ApiResponse> {

        @Override
        protected ApiResponse doInBackground(String... params) {
            if (params[0] != null) {
                Response<ApiResponse> apiResponse = null;
                try {
                    apiResponse = restApi.getApiVersion("Basic " + params[0]).execute();
                    return apiResponse.body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ApiResponse apiResponse) {
            if (apiResponse != null) {
                SharedPreferences sharedPreferences = Utility.getSharedPreferences(getApplicationContext());
                float previousVersion = sharedPreferences.getFloat("apiVersion", -1);
                if (previousVersion > 0 && apiResponse.getApiVersion() > previousVersion) {
                    mWebView.clearCache(true);
                    mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                    getApplicationContext().deleteDatabase(mWebView.databaseDir);
                } else {
                    mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                }
                sharedPreferences.edit().putFloat("apiVersion", apiResponse.getApiVersion()).commit();
            }
            super.onPostExecute(apiResponse);
        }
    }

    public String getCookie(String siteName, String CookieName) {
        String CookieValue = null;

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if (cookies != null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(CookieName)) {
                    String[] temp1 = ar1.split("=");
                    CookieValue = temp1[1];
                    break;
                }
            }
            return CookieValue;
        }
        return null;
    }
}
/*
package com.huzefa.boldirect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private ProgressBar progress;
    //private ProgressDialog prgDialog;
    protected WebView mainWebView;
    private Context mContext;
    private WebView mWebviewPop;
    private FrameLayout mContainer;
    CookieManager cookieManager;
    private String url = "https://app.boldirect.com";
    private String target_url_prefix = "app.boldirect.com";
    TextView mDisplay;
    RelativeLayout noInternetMessage;
    //SwipeRefreshLayout swipeLayout;
    Button refreshButton;
    Utility utility;
    private Uri mCapturedImageURI = null;

    @Override
    public void onBackPressed() {
        if (mWebviewPop != null) {
            mWebviewPop.setVisibility(View.GONE);
            mContainer.removeView(mWebviewPop);
            mWebviewPop = null;
        } else if (mainWebView.canGoBack()) {
            mainWebView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
            finish();
        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        utility = new Utility();

        //  swipeLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
        noInternetMessage = (RelativeLayout) findViewById(R.id.no_internet_message);
        mContainer = (FrameLayout) findViewById(R.id.webview_frame);
        mainWebView = (WebView) findViewById(R.id.webview);
        refreshButton = (Button) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onRefresh();
            }
        });


        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(mainWebView, true);
        }
        mContext = this;
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setMax(100);
        WebSettings webSettings = mainWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowFileAccess(true);
        webSettings.setSupportZoom(true);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setDatabasePath("/data/data/" + mainWebView.getContext().getPackageName() + "/databases/");
        }
        webSettings.setSupportMultipleWindows(true);
        mainWebView.setWebViewClient(new MyCustomWebViewClient());
        mainWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mainWebView.getSettings().setGeolocationDatabasePath(mContext.getFilesDir().getPath());
        mainWebView.setWebChromeClient(new MyCustomChromeClient());

        if (utility.checkInternetConnectivity(mContext)) {
            mContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            mainWebView.loadUrl(url);
        } else {
            mContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }



		*/
/*swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				new Handler().postDelayed(new Runnable() {
					@Override public void run() {
						swipeLayout.setRefreshing(false);
					}
				}, 5000);
				if(utility.checkInternetConnectivity(mContext))
				{
					mContainer.setVisibility(View.VISIBLE);
					noInternetMessage.setVisibility(View.GONE);
					mainWebView.loadUrl(url);

				}
				else
				{

					mContainer.setVisibility(View.GONE);
					noInternetMessage.setVisibility(View.VISIBLE);
				}



			}
		});

		swipeLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);*//*


		*/
/*prgDialog= new ProgressDialog(MyActivity.this);
        prgDialog.setCanceledOnTouchOutside(false);
		prgDialog.setCancelable(false);
		prgDialog.setMessage("Loading..");*//*

        */
/*progress.setProgress(100);*//*

        //showDialog();
    }

    public void onRefresh() {
        // TODO Auto-generated method stub

		*/
/*new Handler().postDelayed(new Runnable() {
            @Override public void run() {
				swipeLayout.setRefreshing(false);
			}
		}, 5000);*//*

        if (utility.checkInternetConnectivity(mContext)) {
            mContainer.setVisibility(View.VISIBLE);
            noInternetMessage.setVisibility(View.GONE);
            mainWebView.loadUrl(url);

        } else {

            mContainer.setVisibility(View.GONE);
            noInternetMessage.setVisibility(View.VISIBLE);
        }
    }

    */
/**
 * @return Application's version code from the {@code PackageManager}.
 *//*

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private class MyCustomWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();


            if (host.equals(target_url_prefix)) {
                // This is my web site, so do not override; let my WebView load
                // the page
                if (mWebviewPop != null) {
                    mWebviewPop.setVisibility(View.GONE);
                    mContainer.removeView(mWebviewPop);
                    mWebviewPop = null;

                }

                return false;
            }

            if (host.equals("m.facebook.com") || host.equals("www.facebook.com")) {
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch
            // another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            Log.d("onReceivedSslError", "onReceivedSslError");
            //super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progress.setVisibility(View.GONE);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            progress.setVisibility(View.VISIBLE);
            //showDialog();
            super.onPageStarted(view, url, favicon);
        }

    }

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }


    private class MyCustomChromeClient extends WebChromeClient {

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

            // Update message
            mUploadMessage = uploadMsg;

            try {

                // Create AndroidExampleFolder at sdcard

                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");

                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }

                // Create camera captured image file path and name
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");

                mCapturedImageURI = Uri.fromFile(file);

                // Camera capture image intent
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image*/
/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");

                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Exception:" + e,
                        Toast.LENGTH_LONG).show();
            }

        }

        // openFileChooser for Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        //openFileChooser for other Android versions
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType,
                                    String capture) {

            openFileChooser(uploadMsg, acceptType);
        }



        // file upload callback (Android 5.0 (API level 21) -- current) (public method)
        @SuppressWarnings("all")
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (Build.VERSION.SDK_INT >= 21) {
                final boolean allowMultiple = fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;

                openFileInput(null, filePathCallback, allowMultiple);

                return true;
            }
            else {
                return false;
            }
        }

        // The webPage has 2 filechoosers and will send a
        // console message informing what action to perform,
        // taking a photo or updating the file

        public boolean onConsoleMessage(ConsoleMessage cm) {

            onConsoleMessage(cm.message(), cm.lineNumber(), cm.sourceId());
            return true;
        }

        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            //Log.d("androidruntime", "Show console messages, Used for debugging: " + message);

        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, false);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            MainActivity.this.setValue(newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {
            mWebviewPop = new WebView(mContext);
            mWebviewPop.setVerticalScrollBarEnabled(false);
            mWebviewPop.setHorizontalScrollBarEnabled(false);
            mWebviewPop.setWebViewClient(new MyCustomWebViewClient());
            mWebviewPop.getSettings().setJavaScriptEnabled(true);
            mWebviewPop.getSettings().setSavePassword(false);

            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            Log.d("onCloseWindow", "called");
        }



    }

    public void setValue(int progress) {
        this.progress.setProgress(progress);
    }

    */
/*@Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {

        if (requestCode == FILECHOOSER_RESULTCODE) {

            if (null == this.mUploadMessage) {
                return;

            }

            Uri result = null;

            try {
                if (resultCode != RESULT_OK) {

                    result = null;

                } else {

                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "activity :" + e,
                        Toast.LENGTH_LONG).show();
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;

        }
    }*//*


}*/
