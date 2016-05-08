package lou.arane.util;

import java.io.IOException;

/** Results from downloading something */
public class DownloadResponse {
	/** anything wrong; if this is null, content should not be null */
	public IOException error;

	/** returned code */
	public Integer code;

	/** what can be retrieved
	 * ; could be incomplete or an error page
	 * ; check response code and error to see if this is what was requested */
	public byte[] content;
}