package me.ecruise.data;

public class CustomerData {

    public CustomerData()
    {
        nameChanged = false;
        lastnameChanged = false;
        emailChanged = false;
        phoneNumberChanged = false;
        passwordChanged = false;
        streetChanged = false;
        houseNumberChanged = false;
        extraAddressLineChanged = false;
        zipCodeChanged = false;
        cityChanged = false;
        countryChanged = false;
    }

    public String name;
    public String lastname;
    public String email;
    public String phoneNumber;
    public String password;
    public String street;
    public String houseNumber;
    public String extraAddressLine;
    public String zipCode;
    public String city;
    public String country;

    private boolean nameChanged;
    private boolean lastnameChanged;
    private boolean emailChanged;
    private boolean phoneNumberChanged;
    private boolean passwordChanged;
    private boolean streetChanged;
    private boolean houseNumberChanged;
    private boolean extraAddressLineChanged;
    private boolean zipCodeChanged;
    private boolean cityChanged;
    private boolean countryChanged;

    public boolean checkForChanges(CustomerData newCustomerData){
        nameChanged = !this.name.equals(newCustomerData.name);
        lastnameChanged = !this.lastname.equals(newCustomerData.lastname);
        emailChanged = !this.email.equals(newCustomerData.email);
        phoneNumberChanged = !this.phoneNumber.equals(newCustomerData.phoneNumber);
        passwordChanged = !this.password.equals(newCustomerData.password);
        streetChanged = !this.street.equals(newCustomerData.street);
        houseNumberChanged = !this.houseNumber.equals(newCustomerData.houseNumber);
        extraAddressLineChanged = !this.extraAddressLine.equals(newCustomerData.extraAddressLine);
        zipCodeChanged = !this.zipCode.equals(newCustomerData.zipCode);
        cityChanged = !this.city.equals(newCustomerData.city);
        countryChanged = !this.country.equals(newCustomerData.country);

        return (nameChanged|
                lastnameChanged|
                emailChanged|
                phoneNumberChanged|
                passwordChanged|
                streetChanged|
                houseNumberChanged|
                extraAddressLineChanged|
                zipCodeChanged|
                cityChanged|
                countryChanged);
    }
}
