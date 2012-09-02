package cz.filmtit.userspace.login;

import java.util.Calendar;
import java.util.Date;

/**
 * Token which is send in email with url address and its validity
 * now you want change pass but your token is valid only one our after asking change pass
 * you can deactivate it after succesfull change
 *
 * @author Pepa
 */
public class ChangePassToken {

    private String token;
    private Date  validTo;
    private Boolean active;

    public String getToken() {
        return token;
    }

    /**
     * @param token - string which is used like  key
     * Creates active instance of ChangePassToken, which is valid 1h hour
     */
    public ChangePassToken(String token){
        setToken(token);
        // set validity of token now()+1h
        Date actualDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(actualDate);
        cal.add(Calendar.HOUR_OF_DAY,1);
        setValidTo(cal.getTime());
        active = true;

    }
    /**
     * @param token - string which is used like key
     * Find out if actual instance of ChangePassToken
     * contains same token like key and if actual instance
     * is valid
     * @return Validity of actual instance to token
     */
    public boolean isValidToken(String token){
        // token is the same and its validity isn`t off
        return ( (this.token.compareTo(token)==0) && (isValidTime()) && (this.active()));
    }


   /**
    * Check if actual instance is still active and
    * its time limit is not expired
    * @return Validity of actual instance
    */
    public boolean  isValidTime() {
        Date actual = new Date();
        return ((validTo.compareTo(actual) > 0) && (this.active()));
    }

   /**
    * Deactivate actual instance
    */
    public void deactivate() {
        this.active = false;
    }

   /**
    * Activate actual instance
    */
    public boolean active() {
        return this.active;
    }

   /**
    * Set time limit of validation
    */
    private void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

   /**
    * Set token to actual instance
    */
    private void setToken(String token) {
        this.token = token;
    }
}
