package mobi.thinkchange.android.fingerscannercn.fragment;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mobi.thinkchange.android.fingerscannercn.R;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer. See the <a href=
 * "https://developer.android.com/design/patterns/navigation-drawer.html#Interaction" > design guidelines</a>
 * for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment
        extends Fragment {

    // Drawer Action Item的数据源
    static final int[] DRAWER_ACTION_ITEM_IDS = {R.id.drawer_action_feedback, R.id.drawer_action_share,
            R.id.drawer_action_about_us, R.id.drawer_action_divider, R.id.drawer_action_our_app};
    static final int[] DRAWER_ACTION_ITEM_TITLES = {R.string.drawer_action_feedback, R.string.drawer_action_share,
            R.string.drawer_action_about_us, 0, R.string.drawer_action_our_app};
    static final int[] DRAWER_ACTION_ITEM_IMAGES = {R.drawable.drawer_feedback, R.drawable.drawer_share, R.drawable.drawer_about, 0, R.drawable.drawer_own};

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of
        // actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        ViewGroup drawerView = (ViewGroup) view.findViewById(R.id.drawer_container);
        initDrawerActionItems(inflater, drawerView);
//        TextView textView = (TextView) view.findViewById(R.id.logo_text);
//        // 获取软件版本号
//        getActivity().runOnUiThread(Utils.getVersionCode(getActivity(), textView));

        return view;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // 注意：使用v7的ActionBarDrawerToggle才会NavigationDrawer toggle的动画效果
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.navigation_drawer_open, /*
                                          * "open drawer" description for accessibility
                                          */
                R.string.navigation_drawer_close /*
                                          * "close drawer" description for accessibility
                                          */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls
                // onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to
                    // prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls
                // onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce
        // them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initDrawerActionItems(LayoutInflater inflater, ViewGroup drawerView) {
        // Drawer Action Item Click监听器
        OnClickListener drawerActionOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                selectItem(v.getId());
            }
        };

        for (int i = 0; i < DRAWER_ACTION_ITEM_IDS.length; i++) {
            int currItemId = DRAWER_ACTION_ITEM_IDS[i];

            if (currItemId == R.id.drawer_action_divider) { // 分隔符
                View divider = inflater.inflate(R.layout.horizontal_divider, drawerView, false);
                drawerView.addView(divider);
            } else {
                // inflate的使用参考<a
                // href="http://blog.csdn.net/michael__li/article/details/21739921">Android
                // Layout的layout_height等属性为什么会不起作用？</a>
                RelativeLayout actionItem =
                        (RelativeLayout) inflater.inflate(R.layout.fragment_navigation_drawer_action_item,
                                drawerView, false);
                actionItem.setId(currItemId);
                actionItem.setOnClickListener(drawerActionOnClickListener);

                // 动态化文字
                TextView tv = (TextView) actionItem.findViewById(R.id.drawer_action_item_title);
                tv.setText(DRAWER_ACTION_ITEM_TITLES[i]);
//                if (currItemId == R.id.drawer_action_our_app) {            // "我们的APP"文字更改颜色
//                    tv.setTextColor(getResources().getColor(
//                            R.color.material_blue_500));
//                }

                // 动态化Drawer Action Item 图标
                ImageView imageView = (ImageView) actionItem.findViewById(R.id.drawer_action_item_icon);
                imageView.setImageResource(DRAWER_ACTION_ITEM_IMAGES[i]);

                drawerView.addView(actionItem);
            }
        }
    }

    private void selectItem(int viewId) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(viewId);
        }
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar.
        // See also
        // showGlobalContextActionBar, which controls the top-left area of the
        // action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app 'context',
     * rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int viewId);
    }
}
