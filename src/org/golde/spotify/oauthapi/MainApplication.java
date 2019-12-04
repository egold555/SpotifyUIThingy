package org.golde.spotify.oauthapi;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;

import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainApplication extends Application {

	private static Text songName = new Text();
	private static Text songArtists = new Text();
	private static Text songAlbum = new Text();
	private static Text songProgress = new Text();
	private static Text songLength = new Text();
	private static ImageView songImage = new ImageView();
	private static int WINDOW_WIDTH;
	private static int WINDOW_HEIGHT;
	
	private static SpotifyOAuthThingy spotify;
	
	private static Button forwardButton = new Button(">>");
	private static Button backButton = new Button("<<");
	private static Button pauseButton = new Button("Pause");
	private static Button playButton = new Button("Play");

	public static void main(String[] args) throws Exception {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		WINDOW_WIDTH = gd.getDisplayMode().getWidth();
		WINDOW_HEIGHT = gd.getDisplayMode().getHeight();
		
		spotify = new SpotifyOAuthThingy(
				Constants.CLIENT_ID, 
				Constants.CLIENT_SECRET,
				50400
				);

		spotify.authorize(
				SpotifyPermissions.USER_READ_CURRENTLY_PLAYING, 
				SpotifyPermissions.USER_READ_PLAYBACK_STATE, 
				SpotifyPermissions.USER_MODIFY_PLAYBACK_STATE
				);
		
		launch(args);
	}

	@Override
	public void start(Stage stg) throws Exception {

		
		//callbacks
		spotify.initCallbackThread(new SpotifyTrackCallback() {
			
			@Override
			public void setCurrentLength(CurrentlyPlayingContext currentlyPlayingContext) {
				MainApplication.setCurrentLengthUI(currentlyPlayingContext);
			}
			
			@Override
			public void refreshSongData(CurrentlyPlayingContext currentlyPlayingContext) throws SpotifyWebApiException, IOException {
				MainApplication.refreshSongDataUI(currentlyPlayingContext);
			}
			
			@Override
			public void refreshNonPlayingSongData() {
				MainApplication.refreshNonPlayingSongDataUI();
			}
		});
		
		//wait for latch
		spotify.getCountdownLatch().await();
		
		//Set up the shitty UI. 
		BorderPane root = new BorderPane();
		
		VBox image = new VBox(songImage);
		image.setPrefSize(WINDOW_WIDTH / 6, WINDOW_HEIGHT / 10);
		image.setAlignment(Pos.CENTER);
		songImage.setPreserveRatio(true);
		songImage.fitWidthProperty().bind(image.widthProperty());
		songImage.fitHeightProperty().bind(image.heightProperty());
		
		VBox songData = new VBox(songName, songArtists, songAlbum, songProgress);
		songData.setPadding(new Insets(0, 0, 0, 0));
		songData.setAlignment(Pos.CENTER_LEFT);
		songData.setPrefSize(WINDOW_WIDTH / 4, WINDOW_HEIGHT / 10);
		
		VBox buttonData = new VBox(forwardButton, backButton, pauseButton, playButton);
		buttonData.setPadding(new Insets(0, 0, 0, 0));
		buttonData.setAlignment(Pos.CENTER_LEFT);
		buttonData.setPrefSize(WINDOW_WIDTH / 8, WINDOW_HEIGHT / 10);
		
		//Button actions
		forwardButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent event) {
		    	spotify.getSpotifyApi().skipUsersPlaybackToNextTrack().build().executeAsync();
		    }
		});
		
		backButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent event) {
		    	spotify.getSpotifyApi().skipUsersPlaybackToPreviousTrack().build().executeAsync();
		    }
		});
		
		pauseButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent event) {
		    	spotify.getSpotifyApi().pauseUsersPlayback().build().executeAsync();
		    }
		});
		
		playButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent event) {
		    	spotify.getSpotifyApi().startResumeUsersPlayback().build().executeAsync();
		    }
		});
		
		HBox trackInfo = new HBox(image, songData, buttonData);
		songData.setPrefSize(WINDOW_WIDTH / 6, WINDOW_HEIGHT / 10);
		root.setCenter(trackInfo);
		
		//Scene
		Scene scene = new Scene(root, WINDOW_WIDTH / 4, WINDOW_HEIGHT / 10);
		stg.setScene(scene);
		stg.setTitle("Spotify Test");
		stg.show();

	}

	private static void refreshSongDataUI(CurrentlyPlayingContext currentlyPlayingContext) throws SpotifyWebApiException, IOException {

		setSongNameUI(currentlyPlayingContext);
		setCurrentAlbumUI(currentlyPlayingContext);
		setCurrentArtistsUI(currentlyPlayingContext);
		setCurrentProgressUI(currentlyPlayingContext);
		setCurrentLengthUI(currentlyPlayingContext);
		setCurrentImage(currentlyPlayingContext);
		spotify.getCountdownLatch().countDown();
	}

	private static void refreshNonPlayingSongDataUI() {
		songName.setText("No song currently playing");
		songAlbum.setText("");
		songArtists.setText("");
		songProgress.setText("");
		songImage.setImage(null);
	}

	private static void setSongNameUI(CurrentlyPlayingContext currentlyPlayingContext) {
		songName.setText(currentlyPlayingContext.getItem().getName());
	}

	private static void setCurrentAlbumUI(CurrentlyPlayingContext currentlyPlayingContext) {
		songAlbum.setText(currentlyPlayingContext.getItem().getAlbum().getName());
	}

	private static void setCurrentArtistsUI(CurrentlyPlayingContext currentlyPlayingContext) {
		ArtistSimplified[] artists = currentlyPlayingContext.getItem().getArtists();
		String songArtistsFinal = "";
		if (artists.length == 1) {
			songArtistsFinal = artists[0].getName();
		} 
		else {
			//Comma seperated artists
			for (int i = 0; i < artists.length; i++) {
				if (i == 0) {
					songArtistsFinal = songArtistsFinal + artists[i].getName() + ", ";
				} 
				else if (i == artists.length - 1) {
					songArtistsFinal = " " + songArtistsFinal + artists[i].getName();
				} 
				else {
					songArtistsFinal = " " + songArtistsFinal + artists[i].getName() + ", ";
				}
			}
		}
		songArtists.setText(songArtistsFinal);
	}

	//sets the progress ui
	//TODO: Fix 01:02 time formatting
	private static void setCurrentProgressUI(CurrentlyPlayingContext currentlyPlayingContext) {
		songProgress.setText(getCurrentProgress(currentlyPlayingContext) / 60 + ":" + getCurrentProgress(currentlyPlayingContext) % 60);
	}

	private static void setCurrentLengthUI(CurrentlyPlayingContext currentlyPlayingContext) {
		songLength.setText(currentlyPlayingContext.getItem().getDurationMs().toString());

	}

	private static int getCurrentProgress(CurrentlyPlayingContext currentlyPlayingContext) {
		return currentlyPlayingContext.getProgress_ms() / 1000;
	}

	//albums can have mutiple images? 
	//Just use the first one, not sure why they have mutiple
	//probs says somewhere in the documentation but its late at night and I cant be bothered
	private static void setCurrentImage(CurrentlyPlayingContext currentlyPlayingContext) {
		Image img = new Image(currentlyPlayingContext.getItem().getAlbum().getImages()[0].getUrl());
		songImage.setImage(img);
	}

}
