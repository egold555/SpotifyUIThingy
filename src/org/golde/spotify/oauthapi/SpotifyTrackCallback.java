package org.golde.spotify.oauthapi;

import java.io.IOException;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;

public interface SpotifyTrackCallback {

	public void refreshNonPlayingSongData();
	public void setCurrentLength(CurrentlyPlayingContext currentlyPlayingContext);
	public void refreshSongData(CurrentlyPlayingContext currentlyPlayingContext) throws SpotifyWebApiException, IOException;
	
}
