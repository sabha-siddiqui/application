package com.easyfitness.intro;

import android.content.Intent;
import android.os.Bundle;

import com.easyfitness.DAO.DATAProfile;
import com.easyfitness.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;
import com.heinrichreimersoftware.materialintro.slide.Slide;

public class MainIntroActivity extends IntroActivity {

    public static final String EXTRA_FULLSCREEN = "com.heinrichreimersoftware.materialintro.demo.EXTRA_FULLSCREEN";
    public static final String EXTRA_SCROLLABLE = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SCROLLABLE";
    public static final String EXTRA_CUSTOM_FRAGMENTS = "com.heinrichreimersoftware.materialintro.demo.EXTRA_CUSTOM_FRAGMENTS";
    public static final String EXTRA_PERMISSIONS = "com.heinrichreimersoftware.materialintro.demo.EXTRA_PERMISSIONS";
    public static final String EXTRA_SHOW_BACK = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_BACK";
    public static final String EXTRA_SHOW_NEXT = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_NEXT";
    public static final String EXTRA_SKIP_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SKIP_ENABLED";
    public static final String EXTRA_FINISH_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_FINISH_ENABLED";
    public static final String EXTRA_GET_STARTED_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_GET_STARTED_ENABLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();

        boolean customFragments = intent.getBooleanExtra(EXTRA_CUSTOM_FRAGMENTS, true);
        boolean permissions = intent.getBooleanExtra(EXTRA_PERMISSIONS, true);
        boolean showBack = intent.getBooleanExtra(EXTRA_SHOW_BACK, true);
        boolean showNext = intent.getBooleanExtra(EXTRA_SHOW_NEXT, true);
        boolean skipEnabled = intent.getBooleanExtra(EXTRA_SKIP_ENABLED, false);
        boolean finishEnabled = intent.getBooleanExtra(EXTRA_FINISH_ENABLED, true);
        boolean getStartedEnabled = intent.getBooleanExtra(EXTRA_GET_STARTED_ENABLED, false);

        setFullscreen(false);

        super.onCreate(savedInstanceState);

        setButtonBackFunction(skipEnabled ? BUTTON_BACK_FUNCTION_SKIP : BUTTON_BACK_FUNCTION_BACK);
        setButtonNextFunction(finishEnabled ? BUTTON_NEXT_FUNCTION_NEXT_FINISH : BUTTON_NEXT_FUNCTION_NEXT);
        setButtonBackVisible(showBack);
        setButtonNextVisible(showNext);
        setButtonCtaVisible(getStartedEnabled);
        setButtonCtaTintMode(BUTTON_CTA_TINT_MODE_TEXT);


        addSlide(new SimpleSlide.Builder()
            .title(R.string.introSlide1Title)
            .description("Welcome to Fitness App! This application is a quick and simple way to record your program and follow your progression")
            .image(R.drawable.web_hi_res_512)
            .background(R.color.launcher_background)
            .backgroundDark(R.color.launcher_background)
            .scrollable(true)
            .build());



        DATAProfile mDbProfils = new DATAProfile(this.getApplicationContext());
        if (mDbProfils.getCount() == 0) {
            final Slide profileSlide;
            profileSlide = new FragmentSlide.Builder()
                .background(R.color.launcher_background)
                .backgroundDark(R.color.launcher_background)
                .fragment(NewProfileFragment.newInstance())
                .build();
            addSlide(profileSlide);
        }
    }
}
