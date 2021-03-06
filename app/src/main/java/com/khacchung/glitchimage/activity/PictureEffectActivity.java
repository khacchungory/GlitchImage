package com.khacchung.glitchimage.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.khacchung.glitchimage.R;
import com.khacchung.glitchimage.adapter.EffectAdapter;
import com.khacchung.glitchimage.application.MyApplication;
import com.khacchung.glitchimage.base.BaseActivity;
import com.khacchung.glitchimage.customs.CallBackPermission;
import com.khacchung.glitchimage.util.AdsUtil;
import com.khacchung.glitchimage.util.AudienceNetworkInitializeHelper;
import com.khacchung.glitchimage.util.GalleryEffect;
import com.khacchung.glitchimage.util.PathManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import cn.ezandroid.ezfilter.EZFilter;
import cn.ezandroid.ezfilter.core.FilterRender;
import cn.ezandroid.ezfilter.core.RenderPipeline;
import cn.ezandroid.ezfilter.core.environment.SurfaceFitView;

public class PictureEffectActivity extends BaseActivity implements View.OnTouchListener {
    private static final String TAG = PictureEffectActivity.class.getSimpleName();
    private static RenderPipeline renderPipeline = new RenderPipeline();

    private LinearLayout lnHand;
    private RelativeLayout rnHand;

    private Animation animation1;
    private Animation animation2;

    private int screenWidth;
    private int screenHeight;

    private Bitmap effectBmp;
    private int pos = 0;
    private List<String> effectName;
    private boolean imageChange = false;
    private int[] icon = {
            R.drawable.ic_0,
            R.drawable.ic_6,
            R.drawable.ic_7,
            R.drawable.ic_14,
            R.drawable.ic_15,
            R.drawable.ic_16,
            R.drawable.ic_18,
            R.drawable.ic_20,
            R.drawable.ic_26,
            R.drawable.ic_12,
            R.drawable.ic_30,
            R.drawable.ic_31,
            R.drawable.ic_46,
            R.drawable.ic_31,
            R.drawable.ic_32,
            R.drawable.ic_33,
            R.drawable.ic_34,
            R.drawable.ic_35,
            R.drawable.ic_36,
            R.drawable.ic_1,
            R.drawable.ic_2,
            R.drawable.ic_3,
            R.drawable.ic_4,
            R.drawable.ic_5,
            R.drawable.ic_8,
            R.drawable.ic_9,
            R.drawable.ic_10,
            R.drawable.ic_11,
            R.drawable.ic_17,
            R.drawable.ic_19,
            R.drawable.ic_21,
            R.drawable.ic_22,
            R.drawable.ic_23,
            R.drawable.ic_24,
            R.drawable.ic_25,
            R.drawable.ic_27,
            R.drawable.ic_28,
            R.drawable.ic_29,
            R.drawable.ic_37,
            R.drawable.ic_38,
            R.drawable.ic_40,
            R.drawable.ic_41,
            R.drawable.ic_43,
            R.drawable.ic_42,
            R.drawable.ic_13,
            R.drawable.ic_45
    };
    private EffectAdapter effectAdapter;

    private RelativeLayout relativeEffect;
    private RecyclerView recyclerView;
    private SurfaceFitView surfaceFitView;
    private AdView adView;
    private com.google.android.gms.ads.AdView mAdView;
    private AdRequest adRequest;

    public static void startIntent(BaseActivity activity) {
        activity.startActivity(
                new Intent(activity, PictureEffectActivity.class)
        );
        MyApplication.addCountAction();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        hidenStatusBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_effect);
        enableBackButton();
        setTitleToolbar(getResources().getString(R.string.effect_image));
        //ads
        AudienceNetworkInitializeHelper.initialize(this);

        effectBmp = myApplication.getImgBMP();
        MyApplication.imgWidth = effectBmp.getWidth();
        MyApplication.imgHeight = effectBmp.getHeight();
        getImageFromUri();
        initView();

        effectName = GalleryEffect.getName();
        effectAdapter = new EffectAdapter(this, effectName, icon);
        recyclerView.setAdapter(effectAdapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        );

