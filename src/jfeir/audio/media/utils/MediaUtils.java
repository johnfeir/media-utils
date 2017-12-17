package jfeir.audio.media.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import com.mpatric.mp3agic.ID3v1Genres;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;

//TODO - build unit tests
//TODO - consolidate makePlaylist method
//TODO - clean up genre playlistname props

/**
 * The Class MediaUtils.
 */
class MediaUtils {
	private static final String FILE_SEP = File.separator;
	private static final String LINE_SEP = System.getProperty("line.separator");
	static final String TAG_DELIMITER = " - ";
	static final int ARTIST = 0;
	static final int TITLE = 1;

	/**
	 * Instantiates a new media utils.
	 */
	MediaUtils() {
	}

	/**
	 * Adjust tags. set album from filename set genre, year sort on artist and
	 * set track number other tag tweaks? TODO: add year patch to ja tagger -
	 * default to current year
	 *
	 * @param base
	 *            the base
	 * @param playListSuffix 
	 */
	public void processMedia(String base, String genre, String year, String playListSuffix, String playListName, boolean writeTag) {
		List<String> sourceList;
		try {
			sourceList = buildFilePathArrayList(base, "*.mp3");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		Collections.sort(sourceList,String.CASE_INSENSITIVE_ORDER);
		
//		StringBuffer playList = new StringBuffer();
//		playList.append("#EXTM3U");
//		playList.append(LINE_SEP);

//		String lastPathSegment = getLastPathSegment(base);
		int trackNum = 0;
		for (String path : sourceList) {
			try {
				trackNum++;
				File file = new File(path);
				if (!file.exists()) {
					throw new IOException("MediaUtils:adjustTags:ERROR:File not found: " + file.getAbsolutePath());
				}

				AudioFile audioFile = AudioFileIO.read(file);
				Tag tag = audioFile.getTag();
				String fileName = file.getName();
				String[] tags = fileName.split(TAG_DELIMITER);
				if (tags.length < 2) {
					throw new IOException("MediaUtils:adjustTags:ERROR:File tags not parsable from file name: " + file.getAbsolutePath());
				}
				if (tag != null && (!(tag.isEmpty()))) {
					tag.setField(FieldKey.ALBUM, tags[ARTIST]);
					tag.setField(FieldKey.ALBUM_ARTIST, tags[ARTIST]);
					tag.setField(FieldKey.GENRE, genre);
					tag.setField(FieldKey.YEAR, year);
					tag.setField(FieldKey.TRACK, getTrackNum(trackNum));
					if (writeTag) {
						System.out.println("Writing tag for:" + fileName);
						audioFile.commit();

					}
					else {
						System.out.println("Processing:" + fileName);

					}
//					addPlayListEntry(playList, tag, file, lastPathSegment);

				} else {
					throw new IOException("MediaUtils:adjustTags:ERROR:Id3v2Tag not present: " + file.getAbsolutePath());

				}

			} catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotWriteException e) {
				e.printStackTrace();

			}

		}
		System.out.println("Processed "+trackNum+ " files.");
//		String playListFilePath = getParentPath(base) + FILE_SEP + playListName + playListSuffix + ".m3u";
//		writePlayListFile(playList, playListFilePath);

	}

	private String getLastPathSegment(String base) {
		File file = new File(base);
		String parentPath = file.getParent();
		String lastPathSegment = base.substring(parentPath.length() + 1);
		return lastPathSegment;
	}

	private String getParentPath(String base) {
		File file = new File(base);
		String parentPath = file.getParent();
		return parentPath;
	}

	private void writePlayListFile(StringBuffer playList, String filePath) {
		try {
			System.out.println("MediaUtils:writePlayListFile:writing playlist file:" + filePath);
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(playList.toString());
			writer.close();
		} catch (IOException e) {
			System.out.println("MediaUtils:writePlayListFile:could not write playlist file:"+filePath);
			e.printStackTrace();
		}
	}

	private void addPlayListEntry(StringBuffer playList, Tag tag, File file, String lastPathSegment) {
		playList.append("#EXTINF:").append(getTrackLen(file));
		playList.append(",");
		playList.append(tag.getFirst(FieldKey.ARTIST));
		playList.append(TAG_DELIMITER);
		playList.append(tag.getFirst(FieldKey.TITLE));
		playList.append(LINE_SEP);
		playList.append(lastPathSegment);
		playList.append(FILE_SEP);
		playList.append(file.getName());
		playList.append(LINE_SEP);

	}

	private String getTrackLen(File file) {
		String trackLen = null;
		try {
			MP3File mp3File = (MP3File) AudioFileIO.read(file);
			MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();
			return Integer.toString(audioHeader.getTrackLength());
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
			e.printStackTrace();
		}
		return trackLen;
	}

	private String getTrackNum(int trackNum) {
		String numberAsString = String.format("%03d", trackNum);
		return numberAsString;
	}

