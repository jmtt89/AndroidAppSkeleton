package com.ogangi.dummyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.ogangi.messangi.android.sdk.Messangi;


/**
 * An activity representing a list of Notifications. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NotificationDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NotificationListFragment} and the item details
 * (if present) is a {@link NotificationDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link NotificationListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class NotificationListActivity extends AppCompatActivity
        implements NotificationListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ProgressBar loading;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification_app_bar);
        setupMessangi();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loading = (ProgressBar) findViewById(R.id.loadingProgressBar);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //TODO: Add a call to refresh a fragment adapter
                swipeContainer.setRefreshing(false);
            }
        });

        if (findViewById(R.id.notification_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NotificationListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.notification_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    protected void onResume() {
        Messangi.getInstance().bindService();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Messangi.getInstance().unBindService();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            Messangi.getInstance().onRequestPermissionsResult(requestCode,permissions,grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMessangi() {


         // Messangi Credentials
         Messangi.getInstance().setAppName("");
         Messangi.getInstance().setApiClientPrivateKey("");
         Messangi.getInstance().setClientId("");

        // GCM Credentials
        Messangi.getInstance().setGcmApiKey(getString(R.string.gcm_api_key));
        Messangi.getInstance().setGcmProjectId(getString(R.string.gcm_defaultSenderId));
        ;

        Messangi.getInstance().requestLocationPermissions(this);
        Messangi.getInstance().requestReadSMSPermission(this);
        Messangi.getInstance().init(this);
        Messangi.getInstance().addMessangiListener(Listener.getIntance());
        Messangi.getInstance().registerDialog(this, this);


    }

    /**
     * Callback method from {@link NotificationListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(NotificationDetailFragment.ARG_ITEM_ID, id);
            NotificationDetailFragment fragment = new NotificationDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.notification_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, NotificationDetailActivity.class);
            detailIntent.putExtra(NotificationDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    protected void hideLoading(){
        loading.setVisibility(View.GONE);
    }
}
