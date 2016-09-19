package com.zly.loadmorerecyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        initView();
    }

    private void initView() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_fragment, SampleFragment.newInstance(SampleFragment.MODE_LIST))
                .commitAllowingStateLoss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_fragment, SampleFragment.newInstance(SampleFragment.MODE_LIST))
                        .commitAllowingStateLoss();
                break;
            case R.id.action_grid:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_fragment, SampleFragment.newInstance(SampleFragment.MODE_GRID))
                        .commitAllowingStateLoss();
                break;
            case R.id.action_staggered_grid:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_fragment, SampleFragment.newInstance(SampleFragment.MODE_STAGGERED_GRID))
                        .commitAllowingStateLoss();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
