package net.xolt.freecam.config.load;

import java.io.Reader;
import java.io.Writer;

public interface ConfigSerializer<N> {
   N parse(Reader var1) throws Exception;

   void write(N var1, Writer var2) throws Exception;

   <T> T deserialize(N var1, Class<T> var2);

   <T> N serialize(T var1);
}
