package sk.pixel.telemetroid;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpResponseHandler;

import sk.pixel.telemetroid.forms.dialogs.ChangePasswordDialog;
import sk.pixel.telemetroid.forms.dialogs.LogoutConfirmationDialog;
import sk.pixel.telemetroid.forms.dialogs.RegisterDeviceDialog;
import sk.pixel.telemetroid.forms.fragments.DeviceLoginFragment;
import sk.pixel.telemetroid.forms.fragments.LoginFragment;
import sk.pixel.telemetroid.forms.fragments.RegisterUserFragment;
import sk.pixel.telemetroid.options_lists.LoginOptionsListFragment;
import sk.pixel.telemetroid.options_lists.MainOptionsListFragment;
import sk.pixel.telemetroid.utils.ServerCommunicator;

public class ItemListActivity extends FragmentActivity
        implements LoginFragment.LoginCallbacks, LoginOptionsListFragment.Callbacks, MainOptionsListFragment.Callbacks,
        ServerCommunicator.ServerResponseListener, LogoutConfirmationDialog.LogoutDialogListener,
        RegisterUserFragment.UserSignUpListener, DeviceLoginFragment.Callbacks, UserViewFragment.UserViewEventsListener {

    private final String TAG = "ItemListActivity";
    private boolean mTwoPane;
    private LoginFragment loginFragment;
    private MainScreenFragment mainScreenFragment;
    private LogoutConfirmationDialog logoutConfirmDialog;
    private RegisterUserFragment registerUserFragment;
    private LoginOptionsListFragment loginOptionsFragment;
    private RegisterDeviceDialog registerDeviceDialog;
    private Menu menu;
    private DeviceLoginFragment deviceLoginFragment;
    private UserViewFragment userViewFragment;
    private UserViewFragment userViewOptions;
    private String loggedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        loginOptionsFragment = new LoginOptionsListFragment(this);
        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.options_container, loginOptionsFragment)
                    .commit();
        }
        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    public void loginAsUserPressed(View view) {
        loginFragment.loginAsUserPressed(view);
    }

    public void loginAsDevicePressed(View view) {
        deviceLoginFragment.loginAsUserPressed(view);
    }

    public void signUpOnClick(View view) {
        registerUserFragment.signUpOnClick(view);
    }

    @Override
    public void loginSuccessful() {
        if (mTwoPane) {
            EditText username = (EditText) findViewById(R.id.username);
            loggedUser = username.getText().toString();
            Log.i(TAG, "logged as " + loggedUser);
            mainScreenFragment = new MainScreenFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, mainScreenFragment)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.options_container, new MainOptionsListFragment(this))
                    .commit();
        }
    }

    @Override
    public void loginAsUserOptionClicked() {
        if (mTwoPane) {
            loginFragment = new LoginFragment(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, loginFragment)
                    .commit();
        }
    }

    @Override
    public void loginAsDeviceOptionClicked() {
        if (mTwoPane) {
            deviceLoginFragment = new DeviceLoginFragment(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, deviceLoginFragment)
                    .commit();
        }
    }

    @Override
    public void registerNewUserClicked() {
        if (mTwoPane) {
            registerUserFragment = new RegisterUserFragment(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, registerUserFragment)
                    .commit();
        }
    }

    @Override
    public void onLogoutClicked() {
        FragmentManager fm = getSupportFragmentManager();
        logoutConfirmDialog = new LogoutConfirmationDialog(this);
        logoutConfirmDialog.show(fm, "logout_confirmation");
    }

    @Override
    public void onWhoClicked() {
        ServerCommunicator poster = new ServerCommunicator(this, this);
        poster.get(ServerCommunicator.WHO_URL, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String content) {
                Log.d(TAG, content);
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "/who failed");
            }
        });
    }

    @Override
    public void onDeviceManagementClicked() {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.device_management, menu);
    }

    @Override
    public void onUserProfileClicked() {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_profile_managment, menu);
        userViewFragment = new UserViewFragment(this);
        userViewOptions = new UserViewFragment(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, userViewFragment)
                .commit();
    }

    public void onChangePasswordClicked(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
        changePasswordDialog.show(fm, "change_password");

    }

    public void menuOnAddThisDeviceClick(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        registerDeviceDialog = new RegisterDeviceDialog();
        registerDeviceDialog.show(fm, "device_registration");
    }

    public void logout() {
        ServerCommunicator poster = new ServerCommunicator(this, this);
        poster.post(ServerCommunicator.LOGOUT_URL, null);
    }

    @Override
    public void onPostDataReceived(String data) {
        logoutConfirmDialog.dismiss();
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.item_detail_container));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.options_container, loginOptionsFragment)
                    .commit();
        }
    }

    @Override
    public void onConnectionError() {
        logoutConfirmDialog.dismiss();
        FragmentManager fm = getSupportFragmentManager();
        InfoDialog infoDialog = new InfoDialog("Can't connect to server", "Error occurred", InfoDialog.BUTTON_TYPE_DANGER);
        infoDialog.show(fm, "error_confirmation");
    }

    @Override
    public void signUpComplete() {
        FragmentManager fm = getSupportFragmentManager();
        InfoDialog infoDialog = new InfoDialog("Now you can log in", "Registration successful", InfoDialog.BUTTON_TYPE_SUCCESS);
        infoDialog.show(fm, "inf_confirmation");
        loginOptionsFragment.setActivatedPosition(LoginOptionsListFragment.USER_LOGIN_POSITION);
        loginAsUserOptionClicked();
    }

    @Override
    public void onDeviceLoginSuccessful() {
        if (mTwoPane) {
            Log.d(TAG, "logged as device");
        }
    }

    @Override
    public String getLoggedUser() {
        return loggedUser;
    }

    @Override
    public void reloadUserView() {
        onUserProfileClicked();
    }
}