	public static int getGenreNumber(String genre) {
		for (int i = 0; i < ID3v1Genres.GENRES.length; i++) {
			if (ID3v1Genres.GENRES[i].equals(genre)) {
				return i;
			}
		}
		return -1;// not found

	}

	private void renameAndSaveMp3(Mp3File mp3File, File file) {
		// save to tmp filename
		// delete orig file
		// rename tmp to orig
		File tmp = new File(file.getAbsolutePath() + ".tmp");
		// File bk = new File(file.getAbsolutePath()+".bk");//test
		try {
			if (tmp.exists()) {
				throw new IOException("MediaUtils:renameAndSaveMp3:ERROR:tmp file exists");
			}
			mp3File.save(tmp.getAbsolutePath());
			// file.renameTo(bk);//test
			file.delete();
			tmp.renameTo(file);

		} catch (NotSupportedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Read mp3.
	 *
	 * @param file
	 *            the file
	 */
	public void readMp3(File file) {
		Mp3Data mp3Data = new Mp3Data(file);
		mp3Data.readMp3();
	}

	/**
	 * Empty files.
	 *
	 * @param base
	 *            the base
	 */
	public void emptyFiles(String base) {
		// one time use to create empty existing files to save space
		File basePath = new File(base);
		File[] files = basePath.listFiles(mp3Filter);

		for (File file : files) {
			System.out.println("Emptying: " + file.getAbsolutePath());
			try {
				new PrintWriter(file.getAbsolutePath()).close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// List<File> fileList = new ArrayList<File>();
		// fileList.addAll(Arrays.asList(files));

	}

	/**
	 * Creates the empty files.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param targetPath
	 *            the target path
	 * @param dryRun
	 *            the dry run
	 */
	public void createEmptyFiles(String sourcePath, String targetPath, boolean dryRun) {

		List<String> sourceList = null;
		List<String> targetList = null;
		try {
			sourceList = buildFileNameArrayList(sourcePath, "*.mp3");
			targetList = buildFileNameArrayList(targetPath, "*.mp3");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		int i = 0;
		for (String fileName : sourceList) {
			if (!targetList.contains(cleanUpFileName(fileName))) {
				// create an empty file of the same name in target
				createFile(targetPath + "/" + fileName, dryRun);
				i++;

			} else {
				System.out.println("File is already present in target:" + fileName);
			}
		}
		System.out.println("Created count:" + i);

	}

	/**
	 * Creates the file.
	 *
	 * @param fullPath
	 *            the full path
	 * @param dryRun
	 *            the dry run
	 */
	private void createFile(String fullPath, boolean dryRun) {
		File f = new File(fullPath);
		if (!f.exists() && !f.isDirectory()) {
			System.out.println("Will create:" + fullPath);
			if (!dryRun) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * Clean up file name.
	 *
	 * @param fileName
	 *            the file name
	 * @return the string
	 */
	private String cleanUpFileName(String fileName) {
		String cleaned = fileName.replace("  ", " ").replace(" (1)", "");
		return cleaned;

	}

	/** The mp3 filter. */
	private FilenameFilter mp3Filter = new FilenameFilter() {
		public boolean accept(File file, String name) {
			if (name.endsWith(".mp3")) {
				return true;
			} else {
				return false;
			}
		}
	};

	/**
	 * Builds the file array list.
	 *
	 * @param base
	 *            the base
	 * @param pattern
	 *            the pattern
	 * @return the list
	 * @throws IOException 
	 */
	private List<File> buildFileList(String base, String pattern) throws IOException {
		File basePath = new File(base);
		if (!basePath.exists()) {
			throw new IOException("MediaUtils:buildFileList:ERROR:Base path does not exist: " + base);

		}
		File[] files = basePath.listFiles(mp3Filter);
		List<File> fileList = Arrays.asList(files);
		
		return fileList;

	}

	/**
	 * Builds the file array list.
	 *
	 * @param base
	 *            the base
	 * @param pattern
	 *            the pattern
	 * @return the list
	 * @throws IOException 
	 */
	private List<String> buildFileNameArrayList(String base, String pattern) throws IOException {
		File basePath = new File(base);
		if (!basePath.exists()) {
			throw new IOException("MediaUtils:buildFileNameArrayList:ERROR:Base path does not exist: " + base);

		}
		File[] files = basePath.listFiles(mp3Filter);

		List<String> fileList = new ArrayList<String>();
		for (int i = 0; i < files.length; ++i) {
			fileList.add(files[i].getName().toLowerCase());
		}

		return fileList;

	}

	private List<String> buildFilePathArrayList(String base, String pattern) throws IOException {
		File basePath = new File(base);
		if (!basePath.exists()) {
			throw new IOException("MediaUtils:buildFilePathArrayList:ERROR:Base path does not exist: " + base);

		}
		File[] files = basePath.listFiles(mp3Filter);

		List<String> fileList = new ArrayList<String>();
		for (int i = 0; i < files.length; ++i) {
			if (validFile(files[i])) {
				fileList.add(files[i].getAbsolutePath());

			}
		}

		return fileList;

	}

	private boolean validFile(File file) {
		if (file.getName().contains(" (1).mp3")) {
			return false;
		}
		return true;
	}

	/**
	 * Delete duplicates.
	 *
	 * @param sourcePath
	 *            the source path
	 * @param targetPath
	 *            the target path
	 * @param dryRun
	 *            the dry run
	 */
	public void deleteDuplicates(String sourcePath, String targetPath, boolean dryRun) {
		List<String> filesToCheck = null;
		List<String> existingFiles = null;
		try {
			filesToCheck = buildFileNameArrayList(sourcePath, "*.mp3");
			existingFiles = buildFileNameArrayList(targetPath, "*.mp3");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("Deleting duplicates from:" + sourcePath);
		System.out.println("That exist in:" + targetPath);
		int i = 0;
		for (String fileName : filesToCheck) {
			if (existingFiles.contains(cleanUpFileName(fileName))) {
				System.out.println("Will delete existing file:" + fileName);
				if (!dryRun) {
					File file = new File(sourcePath + "/" + fileName);
					file.delete();

				}
				i++;

			}
		}
		System.out.println("Duplicate count: " + i + " Total files:" + filesToCheck.size());

	}

	public void makePlaylist(String base, String playListName) {
		List<String> sourceList;
		try {
			sourceList = buildFilePathArrayList(base, "*.mp3");
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		Collections.sort(sourceList, String.CASE_INSENSITIVE_ORDER);
		StringBuffer playList = new StringBuffer();
		playList.append("#EXTM3U");
		playList.append(LINE_SEP);
		String lastPathSegment = getLastPathSegment(base);
		for (String path : sourceList) {
			try {
				File file = new File(path);
				if (!file.exists()) {
					throw new IOException("MediaUtils:adjustTags:ERROR:File not found: " + file.getAbsolutePath());
				}
				System.out.println("Processing:" + file.getName());
				AudioFile audioFile;
				audioFile = AudioFileIO.read(file);
				Tag tag = audioFile.getTag();
				addPlayListEntry(playList, tag, file, lastPathSegment);
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String playListFilePath = getParentPath(base) + FILE_SEP + playListName + ".m3u";
		writePlayListFile(playList, playListFilePath);

	}

	public void deleteInvalidFiles(String basePath) {
		List<String> filesToCheck;
		try {
			filesToCheck = buildFileNameArrayList(basePath, "*.mp3");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Deleting invalid files from:" + basePath);
		int i = 0;
		for (String fileName : filesToCheck) {
			if (fileName.contains(" (1).mp3")) {
				System.out.println("Will delete file:" + fileName);
				File file = new File(basePath + FILE_SEP + fileName);
				file.delete();
				i++;

			}
		}
		System.out.println("Deleted count: " + i + " Total files:" + filesToCheck.size());

	}

	// not tested
	public void addTrackNumToFileNames(String basePath) {
		List<File> files;
		try {
			files = buildFileList(basePath, "*.mp3");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		for (File file : files) {
			AudioFile audioFile = null;
			try {
				audioFile = AudioFileIO.read(file);
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
				e.printStackTrace();
				return;
			}
			Tag tag = audioFile.getTag();
			if (tag != null && (!(tag.isEmpty()))) {
				String trackNum = tag.getFirst(FieldKey.TRACK);
				if (trackNum != null && (!(trackNum.isEmpty()))) {
					System.out.println("Processing filename:" + file.getName());
					StringBuffer newPath = new StringBuffer();
					newPath.append(basePath);
					newPath.append(FILE_SEP);
					newPath.append(trackNum);
					newPath.append(TAG_DELIMITER);
					newPath.append(file.getName());
					File newFile = new File(newPath.toString());
					file.renameTo(newFile);
					
				}
				else {
					System.err.println("MediaUtils:addTrackNumToFileNames:ERROR:tag is empty or null");
				}
				
			}

		}		
		
	}

	public void removeTrackNumFromFileNames(String basePath) {
		// not tested
		List<File> files;
		try {
			files = buildFileList(basePath, "*.mp3");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (File file : files) {
			String[] tags = file.getName().split(TAG_DELIMITER);
			if (!(tags[0].matches("[0-9]+"))) {
				System.err.println("MediaUtils:removeTrackNumToFileNames:ERROR:First element is not a number: " + file.getAbsolutePath());
				continue;
			}
			System.out.println("Processing:" + file.getName());

			StringBuffer newPath = new StringBuffer();
			newPath.append(basePath);
			newPath.append(FILE_SEP);

			for (int i = 1; i < tags.length; i++) {
				newPath.append(tags[i]);
				if (i < tags.length - 1) {
					newPath.append(TAG_DELIMITER);

				}

			}

			File newFile = new File(newPath.toString());
			file.renameTo(newFile);

		}

	}

}
