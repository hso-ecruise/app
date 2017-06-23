package ecruise.navi;

import android.support.test.rule.ActivityTestRule;

import com.google.android.gms.maps.model.Marker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Created by Aivan on 10.06.2017.
 */
public class MainActivityTest
{

    @Rule
    public ActivityTestRule mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void onCreate() throws Exception {
        onView(withText("Zentrieren")).perform(click());
    }

}