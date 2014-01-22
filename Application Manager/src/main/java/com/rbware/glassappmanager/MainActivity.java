package com.rbware.glassappmanager;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private ArrayList<ApplicationInfo> mPackageNames = new ArrayList();

    private int mSelectedListing;
    private UIListingCardScrollAdapter mAdapter;
    private CardScrollView mCardScrollView;
    private TextView mNoAppsFound;

    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackageManager = this.getPackageManager();
        View rootLayout = getLayoutInflater().inflate(R.layout.activity_main, null);
        mNoAppsFound = (TextView)rootLayout.findViewById(R.id.noAppsFound);
        setupCardList(rootLayout);
        setContentView(rootLayout);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_uninstall) {
            String packageName = mPackageNames.get(mSelectedListing).packageName;
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 0);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0){
            mPackageNames.clear();
            setupCardList(null);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupCardList(View rootLayout){

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> list = mPackageManager.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
        for (ResolveInfo rInfo : list) {
            if (!rInfo.activityInfo.applicationInfo.loadLabel(mPackageManager).toString().equals("Glass Home") &&
                    !rInfo.activityInfo.applicationInfo.loadLabel(mPackageManager).toString().equals("Glass App Manager"))
                mPackageNames.add(rInfo.activityInfo.applicationInfo);
        }

        if(!mPackageNames.isEmpty()){
            mAdapter = new UIListingCardScrollAdapter();
            if (mCardScrollView == null){
                mCardScrollView = (CardScrollView)rootLayout.findViewById(R.id.card_scroll_view);
                mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedListing = position;
                        MainActivity.super.openOptionsMenu();
                    }
                });
            }
            mCardScrollView.setVisibility(View.VISIBLE);
            mCardScrollView.setAdapter(mAdapter);
            mCardScrollView.activate();
        } else {


            mNoAppsFound.setVisibility(View.VISIBLE);

            if(mCardScrollView != null)
                mCardScrollView.setVisibility(View.INVISIBLE);

        }
    }

    private class UIListingCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int findIdPosition(Object id) {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) {
            return mPackageNames.indexOf(item);
        }

        @Override
        public int getCount() {
            Log.d("Tag", "getCount() = " + mPackageNames.size());
            return mPackageNames.size();
        }

        @Override
        public Object getItem(int position) {
            Log.d("Tag", "getItem(" + position + ") = " + mPackageNames.get(position));
            return mPackageNames.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater layoutInflater;

            if (v == null) {
                layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.application_detail, null);
            }

            TextView appName = (TextView)v.findViewById(R.id.applicationName);
            TextView appVersion = (TextView)v.findViewById(R.id.applicationVersion);
            ImageView appIcon = (ImageView)v.findViewById(R.id.applicationIcon);

            if (!mPackageNames.isEmpty()){

                ApplicationInfo info = mPackageNames.get(position);

                appName.setText(info.loadLabel(mPackageManager).toString());
                appIcon.setImageDrawable(info.loadIcon(mPackageManager));
                try{
                    appVersion.setText(getPackageManager().getPackageInfo(info.loadLabel(mPackageManager).toString(), 0).versionCode);
                } catch (PackageManager.NameNotFoundException e){

                }
            }
            return v;
        }

    }
}
