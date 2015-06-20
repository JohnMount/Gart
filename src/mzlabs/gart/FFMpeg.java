package mzlabs.gart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class FFMpeg {
	// counting must start at 1
	// ffmpeg -i frames/pic%010d.png -b 9800000 mov.mpg
	// able to merge the audio with:
	// /opt/local/bin/ffmpeg -i /tmp/gmovie13578dir/frames/pic%010d.png -i /Users/johnmount/Downloads/KYOKOSHOUSE.m4a -acodec copy -sameq -intra -s hd720 /Users/johnmount/Desktop/big2.mp4
	public static void encodeFrames(final File workDir, final File movFile, final int w, final int h, final int framesPerSecond,
			final File audioFile) throws FileNotFoundException, IOException, InterruptedException {
		System.out.println("starting ffmpeg\t" + new Date());
		try {
			movFile.delete();
		} catch (Exception ex) {
		}
		final ArrayList<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(new String[] {
				"/opt/local/bin/ffmpeg",
				"-r", "" + framesPerSecond,        // frames per second
				"-i", (new File(workDir,Movie.frameDir)).getAbsolutePath() + "/" + Movie.PIC + Movie.ffNumberFormat + Movie.STILLSUFFIX,
			}));
		if(audioFile!=null) {
			command.addAll(Arrays.asList(new String[] {
				"-i", audioFile.getAbsolutePath()
			 }));
		}
		command.addAll(Arrays.asList(new String[] {
				"-r", "" + framesPerSecond,        // frames per second
				"-s", ("" + w + "x" + h),  // frame size
				 "-b", "4600k",  // bit rate
				movFile.getAbsolutePath()
			}));
		StringBuilder b = new StringBuilder();
		for(final String ci: command) {
			b.append(" " + ci);
		}
		System.out.println("encode: " + b.toString() + "\t" + new Date());
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder.directory(workDir);
		pBuilder.redirectErrorStream(true);
		pBuilder.environment().put("PATH","/sw/bin:/sw/sbin:/bin:/sbin:/usr/bin:/usr/sbin:/usr/local/bin:/usr/texbin:/usr/X11R6/bin");
		Process proc = pBuilder.start();
		Movie.waitForProc(proc);
		System.out.println("wrote '" + movFile.getAbsolutePath() + "'\t" + new Date());		
	}
}
