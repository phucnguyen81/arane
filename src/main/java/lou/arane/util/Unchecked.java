package lou.arane.util;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Convert checked-exception to unchecked-exception.
 *
 * @author Phuc
 */
public abstract class Unchecked {

	public static void tryDo(AutoCloseable c) {
		try {
			c.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Runnable toDo(AutoCloseable c) {
		return () -> {
			try {
				c.close();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static <T> T tryGet(Callable<T> c) {
		try {
			return c.call();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> Supplier<T> toGet(Callable<T> c) {
		return () -> {
			try {
				return c.call();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

	public interface IOFunction<I, O> {
		O apply(I i) throws Exception;
	}

	public static <I,O> Function<I, O> toFun(IOFunction<I, O> io) {
		return i -> {
			try {
				return io.apply(i);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
	}

}
