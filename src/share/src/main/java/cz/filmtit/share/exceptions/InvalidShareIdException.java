/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.share.exceptions;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 *
 * @author matus
 */
public class InvalidShareIdException extends Exception implements Serializable, IsSerializable{

    InvalidShareIdException() {
    }
    
    public InvalidShareIdException(String message) {
        super(message);
    }
    
}
