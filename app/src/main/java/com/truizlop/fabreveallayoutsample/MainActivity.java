/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.truizlop.fabreveallayoutsample;

import android.animation.ObjectAnimator;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.truizlop.fabreveallayout.FABRevealLayout;
import com.truizlop.fabreveallayout.OnRevealChangeListener;

public class MainActivity extends AppCompatActivity {

    private FABRevealLayout fabRevealLayout;
    private TextView albumTitleText;
    private TextView artistNameText;
    private SeekBar songProgress;
    private TextView songTitleText;
    private ImageView prev;
    private ImageView stop;
    private ImageView next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        configureFABReveal();
    }

    private void findViews() {
        fabRevealLayout = (FABRevealLayout) findViewById(R.id.fab_reveal_layout);
        albumTitleText = (TextView) findViewById(R.id.album_title_text);
        artistNameText = (TextView) findViewById(R.id.artist_name_text);
        songProgress = (SeekBar) findViewById(R.id.song_progress_bar);
        styleSeekbar(songProgress);

        songTitleText = (TextView) findViewById(R.id.song_title_text);
        prev = (ImageView) findViewById(R.id.previous);
        stop = (ImageView) findViewById(R.id.stop);
        next = (ImageView) findViewById(R.id.next);
    }

    private void styleSeekbar(SeekBar songProgress) {
        int color = getResources().getColor(R.color.background);
        songProgress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        songProgress.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void configureFABReveal() {
        fabRevealLayout.setOnRevealChangeListener(new OnRevealChangeListener() {
            @Override
            public void onMainViewAppeared(FABRevealLayout fabRevealLayout, View mainView) {
                showMainViewItems();
            }

            @Override
            public void onSecondaryViewAppeared(final FABRevealLayout fabRevealLayout, View secondaryView) {
                showSecondaryViewItems();
                prepareBackTransition(fabRevealLayout);
            }
        });
    }

    private void showMainViewItems() {
        scale(albumTitleText, 50);
        scale(artistNameText, 150);
    }

    private void showSecondaryViewItems() {
        scale(songProgress, 0);
        animateSeekBar(songProgress);
        scale(songTitleText, 100);
        scale(prev, 150);
        scale(stop, 100);
        scale(next, 200);
    }

    private void prepareBackTransition(final FABRevealLayout fabRevealLayout) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fabRevealLayout.revealMainView();
            }
        }, 5000);
    }

    private void scale(View view, long delay){
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(500)
                .setStartDelay(delay)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateSeekBar(SeekBar seekBar){
        seekBar.setProgress(15);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(seekBar, "progress", 15, 0);
        progressAnimator.setDuration(300);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.start();
    }
}
