package cz.filmtit.client.widgets;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;

//This is very ugly, but I don't really know how to deal with GWT :-(
public class VLCWidget extends HTML {

    long lastPosition=-1;
    static long WINDOWSIZE=30000; //in milliseconds

    public void maybePlayWindow(long position) {
        if (position/WINDOWSIZE != lastPosition/WINDOWSIZE){
            long windownum = position/WINDOWSIZE;
            lastPosition=position;
            playPart(windownum*WINDOWSIZE, (windownum+1)*WINDOWSIZE);
            
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

    public VLCWidget(String path, int width, int height) {
        super(buildVLCCode(path, width, height));
        this.setStyleName("fixed_player");
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
    public static native void playPartOfThis(Element el, double start, double end) /*-{
        var vlc = el.querySelector("#video");
        vlc.input.time = start;
        

        watchend = end;
        if (!vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
        setTimeout(function look() { 
            var it = vlc.input.time;

            if(it>watchend) {
                if (vlc.playlist.isPlaying) {
                    vlc.playlist.togglePause();
                }
            } else {
                setTimeout(look, 100);
            } 
        }, 100);
    }-*/;

   
    public void stopPlaying() {
        stopPlayingThis(this.getElement());
    }

    public void startPlaying() {
        startPlayingThis(this.getElement());
    }


    public void playPart(long start, long end) {
        double startD = (double)start;
        double endD = (double)end;

        playPartOfThis(this.getElement(), startD, endD);
    }
}


