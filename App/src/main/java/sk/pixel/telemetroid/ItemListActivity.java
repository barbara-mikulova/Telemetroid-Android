package sk.pixel.telemetroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public class ItemListActivity extends FragmentActivity
        implements ItemListFragment.Callbacks, LoginFragment.LoginCallbacks{

    private boolean mTwoPane;
    private LoginFragment loginFragment;
    private MainScreenFragment mainScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if (findViewById(R.id.item_detail_container) != null) {
            mTwoPane = true;
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }
        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            if (id == ItemListFragment.USER_LOGIN_POSITION) {
                loginFragment = new LoginFragment(this);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, loginFragment)
                        .commit();
            }

        } else {
            if (id == ItemListFragment.USER_LOGIN_POSITION) {
                Intent detailIntent = new Intent(this, ItemDetailActivity.class);
                startActivity(detailIntent);
            }
        }
    }

    public void loginAsUserPressed(View view) {
        loginFragment.loginAsUserPressed(view);
    }

    public void loginAsDevicePressed(View view) {
        loginFragment.loginAsDevicePressed(view);
    }

    @Override
    public void loginSucessfull() {
        if (mTwoPane) {
            mainScreenFragment = new MainScreenFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.item_detail_container, mainScreenFragment)
                    .commit();
            ((ItemListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setOptions(ItemListFragment.OPTIONS_MAIN);
        } else {
            Intent detailIntent = new Intent(this, ItemDetailActivity.class);
            startActivity(detailIntent);
        }
        Log.d("TAG", "logged");
    }
}
