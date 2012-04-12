package cz.filmtit.client;

import cz.filmtit.share.Chunk;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	Chunk suggestions (Chunk chunk);
}
