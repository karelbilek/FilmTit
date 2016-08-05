/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.filmtit.client.widgets;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import cz.filmtit.client.Gui;
import cz.filmtit.client.SubtitleSynchronizer;
import cz.filmtit.client.pages.TranslationWorkspace;
import cz.filmtit.share.TimedChunk;
import cz.filmtit.share.TranslationResult;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author matus
 */
public class VideoWidget extends HTML {

    SubtitleSynchronizer synchronizer;
    HTML sourceLabel;
    HTML targetLabel;
    public int reloadedTimes;
    TranslationWorkspace workspace;
    String path;
    int width;
    int height;

    public static String buildVideoWidgetCode(int width, int height) {

        StringBuilder s = new StringBuilder();
        s.append("<video id=\"video\" name=\"video\" class=\"video-js\" controls preload=\"auto\" ");
        s.append("width=").append(width).append("height=").append(height);

        /*      if (src.contains("youtube.com") || src.contains("youtu.be")) {
            s.append("data-setup='{ \"techOrder\": [\"youtube\"], \"sources\": [{ \"type\": \"video/youtube\", \"src\":").append(src).append("}], \"youtube\": { \"iv_load_policy\": 1 } }'>");
        } else if (src.endsWith("mp4")) {
            s.append(">");
            s.append("<source src=").append(src).append("type='video/mp4'>");
        } else if (src.endsWith("webm")) {
            s.append(">");
            s.append("<source src=").append(src).append("type='video/webm'>");
        }*/
        s.append(">");
        s.append("<p class=\"vjs-no-js\">");
        s.append("To view this video please enable JavaScript, and consider upgrading to a web browser that");
        s.append("<a href=\"http://videojs.com/html5-video-support/\" target=\"_blank\">supports HTML5 video</a>");
        s.append("</p>");

        s.append("</video>");
        s.append("<input type=\"file\" id=\"file_button\" accept=\"video/*\"/>");
        s.append("<input type=\"text\" id=\"yturl\" value=\"Youtube URL\"/>");
        s.append("<button type=\"button\" id=\"confirm_button\">Confirm</button>");
        s.append("<script src=\"http://vjs.zencdn.net/5.10.7/video.js\"></script>");
        /*    s.append("<script>");
        s.append("var video = videojs('my-video');\n"
                + "        var button = document.getElementById(\"confirm_button\").addEventListener(\"click\", function() {\n"
                + "            var file = document.files[0];\n"
                + "            var url = document.getElementById(\"yturl\").value;\n"
                + "            if (file != nul) {\n"
                + "                var canPlay = video.canPlayType(type);\n"
                + "                    if (canPlay) {\n"
                + "                        var fileURL = URL.createObjectURL(file);\n"
                + "                        video.src = fileURL;\n"
                + "                    }\n"
                + "            } else if (url && url != \"Youtube URL\") {\n"
                + "                video.src = url;\n"
                + "            }\n"
                + "        }); \n"
        );
        s.append("</script>");*/

        return s.toString();
    }

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
        attachListener();
        //setSource(this.getElement());

    }

    public static VideoWidget initVideoWidget(String path, HTMLPanel playerFixedPanel, FlexTable table,
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

    private static native void setSource(Element el) /*-{
        var video = el.querySelector("#video");
        el.getElementById('confirm_button').addEventListener("click", function() {
            var file = $wnd.document.files[0];
            var url = el.getElementById('yturl').value;
            if (file != nul) {
                var canPlay = video.canPlayType(type);
                    if (canPlay) {
                        var fileURL = URL.createObjectURL(file);
                        video.src = fileURL;
                    }
            } else if (url && url != "Youtube URL") {
                video.src = url;
            }
        }); 


    }-*/;

    private native void attachListener() /*-{
        $wnd.document.getElementById('file_button').addEventListener("change", function(){
            var file = this.files[0];   
            var video = $wnd.document.getElementById('video');
            
            if (file != null && video.canPlayType(file.type)) {            
                var fileURL = URL.createObjectURL(file);
                video.src = fileURL;
            }
        });    
        
        $wnd.document.getElementById('confirm_button').addEventListener("click", function(){ 
            var url = $wnd.document.getElementById('yturl').value;
            $wnd.alert(url);
        });
    }-*/;
}
