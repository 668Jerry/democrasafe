package org.g0v.democrasafe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.facebook.model.GraphUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.content.pm.Signature;
import android.widget.Toast;
//import java.security.Signature;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends FragmentActivity {

//public class LoginActivity extends ActionBarActivity {

    private static final int SPLASH = 0;
    private static final int SELECTION = 1;
    private static final int FRAGMENT_COUNT = SELECTION +1;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    private boolean isResumed = false;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback =
            new Session.StatusCallback() {
                @Override
                public void call(Session session,
                                 SessionState state, Exception exception) {
                    onSessionStateChange(session, state, exception);
                }
            };

    private String sUserName;
    private String sUserLanguage;
    private String sUserID;

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        FragmentManager fm = getSupportFragmentManager();
        fragments[SPLASH] = fm.findFragmentById(R.id.splashFragment);
        fragments[SELECTION] = fm.findFragmentById(R.id.selectionFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    private String buildUserInfoDisplay(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        sUserName = String.format("Name: %s\n\n", user.getName());
        userInfo.append(sUserName);
        Log.d("facebook", sUserName);

        sUserID = String.format("Id: %s\n\n", user.getId());
        userInfo.append(sUserID);
        Log.d("facebook", sUserID);

        sUserLanguage = String.format("Locale: %s\n\n", user.getProperty("locale"));
        userInfo.append(sUserLanguage);
        Log.d("facebook", sUserLanguage);

        return userInfo.toString();
    }

    private void myAlertDialog () {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle("Alert Dialog");   // Setting Dialog Title
        alertDialog.setMessage(sUserName);      // Setting Dialog Message
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        // Only make changes if the activity is visible
//        myAlertDialog();
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            // Get the number of entries in the back stack
            int backStackSize = manager.getBackStackEntryCount();
            // Clear the back stack
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                // If the session state is open:
                // Show the authenticated fragment
                Request.executeMeRequestAsync(session,
                        new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(GraphUser user, Response response) {
                                if (user != null) {
                                    // Display the parsed user info
                                    Log.d("facebook", buildUserInfoDisplay(user));
                                    showFragment(SELECTION, false);
                                    ((SelectionFragment)fragments[SELECTION]).setUserInformation(sUserID,sUserName,sUserLanguage);
                                }
                            }
                        }
                );
            } else if (state.isClosed()) {
                // If the session state is closed:
                // Show the login fragment
                showFragment(SPLASH, false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    public String getUserId() {
        return sUserID;
    }

    public void simpleTest() {
        Log.d("test", "test");
        Log.d("test", sUserID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open,
            // try to show the selection fragment
            showFragment(SELECTION, false);
        } else {
            // otherwise present the splash screen
            // and ask the person to login.
            showFragment(SPLASH, false);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_login, container, false);
            return rootView;
        }
    }

    /**
     * A SplashFragment containing a simple view.
     */
    public static class SplashFragment extends Fragment {

        public SplashFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.splash, container, false);
//            View view = inflater.inflate(R.layout.fragment_login, container, false);
            return view;
        }
    }

    private static final String TAG = "SelectionFragment";

    /**
     * A SelectionFragment containing a simple view.
     */
    public static class SelectionFragment extends Fragment {

        public SelectionFragment() {
        }

        private TextView userInfoTextView;

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
//            super.onCreateView(inflater, container, savedInstanceState);
//            View view = inflater.inflate(R.layout.selection, container, false);
//            userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);
//            return view;


//            LinearLayout llFull = new LinearLayout(getActivity());
//            LoginButton authButton = new LoginButton(getActivity());
//            authButton.setFragment(this);
//            authButton.setReadPermissions(Arrays.asList("user_likes", "user_status"));
//
//            TextView tvHello = new TextView(getActivity());
//            tvHello.setText("Welcome, you are now logged in.");
//            llFull.addView(authButton);
//            llFull.addView(tvHello);
//            return llFull;

            View view = inflater.inflate(R.layout.fetch, container, false);

            LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
            authButton.setFragment(this);
            authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes"));

            userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);
            return view;

//            LinearLayout llFull = new LinearLayout(getActivity());
//
//            LoginButton authButton = new LoginButton(getActivity());
//            authButton.setFragment(this);
//            authButton.setReadPermissions(Arrays.asList("user_location", "user_birthday", "user_likes"));
//            llFull.addView(authButton);
//
//            TextView tvId = new TextView(getActivity());
//            tvId.setText(((LoginActivity)getActivity()).getUserId());
//            llFull.addView(tvId);
//
//            return llFull;

        }

        public void setUserInformation (String sUserId, String sUserName, String sUserLanguage) {
            userInfoTextView.setText(sUserId);
        }
    }

}
