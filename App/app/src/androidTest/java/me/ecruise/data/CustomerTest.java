package me.ecruise.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by Jens Ullrich on 06.06.2017.
 */
@RunWith(AndroidJUnit4.class)
public class CustomerTest {

    Context appContext;
    @Before
    public void setUp() throws Exception {
        appContext = InstrumentationRegistry.getTargetContext();
        assertNotNull(appContext);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getInstance() throws Exception {
        Customer testCustomer = null;
        testCustomer = Customer.getInstance(null);
        assertNull(testCustomer);
        testCustomer = Customer.getInstance(appContext);
        assertNotNull(testCustomer);
        testCustomer = Customer.getInstance(null);
        assertNotNull(testCustomer);
        testCustomer = Customer.getInstance(appContext);
        assertNotNull(testCustomer);
    }


    @Test
    public void checkForChanges() throws Exception {
        Customer testCustomer1 = new Customer(null);
        Customer testCustomer2 = new Customer(null);

        assertFalse(testCustomer1.checkForChanges(testCustomer2));

        testCustomer1.setName("name");
        testCustomer1.setLastname("lastName");
        testCustomer1.setCountry("country");
        testCustomer1.setExtraAddressLine("extra");
        testCustomer1.setHouseNumber("1");
        testCustomer1.setCity("city");
        testCustomer1.setEmail("email");
        testCustomer1.setPassword("password");
        testCustomer1.setPhoneNumber("1234");
        testCustomer1.setStreet("street");
        testCustomer1.setZipCode(77777);

        assertFalse(testCustomer1.checkForChanges(testCustomer2));

        testCustomer2.setName("name");
        testCustomer2.setLastname("lastName");
        testCustomer2.setCountry("country");
        testCustomer2.setExtraAddressLine("extra");
        testCustomer2.setHouseNumber("1");
        testCustomer2.setCity("city");
        testCustomer2.setEmail("email");
        testCustomer2.setPassword("password");
        testCustomer2.setPhoneNumber("1234");
        testCustomer2.setStreet("street");
        testCustomer2.setZipCode(77777);

        assertFalse(testCustomer1.checkForChanges(testCustomer2));

        testCustomer2.setName("newName");
        testCustomer2.setLastname("newLastName");
        testCustomer2.setCountry("newCountry");
        testCustomer2.setExtraAddressLine("new");
        testCustomer2.setHouseNumber("2");
        testCustomer2.setCity("newCity");
        testCustomer2.setEmail("newEmail");
        testCustomer2.setPassword("newPassword");
        testCustomer2.setPhoneNumber("5678");
        testCustomer2.setStreet("newStreet");
        testCustomer2.setZipCode(88888);

        assertTrue(testCustomer1.checkForChanges(testCustomer2));
    }

    @Test
    public void getUserDataFromServer() throws Exception {

    }

    @Test
    public void setUserData() throws Exception {

    }

    @Test
    public void updateUserData() throws Exception {
        Customer testCustomer1 = new Customer(appContext);
        Customer testCustomer2 = new Customer(appContext);
        Customer.DataCallback callback = new Customer.DataCallback() {
            @Override
            public void onSuccess() {
                assertTrue(true);
            }

            @Override
            public void onFailure() {
                assertTrue(false);
            }
        };

        testCustomer1.setName("name");
        testCustomer1.setLastname("lastName");
        testCustomer1.setCountry("country");
        testCustomer1.setExtraAddressLine("extra");
        testCustomer1.setHouseNumber("1");
        testCustomer1.setCity("city");
        testCustomer1.setEmail("email");
        testCustomer1.setPassword("password");
        testCustomer1.setPhoneNumber("1234");
        testCustomer1.setStreet("street");
        testCustomer1.setZipCode(77777);

        testCustomer2.setName("name");
        testCustomer2.setLastname("lastName");
        testCustomer2.setCountry("country");
        testCustomer2.setExtraAddressLine("extra");
        testCustomer2.setHouseNumber("1");
        testCustomer2.setCity("city");
        testCustomer2.setEmail("email");
        testCustomer2.setPassword("password");
        testCustomer2.setPhoneNumber("1234");
        testCustomer2.setStreet("street");
        testCustomer2.setZipCode(77777);

        //testCustomer1.updateUserData(testCustomer2, callback);

        testCustomer2.setName("newName");
        testCustomer2.setLastname("newLastName");
        testCustomer2.setCountry("newCountry");
        testCustomer2.setExtraAddressLine("new");
        testCustomer2.setHouseNumber("2");
        testCustomer2.setCity("newCity");
        testCustomer2.setEmail("newEmail");
        testCustomer2.setPassword("newPassword");
        testCustomer2.setPhoneNumber("5678");
        testCustomer2.setStreet("newStreet");
        testCustomer2.setZipCode(88888);

        //testCustomer1.updateUserData(testCustomer2, callback);
    }

    @Test
    public void patchString() throws Exception {

    }

    @Test
    public void patchJSON() throws Exception {

    }

    @Test
    public void registerUser() throws Exception {

    }
}