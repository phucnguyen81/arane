package lou.arane.app;

import lou.arane.core.Cmd;
import lou.arane.util.FileResource;
import lou.arane.util.HttpResponse;
import lou.arane.util.URLResource;

/**
 * Unit of download is downloading from url to file.
 *
 * @author Phuc
 */
public class DownloadUnit implements Cmd {

    private final URLResource source;
    private final FileResource target;

    public DownloadUnit(URLResource source, FileResource target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Run only if target file does not exists
     */
    @Override
    public boolean canRun() {
        return target.notExists();
    }

    /**
     * Download source to target
     */
    @Override
    public void doRun() {
        try (HttpResponse res = source.httpGET()) {
            if (res.hasErrorStatus()) {
                throw new RuntimeException(String.format(
                        "Downloading: %s gives error status: %s", source, res));
            }
            res.copyTo(target);
        }
    }

}
