package cz.filmtit.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import cz.filmtit.share.TranslationResult;

@RemoteServiceRelativePath("filmtit")
public interface FilmTitService extends RemoteService {
	TranslationResult suggestions (TranslationResult chunk);
}
