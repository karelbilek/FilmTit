package cz.filmtit.client.widgets;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import cz.filmtit.client.Gui;
import cz.filmtit.client.SubtitleSynchronizer;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.ChunkStringGenerator;
import cz.filmtit.share.TranslationResult;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matus Namesny
 */
public class VideoWidget extends HTML {

    SubtitleSynchronizer synchronizer;
    HTML sourceLabel;
    HTML targetLabel;
    TranslationWorkspace workspace;
    String path;
    int width;
    int height;
    private static final long WINDOWSIZE = 30000;
    private Collection<TranslationResult> currentLoaded = null;

    /**
     * Builds HTML code representing VideoWidget
     *
     * @param width width of player
     * @param height height of player
     * @return HTML code
     */
    public static String buildVideoWidgetCode(int width, int height) {

        StringBuilder s = new StringBuilder();
        s.append("<video id=\"video\" name=\"video\" class=\"video-js\" controls preload=\"auto\" ");
        s.append("width=").append(width).append("height=").append(height);
        s.append(">\n");

        s.append("<p class=\"vjs-no-js\">");
        s.append("To view this video please enable JavaScript, and consider upgrading to a web browser that");
        s.append("<a href=\"http://videojs.com/html5-video-support/\" target=\"_blank\">supports HTML5 video</a>");
        s.append("</p>");

        s.append("</video>");
        s.append("</br>");
        s.append("<input type=\"file\" id=\"file_button\" accept=\"video/*\"/>");

        return s.toString();
    }

    /**
     * Creates new Video Widget
     *
     * @param width width of player
     * @param height height of player
     * @param left HTML on the left side of player
     * @param right HTML on the right side of player
     * @param synchronizer SubtitleSynchronizer to tell the VideoWidget correct
     * subtitles for a time
     * @param workspace Workspace where the widget is loaded
     */
    protected VideoWidget(int width, int height, HTML left, HTML right, SubtitleSynchronizer synchronizer, final TranslationWorkspace workspace) {
        super(buildVideoWidgetCode(width, height));

        this.width = width;
        this.height = height;
        this.setStyleName("fixed_player");
        this.synchronizer = synchronizer;
        targetLabel = right;
        sourceLabel = left;
        this.workspace = workspace;
    }

    /**
     * Returns all chunks that are displayed at a given time.
     *
     *
     * @param subset The subset of chunks that we search in. It should be
     * already sorted (it is what we get from TreeSet)
     * @param time The given time.
     * @return TranslationResults that are displayed at a given time.
     */
    public List<TranslationResult> getCorrect(Collection<TranslationResult> subset, double time) {
        //subset should be already sorted by starting times
        List<TranslationResult> res = null;
        Long correctTime = null;

        for (TranslationResult tr : subset) {
            if (tr == null) {
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
                } else if (correctTime != start) {
                    return res;
                } else {
                    res.add(tr);
                }

            }
        }
        return res;
    }

    @Override
    protected void onLoad() {
        attachListener(this);
    }

    /**
     * Replaces the contents of HTML element, if it is actually different from
     * what we want to replace it with.
     *
     * @param elem Element where we want to put text.
     * @param what Text that we want to put there. newlines are converted to
     * &lt;br&gt;. No check for HTML is done, so beware XSS issues. (I am not
     * sure if we actually do check for XSS anywhere.)
     */
    public static void maybeSetHTML(HTML elem, String what) {
        String withBr = what.replaceAll("\n", "<br/>");
        if (!elem.getHTML().equals(withBr)) {
            elem.setHTML(withBr);
        }
    }

    /**
     * Updates GUI, when Video player is at some time. (IDE might show that it's
     * not used, but it is used by JNI)
     *
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
     * Plays window 30s around a given time. 
     * @param position A given time around which to play the window
     */
    public void maybePlayWindow(Long position) {

        long windownum = position / WINDOWSIZE;

        final double begin = ((windownum > 0) ? (windownum * WINDOWSIZE) : 1);
        final double end = ((windownum + 1) * WINDOWSIZE);

        currentLoaded = synchronizer.getTranslationResultsByTime(begin - 5000, end);

        playPart(begin, end);

    }

    /**
     * Construct a Video widget for Workspace. Should be used instead of
     * constructor which is protected
     *
     * @param playerFixedPanel Top fixed panel where the player will be
     * @param table Table of the player
     * @param playerFixedWrapper Wrapper for the fixed class
     * @param synchronizer Synchronizer, that holds the subtitles
     * @param workspace Workspace that will get changed and that created the Video
     * widget
     * @return The new Video widget
     */
    public static VideoWidget initVideoWidget(HTMLPanel playerFixedPanel, FlexTable table,
            HTMLPanel playerFixedWrapper, SubtitleSynchronizer synchronizer,
            TranslationWorkspace workspace) {

        HTMLPanel panelForVideo = Gui.getPanelForVideo();

        panelForVideo.add(playerFixedPanel);
        playerFixedPanel.setWidth("100%");
        playerFixedPanel.setHeight("250px");
        playerFixedPanel.addStyleName("fixedPlayer");
        table.addStyleName("tableMoved");

        playerFixedWrapper.setWidth("984 px");

        HTML leftLabel = new HTML("");
        leftLabel.addStyleName("subtitleDisplayedLeft");
        playerFixedWrapper.addStyleName("fixedPlayerWrapper");
        playerFixedWrapper.add(leftLabel);

        HTML rightLabel = new HTML("");
        rightLabel.addStyleName("subtitleDisplayedRight");

        VideoWidget videoPlayer = new VideoWidget(400, 225, leftLabel, rightLabel, synchronizer, workspace);
        videoPlayer.addStyleName("vlcPlayerDisplayed");
        playerFixedWrapper.add(videoPlayer);

        playerFixedWrapper.add(rightLabel);

        playerFixedPanel.add(playerFixedWrapper);
        return videoPlayer;
    }

    /**
     * Attaches listener to the button
     *
     * @param widget this widget
     */
    private native void attachListener(VideoWidget widget) /*-{
        $wnd.document.getElementById('file_button').addEventListener("change", function(){
            var file = this.files[0];   
            var video = $wnd.document.getElementById('video');
            
            if (file != null && video.canPlayType(file.type)) {            
                var fileURL = URL.createObjectURL(file);
                video.src = fileURL;
            }
        });    
        
    }-*/;

    /**
     * If player is playing then it pauses it, otherwise starts playing
     */
    private native void togglePlaying() /*-{
        var video = $wnd.document.getElementById('video');
            
        if (video != null && video.src != '') {
            if (video.paused) {
                video.play();
            } else {
                video.pause();
            }
        }        
            
    }-*/;

    /**
     * Plays part of video. Serves as proxy for @playPartNative
     *
     * @param begin start time
     * @param end end time
     */
    private void playPart(double begin, double end) {

        try {
            playPartNative(begin, end);
        } catch (Exception e) {
            Window.alert("Unexpected exception " + e);
        }
    }

    /**
     * Plays part of video and periodically updates GUI
     *
     * @param begin
     * @param end
     */
    private native void playPartNative(double begin, double end) /*-{
            
        var video = $wnd.document.getElementById('video');
                        
        if (video != null && video.src != '') {
                        
            video.currentTime = begin;
            video.play();
            
            setTimeout(function look() { 
                var it = video.currentTime;
                widget.@cz.filmtit.client.widgets.VideoWidget::updateGUI(*)(it);
                if(it>end) {                    
                    widget.@cz.filmtit.client.widgets.VideoWidget::togglePlaying()();
                } else {
                    setTimeout(look, 600);
                } 
            }, 600);                        
        }

    }-*/;

}
