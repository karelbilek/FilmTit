package cz.filmtit.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import cz.filmtit.share.*;
import cz.filmtit.client.*;
import com.google.gwt.user.client.ui.*;
import cz.filmtit.client.pages.TranslationWorkspace;
import java.util.List;
import java.util.LinkedList;
import java.util.Collection;

public class VLCWidget extends HTML {


    long lastPosition=-10000000;
    static long WINDOWSIZE=30000; //in milliseconds

    Collection<TranslationResult> currentLoaded = null;

    double begin = 1;
    double end = WINDOWSIZE;

    public void maybePlayWindow(long position) {
        if (hidden) {
            return;
        }
        if (position/WINDOWSIZE != lastPosition/WINDOWSIZE){
            
            long windownum = position/WINDOWSIZE;
            lastPosition=position;
            
            
            begin = windownum*WINDOWSIZE;
            end = (windownum+1)*WINDOWSIZE;
            if (begin==0){
                begin=1;
            }
            int nonce = reloadedTimes * 1000;
            begin += nonce;
            end += nonce;
            currentLoaded = synchronizer.getTranslationResultsByTime(begin-5000, end);
            
            String beginString = TimedChunk.millisToTime((long)(begin), false).toString();
            String endString = TimedChunk.millisToTime((long)(end), false).toString();
            startLabel.setText(beginString);
            endLabel.setText(endString);

            stopped=false;
            playPart(begin, end);
            
            //getting around the VLC bug when it randomly stops 
            new com.google.gwt.user.client.Timer() { 
                @Override
                public void run() { 
                    if ((!stopped) && getStatus()==begin) {
                        if (reloadedTimes <= 5) {
                            workspace.reloadPlayer();
                        } else {
                            Window.alert("There was an unexpected error with player.\nTry to open the subtitle again.");
                        }
                    }
                } 
            }.schedule(2000); 
        
        }
    }

    boolean stopped = true;
    
   
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
    public void hide() {
        hidden = true;
    }




    @SuppressWarnings("deprecation")
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
        stopA.addClickListener(new ClickListener() {
            @Override
            public void onClick(Widget sender) {
                stopped=true;
                togglePlaying();    
            }
        });
        replayA.addClickListener(new ClickListener() {
            @Override
            public void onClick(Widget sender) {
                stopPlaying();
                playPart(begin, end);

            }
        });
        
        closeA.addClickListener(new ClickListener() {
            @Override
            public void onClick(Widget sender) {
            	if (workspace != null) {
                    workspace.turnOffPlayer();
            	}

            }
        });

       


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

    public static native double getStatusOfThis(Element el) /*-{
        var vlc = el.querySelector("#video");
        if (vlc && vlc.input) {
        	return vlc.input.time;
    	}
    	else {
    		return -1;
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

    public double getStatus() {
        return getStatusOfThis(this.getElement());
    }
    
    public void stopPlaying() {
        if (hidden) {
            return;
        }
        stopPlayingThis(this.getElement());
    }

    public void startPlaying() {
        if (hidden) {
            return;
        }
        startPlayingThis(this.getElement());
    }
    
    public void togglePlaying() {
        if (hidden) {
            return;
        }
        togglePlayingThis(this.getElement());
    }


    public void playPart(double start, double end) {
        if (hidden) {
            return;
        }
        double startD = start;
        double endD = end;
        try {
            playPartOfThis(this.getElement(), startD, endD, this);
        } catch (Exception e) {
            
            //Window.alert("NEEEEEEEE");
        }
    }
    
    
    
    public static VLCWidget initVLCWidget(String path, HTMLPanel playerFixedPanel, FlexTable table, HTMLPanel fixedWrapper, SubtitleSynchronizer synchronizer, TranslationWorkspace workspace) {
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
        
        InlineLabel fromLabel = new InlineLabel("0:0:0");
        InlineLabel toLabel = new InlineLabel("0:0:30");
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


