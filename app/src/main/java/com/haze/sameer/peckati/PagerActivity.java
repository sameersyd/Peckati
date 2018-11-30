package com.haze.sameer.peckati;

import android.animation.ArgbEvaluator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class PagerActivity extends AppCompatActivity {

    SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    ImageView zero, one, two, three;
    ImageView[] indicators;
    FirebaseAuth auth;
    int lastLeftValue = 0;

    Button begin;
    CoordinatorLayout mCoordinator;
    LinearLayout indicatorsLin;

    static final String TAG = "PagerActivity";

    int page = 0;   //  to track page position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(PagerActivity.this, MainActivity.class));
            finish();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        setContentView(R.layout.activity_pager);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
//            mNextBtn.setImageDrawable(
//                    Utils.tintMyDrawable(ContextCompat.getDrawable(this, R.drawable.white_arrow), Color.WHITE)
//            );

        begin = (Button)findViewById(R.id.beginBtn);
        indicatorsLin = (LinearLayout)findViewById(R.id.introIndicators);

        zero = (ImageView) findViewById(R.id.intro_indicator_0);
        one = (ImageView) findViewById(R.id.intro_indicator_1);
        two = (ImageView) findViewById(R.id.intro_indicator_2);
        three = (ImageView) findViewById(R.id.intro_indicator_3);

        mCoordinator = (CoordinatorLayout) findViewById(R.id.main_content);

        indicators = new ImageView[]{zero, one, two, three};

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                page = position;
                updateIndicators(page);

                begin.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
                indicatorsLin.setVisibility(position == 3 ? View.GONE : View.VISIBLE);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PagerActivity.this,LoginActivity.class));
                finish();
            }
        });

    }

    void updateIndicators(int position) {

        switch (position){
            case 0:
                zero.setBackgroundResource(R.drawable.indicator_selected);
                one.setBackgroundResource(R.drawable.indicator_unselected);
                two.setBackgroundResource(R.drawable.indicator_unselected);
                three.setBackgroundResource(R.drawable.indicator_unselected);
                break;
            case 1:
                zero.setBackgroundResource(R.drawable.indicator_selected);
                one.setBackgroundResource(R.drawable.indicator_selected);
                two.setBackgroundResource(R.drawable.indicator_unselected);
                three.setBackgroundResource(R.drawable.indicator_unselected);
                break;
            case 2:
                zero.setBackgroundResource(R.drawable.indicator_selected);
                one.setBackgroundResource(R.drawable.indicator_selected);
                two.setBackgroundResource(R.drawable.indicator_selected);
                three.setBackgroundResource(R.drawable.indicator_unselected);
                break;
            case 3:
                zero.setBackgroundResource(R.drawable.indicator_selected);
                one.setBackgroundResource(R.drawable.indicator_selected);
                two.setBackgroundResource(R.drawable.indicator_selected);
                three.setBackgroundResource(R.drawable.indicator_selected);
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pager, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                        startActivity(intent);
                        finish();
                        System.exit(0);
                    }
                }).setNegativeButton("No", null).show();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        ImageView img;

        int[] bgs = new int[]{R.drawable.pager_one, R.drawable.pager_two, R.drawable.pager_three, R.drawable.pager_four};
        String[] text_design = new String[]
                {   "Wondering what can you do with Peckati? Let’s give you a small tour of the features we have under cover",
                    "With these food suggestion, you may choose to display the recipe or order online",
                    "As soon as you open the app, you’ll be asked to show and capture your face to help Peckati understand your emotions",
                    "Share posts or view stories from your friends" };

        String[] text_head = new String[]{"Welcome Aboard","Innovative Food Suggestions","Food suggestions by your mood","Sharing Experiences with Food"};

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(text_design[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
            TextView headView = (TextView) rootView.findViewById(R.id.section_head);
            headView.setText(text_head[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            img = (ImageView) rootView.findViewById(R.id.section_img);
            img.setBackgroundResource(bgs[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            return rootView;
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
                case 3:
                    return "SECTION 4";
            }
            return null;
        }
    }

}