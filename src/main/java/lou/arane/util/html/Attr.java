package lou.arane.util.html;

/** Used to model attribute */
class Attr {
    final String key;
    final String value;

    Attr(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
    	return String.format("Attr(key=%s, value=%s)", key, value);
    }
}