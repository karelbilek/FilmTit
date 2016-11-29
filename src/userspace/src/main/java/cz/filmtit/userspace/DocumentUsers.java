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
    private volatile Long userId;
    private volatile String moviePath;
    
    public DocumentUsers() {
        //nothing;
    }
    
    public DocumentUsers(Long userId) {
        this.userId = userId;
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
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the moviePath
     */
    public String getMoviePath() {
        return moviePath;
    }

    /**
     * @param moviePath the moviePath to set
     */
    public void setMoviePath(String moviePath) {
        this.moviePath = moviePath;
    }


    
    
    
}
