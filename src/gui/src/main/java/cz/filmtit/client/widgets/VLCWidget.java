/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import cz.filmtit.share.*;
import cz.filmtit.client.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.event.dom.client.*;
import cz.filmtit.client.pages.TranslationWorkspace;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import com.google.gwt.event.shared.UmbrellaException;

/**
 * Class that implemepts VLC playing.
 *
 * @author Karel Bílek
 */
public class VLCWidget extends HTML {

    private long lastPosition=-10000000;
    private static long WINDOWSIZE=30000; //in milliseconds

    private Collection<TranslationResult> currentLoaded = null;

    
    private double nowPlayPartID = -1;

    /**
     * Plays window 30s around a given time. It also tries to detect whether
     * the VLC stopped playing when it shouldn't and therefore probably crashed.
     * @param position A given time around which to play the window.
     */
    public void maybePlayWindow(long position) {
        if (hidden) {
            return;
        }
        if (position/WINDOWSIZE != lastPosition/WINDOWSIZE){
            
            long windownum = position/WINDOWSIZE;
            lastPosition=position;
            
            int nonce = reloadedTimes * 1000;
            
            final double begin =( (windownum>0)? (windownum*WINDOWSIZE) : 1)+nonce;
            final double end = ((windownum+1)*WINDOWSIZE)+nonce;
           
            currentLoaded = synchronizer.getTranslationResultsByTime(begin-5000, end);
            
            String beginString = TimedChunk.millisToTime((long)(begin), false).toString();
            String endString = TimedChunk.millisToTime((long)(end), false).toString();
            startLabel.setText(beginString);
            endLabel.setText(endString);

            playPart(begin, end);
            
            final double playPartID = java.lang.Math.random();
            nowPlayPartID = playPartID;
            
            //getting around the VLC bug when it randomly stops 
            new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    if (getStatus()==begin && nowPlayPartID == playPartID) {
                        if (reloadedTimes <= 5) {
                            Window.alert("I want to reload stuff.");
                            //workspace.reloadPlayer();
                        } else {
                            Window.alert("There was an unexpected error with player.\nTry to open the subtitle again.");
                        }
                    }
                } 
            }.schedule(20000); 
        
        }
    }


    /**
     * Builds the &lt;embed&gt; VLC code.
     * @param path Path of file on user's disk.
     * @param width Width of player
     * @param height Height of player
     * @return HTML code
     */
    public static String buildVLCCode(String path, int width, int height) {
        StringBuilder s = new StringBuilder();
        s.append("<embed type=\"application/x-vlc-plugin\" ");
        s.append("name=\"video\" ");
        s.append("id = \"video\" ");
        s.append("autoplay=\"yes\" loop=\"no\" ");
        s.append("width=\""+width+"\" ");
        s.append("height=\""+height+"\" ");
        s.append("target=\"file://");
        if (!path.startsWith("/")) {
            s.append("/");//windows paths don't begin with "/" but VLC needs that
        }
        s.append(path);
        s.append("\" />");
        return s.toString();
    }

    /**
     * Returns VLC widget with higher nonce. Used only when restarting VLC.
     * @return VLC widget with higher nonce
     */
    public VLCWidget higherNonce() {
        return new VLCWidget(path, width, height,  sourceLabel,targetLabel, synchronizer, startLabel,
                             endLabel, stopA, replayA, closeA, reloadedTimes+1,workspace);
    }

    InlineLabel startLabel;
    InlineLabel endLabel;
 
    SubtitleSynchronizer synchronizer;
    HTML sourceLabel;
    HTML targetLabel;
    public int reloadedTimes;
    TranslationWorkspace workspace;
    String path;
    int width;
    int height;
    Anchor stopA;
    Anchor replayA;
    Anchor closeA;
    
    boolean hidden=false;

    /**
     * Hides the VLC player. It doesn't actually hide anything, it is only
     * used to tell VLC that it is hidden, so javascript don't get errors.
     */
    public void setHiddenTrue() {
        hidden = true;
    }


    /**
     * Creates a new VLC widget.
     * @param path Path of file on the disk.
     * @param width Width of the VLC player.
     * @param height Height of the VLC Player.
     * @param left HTML on the left side of player.
     * @param right HTML on the right side of player.
     * @param synchronizer SubtitleSynchronizer to tell the VLCWidget the right subtitles around time
     * @param startLabel Where to write start time.
     * @param endLabel Where to write end time.
     * @param stopA Link for stopping the player.
     * @param replayA Link for replaying the current window
     * @param closeA Link for closing the player.
     * @param reloadedTimes How many times has VLC been reloaded already?
     * @param workspace Workspace where the widget is loaded
     */
    protected VLCWidget(String path, int width, int height, HTML left, HTML right, SubtitleSynchronizer synchronizer,
                        InlineLabel startLabel, InlineLabel endLabel, Anchor stopA, Anchor replayA, Anchor closeA,
                        int reloadedTimes, final TranslationWorkspace workspace) {
        super(buildVLCCode(path, width, height));
        this.path = path;
        this.width = width;
        this.height = height;
        this.stopA = stopA;
        this.replayA = replayA;
        this.setStyleName("fixed_player");
        this.synchronizer = synchronizer;
        targetLabel = right;
        sourceLabel = left;
        this.startLabel = startLabel;
        this.endLabel = endLabel;
        this.reloadedTimes=reloadedTimes;
        this.workspace = workspace;

        stopA.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                nowPlayPartID=-1;
                togglePlaying();    
            }
        });
        replayA.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stopPlaying();
                playPart(currentStart, currentEnd);

            }
        });
        
        closeA.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if (workspace != null) {
                    workspace.turnOffPlayer();
            	}

            }
        });

       


    }

    /**
     * Replaces the contents of HTML element, if it is actually different from what we want to replace it with.
     * @param elem Element where we want to put text.
     * @param what Text that we want to put there. newlines are converted to &lt;br&gt;. No check for HTML
     *             is done, so beware XSS issues. (I am not sure if we actually do check for XSS anywhere.)
     */
    public static void maybeSetHTML(HTML elem, String what) {
        String withBr =what.replaceAll("\n", "<br/>");
        if (!elem.getHTML().equals(withBr)) {
            elem.setHTML(withBr);
        }
    }

    /**
     * Updates GUI, when VLC player is at some time. (IDE might show that it's not used, but it is used by JNI)
     * @param time What is the current time?
     */
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

    /**
     * Returns all chunks that are displayed at a given time.
     * @param subset The subset of chunks that we search in. It should be already sorted
     *               (it is what we get from TreeSet)
     * @param time The given time.
     * @return TranslationResults that are displayed at a given time.
     */
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

    /**
     * Plays a very short 10 millisecond long part of video.
     * It is here just to prevent VLC from displaying ugly "loading video" screen.
     */
    @Override
    protected void onLoad() {
        playPart(1,10);
    }

    //====================JSNI code====================
    //I found out that comments inside JSNI can cause troubles. So I am not doing it.
    private static native void togglePlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if (vlc!==null && vlc.playlist!==undefined) {
            vlc.playlist.togglePause();
        }
    }-*/;

    private static native void startPlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if ((vlc!==null) && vlc.playlist!==undefined && !vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
    }-*/;

    private static native double getStatusOfThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if ((vlc!==null) && vlc.input) {
        	return vlc.input.time;
    	}
    	else {
    		return -1;
    	}
    }-*/;

    private static native void stopPlayingThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if ((vlc!==null) && vlc.playlist!==undefined && vlc.playlist.isPlaying) {
            vlc.playlist.togglePause();
        }
    }-*/;


    /**
     * Plays a part of video and then periodically checks if the end has not been stepped over.
     * If it has, it stops the video.
     * It also periodically updates the gui.
     * @param el Element where the embedd should be.
     * @param start Start of the part.
     * @param end End of the part.
     * @param widget This widget.
     */
    private static native void playPartOfThis(Element el, double start, double end, VLCWidget widget) /*-{
        var vlc = el.querySelector("#video");
        
        if (vlc!==null && vlc.playlist!==undefined) {
            vlc.input.time = start;

            watchend = end;
            watchstart = start;
            
            if (!vlc.playlist.isPlaying) {
                vlc.playlist.togglePause();
            }
            setTimeout(function look() { 
                if (vlc!==null && vlc.playlist!==undefined) {
                    var it = vlc.input.time;
                    widget.@cz.filmtit.client.widgets.VLCWidget::updateGUI(D)(it);
                    
                    var currentStart = widget.@cz.filmtit.client.widgets.VLCWidget::currentStart;
                    var currentEnd = widget.@cz.filmtit.client.widgets.VLCWidget::currentEnd;
                    if (currentStart==watchstart && currentEnd == watchend) {

                        if(it>watchend) {
                    
                            if (vlc.playlist.isPlaying) {
                                vlc.playlist.togglePause();
                            }
                        } else {
                            setTimeout(look, 600);
                        } 
                    }
                }
            }, 600);
        }
    }-*/;
    //====================END OF JSNI code====================


    /**
     * Tells the current time of VLC player.
     * @return The current time of VLC player.
     */
    private double getStatus() {
        return getStatusOfThis(this.getElement());
    }

    private void stopPlaying() {
        if (hidden) {
            return;
        }
        stopPlayingThis(this.getElement());
    }

    private void startPlaying() {
        if (hidden) {
            return;
        }
        startPlayingThis(this.getElement());
    }

    private void togglePlaying() {
        if (hidden) {
            return;
        }
        togglePlayingThis(this.getElement());
    }

    //This is better to make public because I don't trust JSNI.
    /**
     * Current start of playing movie window.
     * Used in JSNI, that's the reason why it's public, shouldn't be changed.
     */
    public double currentStart = 0;

    /**
     * Current end of playing movie window.
     * Used in JSNI, that's the reason why it's public, shouldn't be changed.
     */
    public double currentEnd = 0;
    

    private void playPart(double start, double end) {
        if (hidden) {
            return;
        }
        double startD = start;
        double endD = end;
        currentStart = startD;
        currentEnd = endD;
        try {
            playPartOfThis(this.getElement(), startD, endD, this);
        } catch (Exception e) {
            Window.alert("Unexpected exception "+e);
        }
    }


    /**
     * Construct a VLC widget for Workspace. Should be used instead of constructor which is protected.
     * @param path Path of file.
     * @param playerFixedPanel Top fixed panel where the player will be.
     * @param table Table of the player.
     * @param fixedWrapper Wrapper for the fixed class.
     * @param synchronizer Synchronizer, that holds the subtitles.
     * @param workspace Workspace that will get changed and that created the VLC widget.
     * @return The new VLC widget.
     */
    public static VLCWidget initVLCWidget(String path, HTMLPanel playerFixedPanel, FlexTable table,
                                          HTMLPanel fixedWrapper, SubtitleSynchronizer synchronizer,
                                          TranslationWorkspace workspace) {
        HTMLPanel panelForVLC = Gui.getPanelForVLC();
        
        
        panelForVLC.add(playerFixedPanel);
        playerFixedPanel.setWidth("100%");
        playerFixedPanel.setHeight("250px");
        playerFixedPanel.addStyleName("fixedPlayer");
        table.addStyleName("tableMoved");
        
        fixedWrapper.setWidth("984 px");


        HTML leftLabel = new HTML("");
        leftLabel.addStyleName("subtitleDisplayedLeft");
        fixedWrapper.addStyleName("fixedPlayerWrapper");
        fixedWrapper.add(leftLabel);


        HTML rightLabel = new HTML("");
        rightLabel.addStyleName("subtitleDisplayedRight");
        
        InlineLabel fromLabel = new InlineLabel("00:00:00");
        InlineLabel toLabel = new InlineLabel("00:00:30");
        Anchor pauseA = new Anchor("[pause]");
        Anchor replayA = new Anchor("[replay]");
        Anchor closeA = new Anchor("[close player]");

        VLCWidget vlcPlayer = new VLCWidget(path, 400, 225, leftLabel, rightLabel, synchronizer, fromLabel, toLabel, pauseA, replayA, closeA, 0, workspace);
        vlcPlayer.addStyleName("vlcPlayerDisplayed"); 
        fixedWrapper.add(vlcPlayer);

        fixedWrapper.add(rightLabel);
       
        HTMLPanel playerStatusPanel = new HTMLPanel("");
        playerStatusPanel.add(new InlineLabel("currently playing from "));
        playerStatusPanel.add(fromLabel);
        playerStatusPanel.add(new InlineLabel(" to "));
        playerStatusPanel.add(toLabel);

        playerStatusPanel.add(new InlineLabel(" "));
        playerStatusPanel.add(pauseA);
        playerStatusPanel.add(new InlineLabel(" "));
        playerStatusPanel.add(replayA);
        playerStatusPanel.add(new InlineLabel(" "));
        playerStatusPanel.add(closeA);

        fixedWrapper.add(playerStatusPanel);
        playerStatusPanel.addStyleName("statusPanel");
        
        playerFixedPanel.add(fixedWrapper);
        return vlcPlayer;
    }
    
    
}


