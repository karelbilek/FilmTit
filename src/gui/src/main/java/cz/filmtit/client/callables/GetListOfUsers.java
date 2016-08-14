/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.callables;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import cz.filmtit.client.Callable;
import cz.filmtit.share.User;
import java.util.List;

/**
 *
 * @author matus
 */
public class GetListOfUsers extends Callable<List<User>>{

    private MultiWordSuggestOracle oracle;
    
    public GetListOfUsers(MultiWordSuggestOracle oracle) {
        super();
        this.oracle = oracle;
        enqueue();
    }
    
    @Override
    public void onSuccessAfterLog(List<User> result) {

        for (User user : result) {
            oracle.add(user.getName());
        }

    }
    
    
    @Override
    protected void call() {
        filmTitService.getListOfUsers(this);
    }
    
}
