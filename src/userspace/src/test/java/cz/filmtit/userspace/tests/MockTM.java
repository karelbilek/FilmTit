package cz.filmtit.userspace.tests;


import cz.filmtit.core.model.TranslationMemory;
import cz.filmtit.core.model.storage.MediaStorage;
import cz.filmtit.share.Chunk;
import cz.filmtit.share.Language;
import cz.filmtit.share.MediaSource;
import cz.filmtit.share.TranslationPair;
import scala.Option;
import scala.collection.immutable.List;

public class MockTM implements TranslationMemory {

    @Override
    public void add(TranslationPair[] pairs) { }

    @Override
    public void reindex() { }

    @Override
    public void reset() { }

    @Override
    public List<TranslationPair> nBest(Chunk chunk, Language language, MediaSource mediaSource, int n, boolean inner) {
        return null;
    }

    @Override
    public Option<TranslationPair> firstBest(Chunk chunk, Language language, MediaSource mediaSource) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MediaStorage mediaStorage() {
        return null;
    }
}
