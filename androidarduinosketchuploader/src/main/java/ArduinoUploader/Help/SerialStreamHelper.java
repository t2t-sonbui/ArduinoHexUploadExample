package ArduinoUploader.Help;

import android.content.Context;

import csharpstyle.StringHelper;

public class SerialStreamHelper {
    public static final <E extends ISerialPortStream> E newInstance(E responseType, Context context, String portName, int baudRate) {
        E tempVar = null;
        if (StringHelper.isNullOrEmpty(portName)) {
            return null;
        }
        try {
            Class clazz = responseType.getClass();
            tempVar = (E) clazz.getConstructor(Context.class, String.class, int.class).newInstance(context, portName, baudRate);

//			Class<?> clazz = Class.forName(className);
//			Constructor<?> ctor = clazz.getConstructor(String.class);
//			Object object = ctor.newInstance(new Object[] { ctorArgument });

//			Lưu ý rằng tên lớp phải là một tên đầy đủ, nghĩa là bao gồm cả không gian tên. Đối với các lớp lồng nhau, bạn cần sử dụng một đô la (vì đó là những gì trình biên dịch sử dụng). Ví dụ:
//			package foo;
//
//			public class Outer
//			{
//			    public static class Nested {}
//			}
//			Để có được Classđối tượng cho điều đó, bạn cần Class.forName("foo.Outer$Nested").

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            return tempVar;
        }

    }

    public static final <E extends ISerialPortStream> E newInstance(Class<E> clazz, Context context, String portName, int baudRate) {
        E tempVar = null;
        if (StringHelper.isNullOrEmpty(portName)) {
            return null;
        }
        try {
            // su dung khi can them bien dau vao de khoi tao
            tempVar = (E) clazz.getConstructor(Context.class, String.class, int.class).newInstance(context, portName, baudRate);
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            return tempVar;
        }

    }
//https://stackoverflow.com/questions/3437897/how-to-get-a-class-instance-of-generics-type-t/5684761#5684761
}
