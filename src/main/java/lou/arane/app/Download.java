package lou.arane.app;

import java.util.stream.Collectors;

import lou.arane.core.cmds.CmdFirstSuccess;
import lou.arane.util.FileResource;
import lou.arane.util.ToString;
import lou.arane.util.URLResource;

/**
 * Download source url to target file.
 *
 * @author Phuc
 */
public class Download extends CmdFirstSuccess {

    /** Instantiate given source and target download */
    public Download(URLResource source, FileResource target) {
        super(source.plusAlternatives().stream()
                .map(url -> new DownloadUnit(url, target))
                .collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return ToString.of(Download.class).add(super.toString()).str();
    }
}