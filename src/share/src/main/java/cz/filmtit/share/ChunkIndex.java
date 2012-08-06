package cz.filmtit.share;

import java.io.Serializable;
public class ChunkIndex implements com.google.gwt.user.client.rpc.IsSerializable, Serializable, Comparable<ChunkIndex> {
    int partNumber;
    int id;

    public ChunkIndex() {
        partNumber=0;
        id=0;
    }

    public ChunkIndex(Integer partNumber, Integer id) {
        if (partNumber==null) {
            partNumber = 0;
        }
        if (id == null) {
            id=0;
        }
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
    public String toString() {
    	// TODO: I would prefer the reverse order (id|partNumber) but I don't want to change it because it might confuse the others. --Ruda
        return new Integer(partNumber).toString()+" | "+ new Integer(id).toString();
    }

    @Override
    public int hashCode() {
        Integer r = partNumber*13+id*53;
        return r.hashCode();
    }

    @Override
    public int compareTo(ChunkIndex other) {
        if (other.id == id) {
            return partNumber - other.partNumber;
        } else {
            return id - other.id;
        }
    }

}
