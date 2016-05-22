package lou.arane.util;

/**
 * Convert methods with checked-exception to methods with unchecked-exceptions.
 * 
 * @author Phuc
 */
public class Try {
	
	public interface ToDo {
		void todo() throws Exception;
	}
	
	public static void toDo(ToDo todo) {
		try {
			todo.todo();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public interface ToGet<T> {
		T toget() throws Exception;
	}
	
	public static <T> T toGet(ToGet<T> toget) {
		try {
			return toget.toget();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
