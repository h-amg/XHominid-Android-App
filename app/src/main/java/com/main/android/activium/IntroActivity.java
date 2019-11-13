package com.main.android.activium;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();


        //// Add your slide fragments

        // Today's meals
        SliderPage todaysMealsSlide = new SliderPage();
        todaysMealsSlide.setTitle(getResources().getString(R.string.todays_meals_title));
        todaysMealsSlide.setDescription(getResources().getString(R.string.todays_meals_descrip));
        todaysMealsSlide.setImageDrawable(R.mipmap.intro_todays_meal);
        todaysMealsSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(todaysMealsSlide));

        // Meal plans
        SliderPage mealPlansSlide = new SliderPage();
        mealPlansSlide.setTitle(getResources().getString(R.string.meal_plans_title));
        mealPlansSlide.setDescription(getResources().getString(R.string.meal_plans_descrip));
        mealPlansSlide.setImageDrawable(R.mipmap.intro_meal_plans);
        mealPlansSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(mealPlansSlide));

        // Shopping list
        SliderPage shoppingListSlide = new SliderPage();
        shoppingListSlide.setTitle(getResources().getString(R.string.shopping_list_title));
        shoppingListSlide.setDescription(getResources().getString(R.string.shopping_list_descrip));
        shoppingListSlide.setImageDrawable(R.mipmap.intro_shopping_list);
        shoppingListSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(shoppingListSlide));

        // Consultation sessions
        SliderPage consultationSessionsSlide = new SliderPage();
        consultationSessionsSlide.setTitle(getResources().getString(R.string.consultation_sessions_title));
        consultationSessionsSlide.setDescription(getResources().getString(R.string.consultation_sessions_descrip));
        consultationSessionsSlide.setImageDrawable(R.mipmap.intro_consultation_sessions);
        consultationSessionsSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(consultationSessionsSlide));


        // Feedback
        SliderPage feedbackSlide = new SliderPage();
        feedbackSlide.setTitle(getResources().getString(R.string.feedback_intro_title));
        feedbackSlide.setDescription(getResources().getString(R.string.feedback_intro_descrip));
        feedbackSlide.setImageDrawable(R.mipmap.intro_feedback);
        feedbackSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(feedbackSlide));

        // Nav bar
        SliderPage navBarSlide = new SliderPage();
        navBarSlide.setTitle(getResources().getString(R.string.nav_bar_title));
        navBarSlide.setDescription(getResources().getString(R.string.nav_bar_descrip));
        navBarSlide.setImageDrawable(R.mipmap.intro_nav_bar);
        navBarSlide.setBgColor(R.color.black);
        addSlide(AppIntroFragment.newInstance(navBarSlide));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(Color.parseColor("#CC1D1D"));
        setSeparatorColor(Color.parseColor("#ffffff"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // send to main activity
        final Intent i = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // send to main activity
        final Intent i = new Intent(IntroActivity.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
