package cz.filmtit.client;

import cz.filmtit.share.MediaSource;

/**
 * An interface for a class that is ready to receive a media source selected by a MediaSelector.
 * @author rur
 *
 */
public interface ReceivesSelectSource {
	
	/**
	 * Called by MediaSelector when MediaSource is selected
	 * @param selectedMediaSource
	 */
	void selectSource(MediaSource selectedMediaSource);

}
