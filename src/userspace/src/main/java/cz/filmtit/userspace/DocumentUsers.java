/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.userspace;

import java.io.Serializable;

/**
 *
 * @author matus
 */
public class DocumentUsers implements Serializable{
    
    private volatile Long id;
    private volatile USUser user;
    
    public DocumentUsers() {
        //nothing;
    }
    
    public DocumentUsers(USUser user) {
        this.user = user;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the user
     */
    public USUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(USUser user) {
        this.user = user;
    }
    
    
    
}
