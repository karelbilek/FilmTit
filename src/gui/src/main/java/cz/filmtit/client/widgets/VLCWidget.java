package cz.filmtit.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import cz.filmtit.share.*;
import cz.filmtit.client.pages.TranslationWorkspace;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

public class VLCWidget extends HTML {

    long lastPosition=-10000000;
    static long WINDOWSIZE=30000; //in milliseconds

    Collection<TranslationResult> currentLoaded = null;

    public void maybePlayWindow(long position) {
        if (position/WINDOWSIZE != lastPosition/WINDOWSIZE){
            
            long windownum = position/WINDOWSIZE;
            lastPosition=position;
            
            
            double begin = windownum*WINDOWSIZE;
            double end = (windownum+1)*WINDOWSIZE;
            currentLoaded = workspace.getChunkIndexesFrom(begin-5000, end);
            
            playPart(begin, end);
            
        
        }
    }


    public static String buildVLCCode(String path, int width, int height) {
        StringBuilder s = new StringBuilder();
        s.append("<embed type=\"application/x-vlc-plugin\" ");
        s.append("name=\"video\" ");
        s.append("id = \"video\" ");
        s.append("autoplay=\"yes\" loop=\"no\" ");
        s.append("width=\""+width+"\" ");
        s.append("height=\""+height+"\" ");
        s.append("target=\"file://");
        s.append(path);
        s.append("\" />");
        return s.toString();
    }


    TranslationWorkspace workspace;
    HTML sourceLabel;
    HTML targetLabel;

    public VLCWidget(String path, int width, int height, HTML left, HTML right, TranslationWorkspace workspace) {
        super(buildVLCCode(path, width, height));
        this.setStyleName("fixed_player");
        this.workspace = workspace;
        targetLabel = right;
        sourceLabel = left;
    }

    public static void maybeSetHTML(HTML elem, String what) {
        String withBr =what.replaceAll("\n", "<br/>");
        if (!elem.getHTML().equals(withBr)) {
            elem.setHTML(withBr);
        }
    }
    
    public void updateGUI(double time) {
        try {
                //it is null at the very beginning
            if (currentLoaded != null) {
                List<TranslationResult> correct = getCorrect(currentLoaded, time);
                String source = ChunkStringGenerator.listWithSameTimeToString(correct, ChunkStringGenerator.SOURCE_SIDE);
                String target = ChunkStringGenerator.listWithSameTimeToString(correct, ChunkStringGenerator.TARGET_SIDE);
                maybeSetHTML(sourceLabel, source);
                maybeSetHTML(targetLabel, target);
            }
        } catch (Exception e) {
           StringBuilder sb = new StringBuilder();
		
    	// exception name and message
    	sb.append(e.toString());
    	sb.append('\n');
    	// exception stacktrace
		StackTraceElement[] st = e.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	    	sb.append(stackTraceElement);
	    	sb.append('\n');
		}
		
		String result = sb.toString();
	 
            
            Window.alert(result);
        
        }
    }

    public List<TranslationResult> getCorrect(Collection<TranslationResult> subset, double time) {
        //subset should be already sorted by starting times
        List<TranslationResult> res=null;
        Long correctTime = null;
        
        for (TranslationResult tr : subset) {
            if (tr==null) {
                Window.alert("!!!!! tr==null");
            }
            long start = (tr.getSourceChunk().getStartTimeLong());
            long end = (tr.getSourceChunk().getEndTimeLong());
            if (start > time) {
                return res;
            }
            if (end > time) {
                if (correctTime == null) {
                    correctTime = start;
                    res = new LinkedList();
                    res.add(tr);
                } else {
                    if (correctTime != start) {
                        return res;
                    } else {
                        res.add(tr);
                    }
                }

            }
        }
        return res;    
    }

    @Override
    protected void onLoad() {
        //this is here just to prevent VLC from displaying that ugly "loading video"
        playPart(1,10);
    }

    //I am not sure how reusable this code is/how much they can call each other.
    //I will try to discover this later, not now though
    public static native void togglePlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        vlc.playlist.togglePause();
    }-*/;

    public static native void startPlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if (!vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
    }-*/;

    public static native void stopPlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if (vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
    }-*/;

    static double watchend=0;
    public static native void playPartOfThis(Element el, double start, double end, VLCWidget widget) /*-{
        var vlc = el.querySelector("#video");
        vlc.input.time = start;

        watchend = end;
        
        if (!vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
        setTimeout(function look() { 
            var it = vlc.input.time;
            widget.@cz.filmtit.client.widgets.VLCWidget::updateGUI(D)(it);

            if(it>watchend) {
                if (vlc.playlist.isPlaying) {
                    vlc.playlist.togglePause();
                }
            } else {
                setTimeout(look, 600);
            } 
        }, 600);
    }-*/;

    
    public void stopPlaying() {
        stopPlayingThis(this.getElement());
    }

    public void startPlaying() {
        startPlayingThis(this.getElement());
    }


    public void playPart(double start, double end) {
        double startD = start;
        double endD = end;
        try {
            playPartOfThis(this.getElement(), startD, endD, this);
        } catch (Exception e) {
            Window.alert("NEEEEEEEE");
        }
    }
}


