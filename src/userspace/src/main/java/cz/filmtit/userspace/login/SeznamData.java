package cz.filmtit.userspace.login;

import cz.filmtit.userspace.USLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class gets information from return url from openID authority - Seznam.cz
 *
 * @author Pepa
 */

public class SeznamData{

    /**
     *  Extracted login
     */
    private String login;

    /**
     * Extracted openID
     */
    private String openid;

    /**
     * Extracted email address
     */
    private String email;

    /**
     * Extract data from given url - url given by openID
     * authority - Seznam.cz
     * @param url
     */
    public SeznamData(String url) {
        USLogger logger = USLogger.getInstance();
        try {
        StringBuilder encodedUrl = new StringBuilder(url);
        Pattern claimed_id = Pattern.compile(new String("claimed_id="));
        Matcher cm = claimed_id.matcher(encodedUrl);
        if (cm.find()){
        int startIndex = cm.start();
        int endIndex = encodedUrl.indexOf("&",startIndex);

        openid = decodeUrl(encodedUrl.substring(startIndex + 11 , endIndex));

        email = openid.replace(".id.","@");

        login = openid.substring(0,openid.indexOf(".id."));

        }
        else{
            throw new IllegalArgumentException("No claimed_id found");
        }
            StringBuilder data = new StringBuilder();
            logger.info("SeznamData/Init","Url " + url + " succesfully parsed!");
        }
        catch (IllegalArgumentException e)
        {

            logger.error("SeznamData/Init",e.getMessage());
            login = "";
            openid = "";
            email = "";
        }
    }

    /**
     * Get extracted login
     * @return   login
     */
    public String getLogin(){
        return login;
    }

    /**
     * Get extracted openID
     * @return  openID
     */
    public String getOpenId(){
        return openid;
    }

    /**
     Get extracted email address
     * @return email address
     */
    public String getEmail(){
        return email;
    }

    /**
     * Returns information about extraction data from url
     * @return Infomation if extracting ends without problems
     */
    public boolean isOk()
    {
        return !(getLogin().isEmpty() || getOpenId().isEmpty());
    }

    /**
     * Replacing Base64 chars in url
     * @param data  origin url
     * @return decoded url
     */
    private String decodeUrl(String  data){
        data = data.replace("%3A",":");
        data = data.replace("%2F","//");
        return data.substring(9,data.length()-2);
    }
}