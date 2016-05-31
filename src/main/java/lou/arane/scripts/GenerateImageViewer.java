package lou.arane.scripts;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SIBLINGS;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import lou.arane.util.Check;
import lou.arane.util.IO;
import lou.arane.util.Log;
import lou.arane.util.Util;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

/**
 * Generate an html file to view images
 *
 * @author LOU
 */
public class GenerateImageViewer {

    /** Stand-alone script to generate html viewers*/
    public static void main(String[] args) throws Exception {
        Path mangaDir = Util.userHomeDir().resolve(Paths.get("MyDocs", "Comics"));
        Files.walkFileTree(mangaDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException
            {
                if (dir.getFileName().toString().equals("images")) {
                    Object title = dir.getParent().getFileName();
                    new GenerateImageViewer(dir).setTitle(title).run();
                    return SKIP_SIBLINGS;
                } else {
                    return CONTINUE;
                }
            }
        });
    }

    private final Path imagesDir;

    private final Path indexFile;

    private String title;

    /**
     * Generate an html index file to view images in a given directory. The
     * images can be in the directory or direct sub-directories.
     */
    public GenerateImageViewer(Path imagesDir) {
        Check.notNull(imagesDir, "Null directory");
        Check.require(Files.isDirectory(imagesDir), imagesDir + " must be a directory.");
        this.imagesDir = imagesDir;
        indexFile = imagesDir.resolveSibling("index.html");
        title = imagesDir.getFileName().toString();
    }

    /** Set title of the generated html file; should be the story's name */
    public GenerateImageViewer setTitle(Object title) {
        this.title = String.valueOf(title);
        return this;
    }

    public void run() {
        generateChapterIndex();
    }

    /** Generate an index file to read the story */
    private void generateChapterIndex() {
        Story story = new Story(imagesDir);
        story.makeRelativeTo(imagesDir.getParent());
        story.name = title;
        generateStoryIndex(story);
    }

    private void generateStoryIndex(Story storyModel) {
        STGroup templates = new STGroupFile("StoryHtml.stg");
        templates.encoding = IO.defaultEncoding();
        ST template = templates.getInstanceOf("story");
        template.add("story", storyModel);
        String story = template.render();
        tryWrite(story);
    }

	private void tryWrite(String story) {
		try {
        	IO.write(story, indexFile);
        } catch (Exception e) {
        	Log.error("Failed to copy to " + indexFile);
        	throw new AssertionError(e);
		}
	}

}
