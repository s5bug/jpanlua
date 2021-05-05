package tf.bug.jpanlua;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemorySegment;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class LuaKFunctionPtr implements AutoCloseable {

    private final MemorySegment segment;

    private interface Upcast {
        int run(MemoryAddress state, int status, MemoryAddress context);
    }

    private LuaKFunctionPtr(Lua l, LuaKFunction f) {
        Upcast upcasted = (internal, status, context) -> {
            LuaState state = new LuaState(l, internal);
            return f.run(state, status, context);
        };
        MethodHandle upcastRunHandle;
        try {
            upcastRunHandle = MethodHandles.lookup()
                .findVirtual(upcasted.getClass(), "run", MethodType.methodType(int.class, MemoryAddress.class, int.class, MemoryAddress.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Illegal access to own method", e);
        }
        this.segment = CLinker.getInstance().upcallStub(upcastRunHandle,
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER));
    }

    @Override
    public void close() {
        segment.close();
    }

    public MemorySegment getSegment() {
        return segment;
    }
}
