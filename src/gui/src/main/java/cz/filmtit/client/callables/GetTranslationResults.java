/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/
package cz.filmtit.client.callables;

import cz.filmtit.client.*;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.client.pages.TranslationWorkspace.SendChunksCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.client.*;
import cz.filmtit.share.*;
import java.util.*;

/**
 * Get the list of possible translations of the given chunk. The
 * TranslationResult instance contains zero or more translation suggestions,
 * which come from the Translation Memory and/or the Machine Translation System.
 * Shows the results in Translation Workspace on success.
 *
 * @author rur
 *
 */
public class GetTranslationResults extends Callable<List<TranslationResult>> {

    // parameters
    private List<TimedChunk> chunks;
    private SendChunksCommand command;
    private TranslationWorkspace workspace;

    private int id;
    private static int nextId = 0;

    @Override
    public String getName() {
        return "GetTranslationResults (chunks size: " + chunks.size() + ")";
    }

    @Override
    protected boolean onEachReturn(Object returned) {
        if (workspace.getStopLoading()) {
            return false;
        } else {
            workspace.removeGetTranslationsResultsCall(id);
            return true;
        }
    }

    @Override
    public void onSuccessAfterLog(List<TranslationResult> newresults) {
        if (newresults == null || newresults.isEmpty()
                || !newresults.get(0).getSourceChunk().isActive
                || newresults.size() != chunks.size()) {
            // expected suggestions for all chunks but did not get them
            // retry
            hasReturned = false;
            if (!retry()) {
                // cannot retry

                // tell workspace that these chunks won't arrive
                for (TimedChunk chunk : chunks) {
                    workspace.noResult(chunk.getChunkIndex());
                }
                // request next translations
                command.execute();
                // say error
                displayWindow("Some of the translation suggestions did not arrive. "
                        + "You can ignore this or you can try refreshing the page.");
            }
        } else {
            // got suggestions alright
            for (TranslationResult newresult : newresults) {
                workspace.showResult(newresult);
            }
            // request next translations
            command.execute();
        }
    }

    @Override
    protected void onProbablyOffline(Throwable returned) {
        // tell workspace not to expect any more results
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                // tell workspace that these chunks won't arrive
                for (TimedChunk chunk : chunks) {
                    workspace.noResult(chunk.getChunkIndex());
                }
                // tell workspace that no other chunk translations will arrive either
                command.noMoreResults();
            }
        });
        if (LocalStorageHandler.isOnline()) {
            displayWindow("Some of the translation suggestions cannot arrive "
                    + "because there is no connection to the server, "
                    + "probably because you are offline. "
                    + "You can try refreshing the page once back online.");
        } else {
            displayWindow("You went offline before some of the translation suggestions arrived. "
                    + "You can try refreshing the page once back online to get them, "
                    + "or you can just do without them.");
        }
    }

    @Override
    protected void onFinalError(String message) {
        // tell workspace that these chunks won't arrive
        for (TimedChunk chunk : chunks) {
            workspace.noResult(chunk.getChunkIndex());
        }
        // TODO: request next translations or not based on the type of error
        // request next translations
        command.execute();
        // say error
        /*displayWindow("Some of the translation suggestions did not arrive. " +
			"You can ignore this or you can try refreshing the page. " +
			"Error message: " + message);*/
    }

    /**
     * Get the list of possible translations of the given chunk. The
     * TranslationResult instance contains zero or more translation suggestions,
     * which come from the Translation Memory and/or the Machine Translation
     * System. Shows the results in Translation Workspace on success.
     */
    public GetTranslationResults(List<TimedChunk> chunks,
            SendChunksCommand command, TranslationWorkspace workspace) {
        super();

        this.chunks = chunks;
        this.command = command;
        this.workspace = workspace;

        // + 5s for each chunk
        callTimeOut += 5000 * chunks.size();
        enqueue();
    }

    @Override
    protected void call() {
        id = nextId++;
        workspace.addGetTranslationsResultsCall(id, this);
        filmTitService.getTranslationResults(Gui.getSessionID(), chunks, this);
    }

    /**
     * Stops the translation results generation.
     */
    public void stop() {
        new StopTranslationResults(chunks);
    }
}
