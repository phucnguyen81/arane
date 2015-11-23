package lou.arane.util.html;

import lou.arane.util.New;

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
        return New.toStringHelper(this).add("key", key).add("value", value).toString();
    }
}