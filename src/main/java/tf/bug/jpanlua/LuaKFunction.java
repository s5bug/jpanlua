package tf.bug.jpanlua;

import jdk.incubator.foreign.MemoryAddress;

public interface LuaKFunction {

    int run(LuaState state, int status, MemoryAddress context);

}
