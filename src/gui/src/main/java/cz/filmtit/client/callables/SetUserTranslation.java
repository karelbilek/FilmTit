package cz.filmtit.client.callables;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import cz.filmtit.client.*;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

public class SetUserTranslation extends Callable<Void> {
		
		// parameters
		ChunkIndex chunkIndex;
		long documentId;
		String userTranslation;
		long chosenTranslationPair;

	
        @Override
        public String getName() {
            return "setUserTranslation("+chunkIndex.toString()+","+documentId+","+userTranslation+","+chosenTranslationPair+")";
        }

        @Override
        public void onSuccessAfterLog(Void o) {
        }
        
        // constructor
		public SetUserTranslation(ChunkIndex chunkIndex, long documentId,
				String userTranslation, long chosenTranslationPair) {		
			super();
			
			this.chunkIndex = chunkIndex;
			this.documentId = documentId;
			this.userTranslation = userTranslation;
			this.chosenTranslationPair = chosenTranslationPair;
			
	        enqueue();
		}

		@Override
		public void call() {
			filmTitService.setUserTranslation(gui.getSessionID(), chunkIndex,
					documentId, userTranslation, chosenTranslationPair,
					this);
		}
	}

