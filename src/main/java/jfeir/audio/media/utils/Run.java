/* Steps for new media
 * 1. deleteDuplicates (dry run first)
 * 2. deleteDuplicates (actual)
 * 3. deleteInvalidFiles
 * 4. Manage large mixes
 * 5. Copy non-duplicates to existing folder and empty contents
 * 6. processMedia
 * 7. addTrackNumToFileNames
 * 8. makePlaylist
 * 
 * 
 * 
 */
package jfeir.audio.media.utils;

import java.io.File;

import com.mpatric.mp3agic.Mp3File;



public class Run {

	private static final String VOCAL_TRANCE_EXISTING_PATH = "X:/john/audio/vocal-trance-existing";
	static final boolean DRYRUN=true;
	static final boolean NO_DRYRUN=false;
	static final String AUDIO_PATH="C:/audio/stream-rip/VocalChillout-2017";
	static final String PLAYLIST_SUFFIX=" 2017";
	static final String GENRE="Vocal Chillout";
	static final String PLAYLIST_NAME="Vocal Chillout 2017";
	static final String YEAR = "2017";

		
	public static void main(String[] args) {
		
//		deleteDuplicates();
//		deleteInvalidFiles();
//		emptyFiles(); // one time run 
		processMedia(); 
		addTrackNumToFileNames();
		makePlaylist();
//		
//		removeTrackNumFromFileNames();

	}
	private static void emptyFiles() {
		MediaUtils mu = new MediaUtils();
		mu.emptyFiles(VOCAL_TRANCE_EXISTING_PATH);
		
	}

	private static void removeTrackNumFromFileNames() {
		MediaUtils mu = new MediaUtils();
		mu.removeTrackNumFromFileNames(AUDIO_PATH);
		
	}

	private static void addTrackNumToFileNames() {
		MediaUtils mu = new MediaUtils();
		mu.addTrackNumToFileNames(AUDIO_PATH);
		
	}

	private static void deleteInvalidFiles() {
		MediaUtils mu = new MediaUtils();
		mu.deleteInvalidFiles(AUDIO_PATH);
		
	}

	private static void deleteDuplicates() {
		MediaUtils mu = new MediaUtils();
		mu.deleteDuplicates(AUDIO_PATH,VOCAL_TRANCE_EXISTING_PATH, NO_DRYRUN);
//		mu.deleteDuplicates(AUDIO_PATH,"Z:/audio/2013/Vocal Lounge Set 2", DRYRUN);
//		mu.deleteDuplicates(AUDIO_PATH,"Z:/audio/2012/Lounge - Digitally Imported Premium", NO_DRYRUN);

	}

//	private static void emptyFiles() {
//		MediaUtils mu = new MediaUtils();
//		mu.emptyFiles("Z:/audio/vocal-trance-existing");
//
//	}
	
//	private static void createEmptyFiles () {
//		MediaUtils mu = new MediaUtils();
//		mu.createEmptyFiles("Z:/audio/media/2012/vt classics","Z:/audio/vocal-trance-existing", DRYRUN);
//	}

	private static void processMedia() {
		MediaUtils mu = new MediaUtils();
//		mu.processMedia("C:/audio/stream-rip/test", "Vocal Trance", true);
		// currently includes makePlaylist
		mu.processMedia(AUDIO_PATH, GENRE, YEAR, PLAYLIST_SUFFIX, PLAYLIST_NAME, true);
		
		
	}
	private static void addTrackNumToFileNames(Mp3Data mp3, File file) {
		MediaUtils mu = new MediaUtils();
		mu.addTrackNumToFileNames(AUDIO_PATH);
		
	}

	private static void makePlaylist() {
		MediaUtils mu = new MediaUtils();
		mu.makePlaylist(AUDIO_PATH, PLAYLIST_NAME);

	}
}
