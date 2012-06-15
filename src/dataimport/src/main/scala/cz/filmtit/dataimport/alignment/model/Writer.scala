package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.parsing.UnprocessedChunk

import cz.filmtit.share.TimedChunk
import cz.filmtit.core.Configuration

import java.io._

class Writer(conf:Configuration) {
    
    var lastName:Option[String]=None;
    var printWriter:PrintWriter=null;

    def write(name:String, chunk1:UnprocessedChunk, chunk2:UnprocessedChunk) {
        write(name, chunk1.getText, chunk2.getText)
    }    

    def write(name:String, chunk1:TimedChunk, chunk2:TimedChunk) {
        write(name, chunk1.getSurfaceForm, chunk2.getSurfaceForm)
    }

    def write(name:String, chunk1:String, chunk2:String) {
        if (lastName!= None && lastName!=Some(name)) {
            throw new Exception("Did not flush");
        }

        if (lastName==None) {
            printWriter = new java.io.PrintWriter(new File(conf.getDataFileName(name)));
        }

        printWriter.println(chunk1.replaceAll("\t"," ") +"\t"+ chunk2.replaceAll("\t"," "))
    }

    def flush(name:String) {
        if (lastName!= None && lastName!=Some(name)) {
            throw new Exception("Did not flush");
        }
        
        lastName=None;
        printWriter.close;
        printWriter=null;

    }

}
