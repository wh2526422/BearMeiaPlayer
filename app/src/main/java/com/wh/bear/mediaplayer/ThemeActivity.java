package com.wh.bear.mediaplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.wh.bear.mediaplayer.utils.MediaKeeper;


public class ThemeActivity extends Activity {
    private ImageView expandView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_layout);
        expandView = (ImageView) findViewById(R.id.expend);
        GridLayout gridLayout = (GridLayout) findViewById(R.id.gridview);

        initGridLayout(gridLayout);
    }

    private void initGridLayout(GridLayout gridLayout) {
        Animation am = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.showout);
        gridLayout.setLayoutAnimation(new LayoutAnimationController(am));
        int[] ids = {R.drawable.p1, R.drawable.p2, R.drawable.p3,
                R.drawable.p4, R.drawable.p5, R.drawable.p6};
        for (int i=0;i<ids.length;i++) {
            final ImageView imageView = (ImageView) LayoutInflater.from(
                    getApplicationContext()).inflate(R.layout.grid_item, gridLayout, false);

            imageView.setImageResource(ids[i]);
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ZoomInView((ImageView) v);

                }
            });
            imageView.setTag(""+(i+1));
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ThemeActivity.this);
                    builder.setTitle("主题更改");
                    builder.setMessage("是否选择当前主题 ??");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int themeId = Integer.parseInt(imageView.getTag() + "");
                            MediaKeeper.writeTheme(ThemeActivity.this,themeId);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                    return false;
                }
            });
            gridLayout.addView(imageView);
        }
    }


    private Animator createZoomInAnimator(View target) {
        Rect thumbRect = new Rect();
        Rect expandRect = new Rect();
        target.getGlobalVisibleRect(thumbRect);
        ((ViewGroup) target.getParent()).getGlobalVisibleRect(expandRect);
        float scale = 1;
        // calculate scale
        float scaleX = thumbRect.width() / (float) expandRect.width();
        float scaleY = thumbRect.height() / (float) expandRect.height();
        scale = Math.max(scaleX, scaleY);
        // calculate translate x and y
        int x = expandRect.centerX() - thumbRect.centerX();
        int y = expandRect.centerY() - thumbRect.centerY();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(expandView, View.SCALE_X,
                scale, 1f), ObjectAnimator.ofFloat(expandView, View.SCALE_Y,
                scale, 1f), ObjectAnimator.ofFloat(expandView,
                View.TRANSLATION_X, 0 - x, 0), ObjectAnimator.ofFloat(
                expandView, View.TRANSLATION_Y, 0 - y, 0));
        set.setDuration(300);
        return set;
    }

    private Animator createZoomOutAnimator(View target) {
        Rect thumbRect = new Rect();
        Rect expandRect = new Rect();
        target.getGlobalVisibleRect(thumbRect);
        ((ViewGroup) target.getParent()).getGlobalVisibleRect(expandRect);
        float scale = 1;
        // calculate scale
        float scaleX = thumbRect.width() / (float) expandRect.width();
        float scaleY = thumbRect.height() / (float) expandRect.height();
        scale = Math.max(scaleX, scaleY);
        // calculate translate x and y
        int x = expandRect.centerX() - thumbRect.centerX();
        int y = expandRect.centerY() - thumbRect.centerY();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(expandView, View.SCALE_X, 1f,
                scale), ObjectAnimator.ofFloat(expandView, View.SCALE_Y, 1f,
                scale), ObjectAnimator.ofFloat(expandView, View.TRANSLATION_X,
                0, 0 - x), ObjectAnimator.ofFloat(expandView,
                View.TRANSLATION_Y, 0, 0 - y));
        set.setDuration(300);
        return set;
    }

    private void ZoomInView(final ImageView target) {
        expandView.setVisibility(View.VISIBLE);
        expandView.setImageDrawable(target.getDrawable());
        target.setVisibility(View.INVISIBLE);
        Animator animator = createZoomInAnimator(target);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                expandView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomOutView(target);
                    }
                });
            }
        });
    }

    private void zoomOutView(final ImageView target) {
        expandView.setOnClickListener(null);
        expandView.setVisibility(View.VISIBLE);
        Animator animator = createZoomOutAnimator(target);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.VISIBLE);
                expandView.setVisibility(View.INVISIBLE);
                super.onAnimationEnd(animation);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.setResult(0x020);
    }
}