        renderPipeline = EZFilter.input(effectBmp)
                .addFilter(new FilterRender())
                .into(surfaceFitView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancleLoading();
        checkPermission(new String[]{
                        BaseActivity.PER_READ,
                        BaseActivity.PER_WRITE
                },
                new CallBackPermission() {
                    @Override
                    public void grantedFullPermission() {

                    }

                    @Override
                    public void notFullPermission() {
                        Toast.makeText(PictureEffectActivity.this,
                                getResources().getString(R.string.error_permission),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initView() {
        mAdView = findViewById(R.id.adView);
        recyclerView = findViewById(R.id.rcv_filters);
        relativeEffect = findViewById(R.id.relative_effects);
        surfaceFitView = findViewById(R.id.render_view);
        lnHand = findViewById(R.id.ln_hand);
        rnHand = findViewById(R.id.rl_move_hand);
        animation1 = AnimationUtils.loadAnimation(this, R.anim.anim_hand_1);
        animation2 = AnimationUtils.loadAnimation(this, R.anim.anim_hand_2);
        lnHand.setAnimation(animation1);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lnHand.setAnimation(animation2);
                animation2.start();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                lnHand.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rnHand.setVisibility(View.GONE);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) relativeEffect.getLayoutParams();
        params.width = (screenWidth);
        params.height = (screenHeight * 229) / 1920;
        params.addRule(13);
        relativeEffect.setLayoutParams(params);
        surfaceFitView.setRenderMode(1);
        surfaceFitView.setOnTouchListener(this);

        //ads
        adView = new AdView(this, AdsUtil.BANNER_ID, AdSize.BANNER_HEIGHT_50);

        // Find the Ad Container
        LinearLayout adContainer = findViewById(R.id.banner_container);
        // Add the ad view to your activity layout
        adContainer.addView(adView);
        // Request an ad
        adView.loadAd();
        adView.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                super.onError(ad, error);
                Log.e(TAG, "loadAdsFacebook onError()");
                adRequest = new AdRequest.Builder()
                        .addTestDevice(AdsUtil.HASHED_ID)
                        .build();
                mAdView.loadAd(adRequest);
            }
        });
    }

    private void getImageFromUri() {
        if (myApplication != null) {
            String path = myApplication.getPathImage();
            File f = new File(path);
            if (!f.exists()) {
                finish();
            }
            try {
                myApplication.setImgBMP(effectBmp);
                renderPipeline = EZFilter.input(effectBmp).into(surfaceFitView);
                cancleLoading();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPositionToStartAnimation(int pos) {
        boolean check = false;
        for (int item : GalleryEffect.listPositionNone) {
            if (item == pos) {
                check = true;
            }
        }
        if (!check) {
            rnHand.setVisibility(View.VISIBLE);
            animation1.reset();
            animation2.reset();
            lnHand.setVisibility(View.VISIBLE);
            lnHand.setAnimation(animation1);
            animation1.start();
        } else {
            rnHand.setVisibility(View.GONE);
        }
    }

    @Override
    public void setEffects(int i) {
        checkPositionToStartAnimation(i);
        imageChange = true;
        renderPipeline.clearEndPointRenders();
        try {
            pos = i;
            if (i == 0) {
                imageChange = false;
                renderPipeline = EZFilter.input(effectBmp)
                        .addFilter(new FilterRender())
                        .into(surfaceFitView);
                return;
            }
            imageChange = true;
            renderPipeline = EZFilter.input(effectBmp)
                    .addFilter(GalleryEffect.getEffect(this, i))
                    .into(surfaceFitView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_save:
                saveImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveImage() {
        PathManager pathManager = new PathManager(this);
        if (pathManager.checkFolderExists(PathManager.FOLDER_IMAGE)) {
            pathManager.createFolderImage();
        }
        StringBuilder pathFile = new StringBuilder();
        pathFile.append(PathManager.getPathFolder(this));
        pathFile.append(PathManager.FOLDER_IMAGE);
        pathFile.append("/img");
        pathFile.append(System.currentTimeMillis());
        pathFile.append(".jpg");

        renderPipeline.output(bitmap ->
                runOnUiThread(() ->
                        saveBitmap(bitmap, pathFile.toString()))
        );
    }

    public void saveBitmap(Bitmap bitmap, String str) {
        File file = new File(str);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(PictureEffectActivity.this,
                    getResources().getString(R.string.save) + str,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE"
                , Uri.fromFile(new File(str))));

        showLoading();
        interstitialAd.loadAd();
        interstitialAd.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                super.onError(ad, error);
                Log.e(TAG, "load Ads Facebook onError()");
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        showLoading();
                        ListFileActivity.startIntent(PictureEffectActivity.this, str, ListFileActivity.TYPE_IMG);
                        finish();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        super.onAdFailedToLoad(i);
                        Log.e(TAG, "load Admob onError()");
                        ListFileActivity.startIntent(PictureEffectActivity.this, str, ListFileActivity.TYPE_IMG);
                        finish();
                    }
                });
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    ListFileActivity.startIntent(PictureEffectActivity.this, str, ListFileActivity.TYPE_IMG);
                    finish();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                super.onAdLoaded(ad);
                interstitialAd.show();
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
                showLoading();
                ListFileActivity.startIntent(PictureEffectActivity.this, str, ListFileActivity.TYPE_IMG);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancleLoading();
    }

    @Override
    public void onBackPressed() {
        if (imageChange) {
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_alert_save);

            Button btnSave = dialog.findViewById(R.id.btn_save);
            Button btnCancel = dialog.findViewById(R.id.btn_cancel);
            btnSave.setOnClickListener(v -> saveImage());
            btnCancel.setOnClickListener(v -> {
                if (dialog.isShowing()) {
                    dialog.cancel();
                    dialog.dismiss();
                    super.onBackPressed();
                }
            });
            Window window = dialog.getWindow();
            window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            dialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                GalleryEffect.setTouch(pos, x, y);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}
