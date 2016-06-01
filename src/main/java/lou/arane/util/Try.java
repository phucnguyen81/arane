package lou.arane.util;

/**
 * Convert methods with checked-exception to methods with unchecked-exceptions.
 *
 * @author Phuc
 */
public abstract class Try {

	public interface TryDo {
		void tryDo() throws Exception;
	}

	public static void toDo(TryDo t) {
		try {
			t.tryDo();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public interface TryGet<T> {
		T tryGet() throws Exception;
	}

	public static <T> T toGet(TryGet<T> t) {
		try {
			return t.tryGet();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
