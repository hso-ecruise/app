package data;

import data.CustomerData;

public class Server
{
    private static Server ourInstance = new Server();

    public static Server getInstance()
    {
        return ourInstance;
    }

    private Server()
    {
    }

    public boolean checkLoginData(String email, String password) {
        return true;
    }

    public CustomerData getUserData(){
        CustomerData customerData = new CustomerData();

        //server call to get UserData
        customerData.city = "Offenburg";
        customerData.country = "Deutschland";
        customerData.email = "a@a.a";
        customerData.extraAddressLine = "";
        customerData.houseNumber = "8";
        customerData.lastname = "Lustig";
        customerData.name = "Peter";
        customerData.password = "aaaaa";
        customerData.phoneNumber = "12345";
        customerData.street = "Regenbogen-Boulevard";
        customerData.zipCode = "99999";

        return customerData;
    }

    public boolean updateUserData(CustomerData customerData){

        //server call to set UserData

        //true if successfull
        return true;
    }

}
