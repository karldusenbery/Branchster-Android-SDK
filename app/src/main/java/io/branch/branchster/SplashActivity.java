package io.branch.branchster;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.branchster.util.MonsterPreferences;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

import static io.branch.referral.Defines.Jsonkey.SDK;

public class SplashActivity extends Activity {

    TextView txtLoading;
    int messageIndex;
    private static final String TAG = "SplashActivity";
    ImageView imgSplash1, imgSplash2;
    Context mContext;
    final int ANIM_DURATION = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;

        // Get loading messages from XML definitions.
        final String[] loadingMessages = getResources().getStringArray(R.array.loading_messages);
        txtLoading = (TextView) findViewById(R.id.txtLoading);
        imgSplash1 = (ImageView) findViewById(R.id.imgSplashFactory1);
        imgSplash2 = (ImageView) findViewById(R.id.imgSplashFactory2);
        imgSplash2.setVisibility(View.INVISIBLE);
        imgSplash1.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Initialize Branch session.
        //If a monster was linked to, open the viewer Activity to that Monster.
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).withData(getIntent() != null ? getIntent().getData() : null).init();
        proceedToAppTransparent();

        // listener (within Main Activity's onStart)
        Branch.sessionBuilder(this).withCallback(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.i("BRANCH SDK", referringParams.toString());
                } else {
                    Log.i("BRANCH SDK", error.getMessage());
                }
            }
        }).withData(this.getIntent().getData()).init();

        // latest
        JSONObject sessionParams = Branch.getInstance().getLatestReferringParams();

        // first
        JSONObject installParams = Branch.getInstance().getFirstReferringParams();
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        // if activity is in foreground (or in backstack but partially visible) launching the same
        // activity will skip onStart, handle this case with reInitSession
        Branch.sessionBuilder(this).withCallback(branchReferralInitListener).reInit();
    }

    private Branch.BranchReferralInitListener branchReferralInitListener = new Branch.BranchReferralInitListener() {
        @Override
        public void onInitFinished(JSONObject linkProperties, BranchError error) {
            // do stuff with deep link data (nav to page, display content, etc)
        }
    };

    /**
     * Opens the appropriate next Activity, based on whether a Monster has been saved in {@link MonsterPreferences}.
     */
    private void proceedToApp() {
        MonsterPreferences prefs = MonsterPreferences.getInstance(getApplicationContext());
        Intent intent;

        if (prefs.getMonsterName() == null || prefs.getMonsterName().length() == 0) {
            prefs.setMonsterName("");
            intent = new Intent(SplashActivity.this, MonsterCreatorActivity.class);
        } else {
            // Create a default monster
            intent = new Intent(SplashActivity.this, MonsterViewerActivity.class);
            intent.putExtra(MonsterViewerActivity.MY_MONSTER_OBJ_KEY, prefs.getLatestMonsterObj());
        }

        startActivity(intent);
        finish();
    }

    /**
     * Displays an animation to start the app. Once the animation has finished, will call {@link #proceedToApp()}.
     */
    private void proceedToAppTransparent() {
        Animation animSlideIn = AnimationUtils.loadAnimation(mContext, R.anim.push_down_in);
        animSlideIn.setDuration(ANIM_DURATION);
        animSlideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                proceedToApp();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imgSplash1.setVisibility(View.VISIBLE);
        imgSplash2.setVisibility(View.VISIBLE);
        imgSplash2.startAnimation(animSlideIn);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
