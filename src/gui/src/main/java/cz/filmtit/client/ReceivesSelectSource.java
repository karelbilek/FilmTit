package cz.filmtit.client;

import cz.filmtit.share.MediaSource;

public interface ReceivesSelectSource {
	
	/**
	 * Called by MediaSelector when MediaSource is selected
	 * @param selectedMediaSource
	 */
	void selectSource(MediaSource selectedMediaSource);

}
