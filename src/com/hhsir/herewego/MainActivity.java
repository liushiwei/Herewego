
package com.hhsir.herewego;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hhsir.herewego.board.LoginFragment;
import com.hhsir.herewego.board.FindPeopleFragment;
import com.hhsir.herewego.board.HomeFragment;
import com.hhsir.herewego.board.MenuFragment;
import com.hhsir.herewego.board.MenuFragment.SLMenuListOnItemClickListener;
import com.hhsir.herewego.board.PagesFragment;
import com.hhsir.herewego.board.PhotosFragment;
import com.hhsir.herewego.board.WhatsHotFragment;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

public class MainActivity extends SlidingActivity implements SLMenuListOnItemClickListener {
    // public class MainActivity extends SlidingFragmentActivity {

    private SlidingMenu mSlidingMenu;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Home");
        // setTitle(R.string.sliding_title);
        setContentView(R.layout.frame_content);

        // 1、 set the Behind View
        setBehindContentView(R.layout.frame_menu);

     // customize the SlidingMenu
        mSlidingMenu = getSlidingMenu();
        // mSlidingMenu.setMenu(R.layout.frame_menu); //设置左侧菜单的布局文件
        // mSlidingMenu.setSecondaryMenu(R.layout.frame_menu); 设置右侧菜单的布局文件

        // mSlidingMenu.setShadowWidth(5);
        // mSlidingMenu.setBehindOffset(100);
        mSlidingMenu.setShadowDrawable(R.drawable.drawer_shadow);// 设置阴影图片
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width); // 设置阴影图片的宽度
        mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset); // SlidingMenu划出时主页面显示的剩余宽度
        mSlidingMenu.setFadeDegree(0.35f);
        // 设置SlidingMenu 的手势模式
        // TOUCHMODE_FULLSCREEN 全屏模式，在整个content页面中，滑动，可以打开SlidingMenu
        // TOUCHMODE_MARGIN
        // 边缘模式，在content页面中，如果想打开SlidingMenu,你需要在屏幕边缘滑动才可以打开SlidingMenu
        // TOUCHMODE_NONE 不能通过手势打开SlidingMenu
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        // 设置 SlidingMenu 内容
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        MenuFragment menuFragment = new MenuFragment();
        fragmentTransaction.replace(R.id.menu, menuFragment);
        fragmentTransaction.replace(R.id.content, new HomeFragment());
        fragmentTransaction.commit();

        // 使用左上方icon可点，这样在onOptionsItemSelected里面才可以监听到R.id.home
        int mCurrentOrientation = getResources().getConfiguration().orientation;

        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            getActionBar().show();
            getActionBar().setDisplayHomeAsUpEnabled(true);
            findViewById(R.id.slidingmenu).setVisibility(View.GONE);

        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActionBar().hide();
            ImageButton btn = (ImageButton) findViewById(R.id.slidingmenu);
            btn.setOnClickListener(new OnClickListener() {
                
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    mSlidingMenu.toggle();
                }
            });
            btn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                toggle(); // 动态判断自动关闭或开启SlidingMenu
                // getSlidingMenu().showMenu();//显示SlidingMenu
                // getSlidingMenu().showContent();//显示内容
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    @Override
    public void selectItem(int position, String title) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new FindPeopleFragment();
                break;
            case 2:
                fragment = new PhotosFragment();
                break;
            case 3:
                fragment = new LoginFragment();
                break;
            case 4:
                fragment = new PagesFragment();
                break;
            case 5:
                fragment = new WhatsHotFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content, fragment).commit();
            // update selected item and title, then close the drawer
            setTitle(title);
            mSlidingMenu.showContent();
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // newConfig.orientation获得当前屏幕状态是横向或者竖向
        // Configuration.ORIENTATION_PORTRAIT 表示竖向
        // Configuration.ORIENTATION_LANDSCAPE 表示横屏
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(MainActivity.this, "现在是竖屏", Toast.LENGTH_SHORT).show();
            getActionBar().show();
            findViewById(R.id.slidingmenu).setVisibility(View.GONE);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(MainActivity.this, "现在是横屏", Toast.LENGTH_SHORT).show();
            getActionBar().hide();
            ImageButton btn = (ImageButton) findViewById(R.id.slidingmenu);
            btn.setOnClickListener(new OnClickListener() {
                
                public void onClick(View v) {
                    mSlidingMenu.toggle();
                }
            });
            btn.setVisibility(View.VISIBLE);
        }
    }

}
