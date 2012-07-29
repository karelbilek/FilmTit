package cz.filmtit.share;

import java.io.Serializable;
public class ChunkIndex implements com.google.gwt.user.client.rpc.IsSerializable, Serializable {
    int partNumber;
    int id;

    public ChunkIndex() {
        partNumber=0;
        id=0;
    }

    public ChunkIndex(int partNumber, int id) {
        this.partNumber=partNumber;
        this.id=id;
    }
   
    public ChunkIndex(TimedChunk tc) {
        this.partNumber = tc.getPartNumber();
        this.id = tc.getId();;
    }

    @Override
    public boolean equals(Object o) {
        if (o==null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }

        ChunkIndex pol = (ChunkIndex) o;
        return (pol.partNumber==partNumber && pol.id == id);

    }

    @Override
    public int hashCode() {
        Integer r = partNumber*13+id*53;
        return r.hashCode();
    }

}
