package com.indiewalk.watchdog.earthquake


import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.intent.Intents
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent

import com.indiewalk.watchdog.earthquake.UI.MainActivityEarthquakesList
import com.indiewalk.watchdog.earthquake.UI.SettingSimpleActivity

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.Espresso.pressBackUnconditionally
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf


@LargeTest
@RunWith(AndroidJUnit4::class)
class CheckMainActivityButton {
    private lateinit var activity: MainActivityEarthquakesList

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivityEarthquakesList::class.java)


    @Before
    fun setUp() {
        activity = mActivityTestRule.activity

        // set consentSDK avoid showing consent banner
        activity.consentSDKNeed = false


        // Otherwise the workaround would be :
        // - delete preferences on device app
        // - launch one time and give admob consent
    }


    @Test
    fun checkRefreshButton() {
        val refresh_btn = onView(withId(R.id.refresh_action)).check(matches(isDisplayed()))
        refresh_btn.perform(click())

    }

    @Test
    fun checkInfoButton() {
        val filter_btn = onView(withId(R.id.info_filter_fb)).check(matches(isDisplayed()))
        filter_btn.perform(click())
        onView(withId(R.id.filter_memo)).check(matches(isDisplayed()))
    }

    @Test
    fun checkQuickSettingButton() {
        val main_menu_opened = onView(withId(R.id.action_settings)).check(matches(isDisplayed()))
        main_menu_opened.perform(click())

        // onView(withId(R.id.quick_settings)).perform(click());
        // onView(withContentDescription(R.string.quick_settings_title)).perform(click());
        onView(withText("Impostazioni Veloci")).perform(click())

    }


    @Test
    fun checkGeneralSettingButton() {
        Intents.init()
        val main_menu_opened = onView(withId(R.id.action_settings)).check(matches(isDisplayed()))
        main_menu_opened.perform(click())

        onView(withText("Impostazioni Generali")).perform(click())
        intended(hasComponent(SettingSimpleActivity::class.java.name))
        pressBack()
        // intended(hasComponent(MainActivityEarthquakesList.class.getName()));
        Intents.release()
    }


    @Test
    fun checkPositionButton() {
        Intents.init()
        val map_btn = onView(withId(R.id.set_myposition_action)).check(matches(isDisplayed()))
        map_btn.perform(click())
        intended(hasComponent(MapsActivity::class.java.name))
        pressBack()
        // intended(hasComponent(MainActivityEarthquakesList.class.getName()));
        Intents.release()
    }


}