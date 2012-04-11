package cz.filmtit.server;

import cz.filmtit.client.FilmTitService;
import cz.filmtit.share.Chunk;
import cz.filmtit.userspace.*;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class FilmTitServiceImpl extends RemoteServiceServlet implements
		FilmTitService {

	public Chunk suggestions(Chunk chunk) {
		
		ChunkUS uschunk = new ChunkUS(chunk);
				
		if(uschunk.getText().equals("hi")) {
			uschunk.setUserTranslation("ahoj");
		} else if (chunk.text.equals("bye")) {
			uschunk.setUserTranslation("čau");
		} else if (chunk.text.equals("platypus")) {
			uschunk.setUserTranslation("ptakopysk");
		} else {
			uschunk.setUserTranslation("no translation");
		}

		return uschunk.getChunk();
	}
	
}
