package cz.filmtit.dataimport.alignment.model

import cz.filmtit.share.parsing.UnprocessedChunk

import cz.filmtit.share.TimedChunk
import cz.filmtit.core.Configuration

import java.io._

class Writer(conf:Configuration) {
    
    var lastName:Option[String]=None;
    var printWriter:PrintWriter=null;

    def quickClean(what:String):String ={
        return what.replaceAll("\\s*\\|\\s*"," ").replaceAll("(^|\\|)\\s*-\\s*","").replaceAll("<[>]*>","") 
    }

    def write(name:String, chunk1:UnprocessedChunk, chunk2:UnprocessedChunk) {
        //in the case of different sentence split, I still want to have the chunks there
        
        write(name, 
            quickClean(chunk1.getText),
            quickClean(chunk2.getText));
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
        lastName = Some(name)
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
