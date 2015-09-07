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
package com.truizlop.fabreveallayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class FABRevealLayout extends RelativeLayout {

    private static final int MAX_CHILD_VIEWS = 2;
    private static final int FAB_SIZE = 48;
    private static final int ANIMATION_DURATION = 500;
    private final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private List<View> childViews = null;
    private FloatingActionButton fab = null;
    private CircularExpandingView circularExpandingView = null;
    private OnRevealChangeListener onRevealChangeListener = null;
    private OnClickListener fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            revealSecondaryView();
        }
    };

    public FABRevealLayout(Context context) {
        this(context, null);
    }

    public FABRevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FABRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        childViews = new ArrayList<>(2);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        setupView(child);
        super.addView(child, index, params);

        if(areAllComponentsReady()){
            setupInitialState();
        }
    }

    private void setupView(View child) {
        if(child instanceof FloatingActionButton){
            setupFAB(child);
        }else if(!(child instanceof CircularExpandingView)){
            setupChildView(child);
        }
    }

    private void setupFAB(View view){
        validateFAB();
        fab = (FloatingActionButton) view;
        fab.setOnClickListener(fabClickListener);
    }

    private void setupChildView(View view){
        validateChildView();
        childViews.add(view);
        if(childViews.size() == 1){
            addCircularRevealView();
        }
    }

    private void validateFAB() {
        if(fab != null){
            throw new IllegalArgumentException("FABRevealLayout can only hold one FloatingActionButton");
        }
    }

    private void validateChildView() {
        if(childViews.size() >= MAX_CHILD_VIEWS){
            throw new IllegalArgumentException("FABRevealLayout can only hold two views");
        }
    }

    private void addCircularRevealView() {
        circularExpandingView = new CircularExpandingView(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.topMargin = dipsToPixels(FAB_SIZE);
        circularExpandingView.setVisibility(View.GONE);
        addView(circularExpandingView, params);
    }

    private boolean areAllComponentsReady(){
        return fab != null && childViews.size() == MAX_CHILD_VIEWS;
    }

    private void setupInitialState(){
        setupFABPosition();
        setupChildViewsPosition();
    }

    private void setupFABPosition(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.rightMargin = dipsToPixels(16);
            params.topMargin = dipsToPixels(20);
        }
        fab.bringToFront();
    }

    private void setupChildViewsPosition(){
        for(int i = 0; i < childViews.size(); i++){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) childViews.get(i).getLayoutParams();
            params.topMargin = dipsToPixels(FAB_SIZE);
        }
        getSecondaryView().setVisibility(GONE);
    }

    private boolean isShowingMainView(){
        return getMainView().getVisibility() == VISIBLE;
    }

    public void revealMainView(){
        if(!isShowingMainView()){
            startHideAnimation();
        }
    }

    public void revealSecondaryView(){
        if(isShowingMainView()){
            startRevealAnimation();
        }
    }

    public void setOnRevealChangeListener(OnRevealChangeListener onRevealChangeListener) {
        this.onRevealChangeListener = onRevealChangeListener;
    }

    private void startRevealAnimation(){
        View disappearingView = getMainView();

        ObjectAnimator fabAnimator = getFABAnimator();
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(disappearingView, "alpha", 1, 0);

        AnimatorSet set = new AnimatorSet();
        set.play(fabAnimator).with(alphaAnimator);
        setupAnimationParams(set);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fab.setVisibility(GONE);
                prepareForReveal();
                expandCircle();
            }
        } );

        set.start();
    }

    private void prepareForReveal() {
        circularExpandingView.getLayoutParams().height = getMainView().getHeight();
        circularExpandingView.setColor(fab.getBackgroundTintList() != null ?
                        fab.getBackgroundTintList().getDefaultColor() :
                        0xFF000000
        );
        circularExpandingView.setVisibility(VISIBLE);
    }

    private void setupAnimationParams(Animator animator) {
        animator.setInterpolator(INTERPOLATOR);
        animator.setDuration(ANIMATION_DURATION);
    }

    private CurvedAnimator getCurvedAnimator() {
        View view = getMainView();

        float fromX = fab.getLeft();
        float fromY = fab.getTop();
        float toX = view.getWidth() / 2 - fab.getWidth() / 2 + view.getLeft();
        float toY = view.getHeight() / 2 - fab.getHeight() / 2 + view.getTop();

        if(isShowingMainView()) {
            return new CurvedAnimator(fromX, fromY, toX, toY);
        }else{
            return new CurvedAnimator(toX, toY, fromX, fromY);
        }
    }

    private ObjectAnimator getFABAnimator(){
        CurvedAnimator curvedAnimator = getCurvedAnimator();
        return ObjectAnimator.ofObject(this, "fabPosition", new CurvedPathEvaluator(), curvedAnimator.getPoints());
    }

    private void expandCircle(){
        Animator expandAnimator = circularExpandingView.expand();;
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                swapViews();
            }
        });
        expandAnimator.start();
    }

    private void startHideAnimation(){
        Animator contractAnimator = circularExpandingView.contract();
        View disappearingView = getSecondaryView();
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(disappearingView, "alpha", 1, 0);

        AnimatorSet set = new AnimatorSet();
        set.play(contractAnimator).with(alphaAnimator);
        setupAnimationParams(set);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fab.setVisibility(VISIBLE);
                circularExpandingView.setVisibility(GONE);
                moveFABToOriginalLocation();
            }
        });
        set.start();
    }

    private void moveFABToOriginalLocation(){
        ObjectAnimator fabAnimator = getFABAnimator();

        setupAnimationParams(fabAnimator);
        fabAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                swapViews();
            }
        });

        fabAnimator.start();
    }

    public void setFabPosition(Point point){
        fab.setX(point.x);
        fab.setY(point.y);
    }

    private void swapViews() {
        if(isShowingMainView()){
            getMainView().setVisibility(GONE);
            getMainView().setAlpha(1);
            getSecondaryView().setVisibility(VISIBLE);
            circularExpandingView.setVisibility(VISIBLE);
        }else{
            getMainView().setVisibility(VISIBLE);
            getSecondaryView().setVisibility(GONE);
            getSecondaryView().setAlpha(1);
            circularExpandingView.setVisibility(View.GONE);
        }
        notifyListener();
    }

    private void notifyListener(){
        if(onRevealChangeListener != null){
            if(isShowingMainView()){
                onRevealChangeListener.onMainViewAppeared(this, getMainView());
            }else{
                onRevealChangeListener.onSecondaryViewAppeared(this, getSecondaryView());
            }
        }
    }

    private View getSecondaryView() {
        return childViews.get(1);
    }

    private View getMainView() {
        return childViews.get(0);
    }

    private int dipsToPixels(float dips){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        ((MarginLayoutParams) params).topMargin -= dipsToPixels(FAB_SIZE);
        super.setLayoutParams(params);
    }

}
