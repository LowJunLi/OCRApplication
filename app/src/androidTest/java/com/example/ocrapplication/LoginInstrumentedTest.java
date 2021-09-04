package com.example.ocrapplication;

import static android.content.Context.MODE_PRIVATE;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LoginInstrumentedTest
{
    static SharedPreferences pref;

    @Rule
    public ActivityScenarioRule<Login> rule = new ActivityScenarioRule<>(Login.class);

    /**
     * Set up environment for testing
     */
    @BeforeClass
    public static void setUp()
    {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        pref = context.getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }

    @Test
    public void correctUsernameAndPasswordShouldLogin()
    {
        String correctUsername = pref.getString("username", "User");
        String correctPassword = pref.getString("password", "Password");

        onView(withId(R.id.login_etUsername)).perform(typeText(correctUsername));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.login_etPassword)).perform(typeText(correctPassword));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.login_btnLogin)).perform(click());
        //if the btnViewAllData exist, this mean the user successfully see the main menu
        // (login successful)
        onView(withId(R.id.main_btnViewAllData)).check(matches(isDisplayed()));
    }
}