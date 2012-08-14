package cz.filmtit.userspace.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SeznamData{

    private String login;
    private String openid;
    private String email;

    public SeznamData(String url) {

        System.out.println(url);
        StringBuilder encodedUrl = new StringBuilder(url);
        Pattern claimed_id = Pattern.compile(new String("claimed_id="));
        Matcher cm = claimed_id.matcher(encodedUrl);
        if (cm.find()){
        int startIndex = cm.start();
        int endIndex = encodedUrl.indexOf("&",startIndex);
        System.out.println(String.valueOf(endIndex));
        openid = decodeUrl(encodedUrl.substring(startIndex + 11 , endIndex));
        System.out.println(openid);
        email = openid.replace(".id.","@");
        System.out.println(email);
        login = openid.substring(0,openid.indexOf(".id."));
        System.out.println(login  );
        }
        else{
            throw new IllegalArgumentException("No claimed_id found");
        }
    }

    public String getLogin(){
        return login;
    }

    public String getOpenId(){
        return openid;
    }

    public String getEmail(){
        return email;
    }
    private String decodeUrl(String  data){
        data = data.replace("%3A",":");
        data = data.replace("%2F","//");
        return data.substring(9,data.length()-2);
    }
}