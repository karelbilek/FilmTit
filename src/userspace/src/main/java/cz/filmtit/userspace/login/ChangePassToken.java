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

    public boolean isValidToken(String token){
        // token is the same and its validity isn`t off
        return ( (this.token.compareTo(token)==0) && (isValidTime()) && (this.active()));
    }

    public boolean  isValidTime() {
        Date actual = new Date();
        return ((validTo.compareTo(actual) > 0) && (this.active()));
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean active() {
        return this.active;
    }

    private void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    private void setToken(String token) {
        this.token = token;
    }
}
