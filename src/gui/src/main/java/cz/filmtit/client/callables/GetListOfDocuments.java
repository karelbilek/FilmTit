package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.UserPage;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

    public class GetListOfDocuments extends Callable<List<Document>> {

    	// parameters
    	UserPage userpage;
    	
        @Override
        public String getName() {
            return "getListOfDocuments";
        }
        
        @Override
        public void onSuccessAfterLog(List<Document> result) {
            Gui.log("received " + result.size() + " documents");
            
            userpage.setDocuments(result);
            for (Document d:result) {
                Gui.log("GUI Dalsi document. Ma "+d.getTranslationResults().size()+" prfku.");
            }
        }

            


        // constructor
		public GetListOfDocuments(UserPage userpage) {
			super();

			this.userpage = userpage;
			
			// 20s
			callTimeOut = 20000;
			
			enqueue();
		}
        

		@Override protected void call() {
	        filmTitService.getListOfDocuments(Gui.getSessionID(), this);
		}

	}
