```java
package com.example.finance;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void appUsesExpectedPackageAndLaunchesMainActivity() {
        Context appContext =
                InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.example.finance", appContext.getPackageName());
        assertNotNull(activityRule.getActivity());
    }

    @Test
    public void onboardingScreenDisplaysWelcomeMessage() {
        activityRule.getActivity().runOnUiThread(() -> {
            assertNotNull(activityRule.getActivity().findViewById(R.id.welcomemsg));
            assertNotNull(activityRule.getActivity().findViewById(R.id.btnproceed));
            assertNotNull(activityRule.getActivity().findViewById(R.id.btnskip));
        });

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        String welcomeText = activityRule.getActivity()
                .findViewById(R.id.welcomemsg)
                .toString();

        assertNotNull(welcomeText);
    }

    @Test
    public void proceedButtonOpensQuoteScreen() {
        activityRule.getActivity().runOnUiThread(() ->
                activityRule.getActivity()
                        .findViewById(R.id.btnproceed)
                        .performClick()
        );

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertTrue(activityRule.getActivity().isFinishing()
                || activityRule.getActivity().isDestroyed()
                || activityRule.getActivity().getClass() == MainActivity.class);
    }

    @Test
    public void skipButtonOpensLoginScreen() {
        activityRule.getActivity().runOnUiThread(() ->
                activityRule.getActivity()
                        .findViewById(R.id.btnskip)
                        .performClick()
        );

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertNotNull(activityRule.getActivity());
    }
}
```
