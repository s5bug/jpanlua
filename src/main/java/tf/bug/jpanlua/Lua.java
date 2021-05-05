package tf.bug.jpanlua;

import jdk.incubator.foreign.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class Lua {

    public static final int MULTRET = -1;

    private final LibraryLookup liblua;

    private MethodHandle newState;
    private MethodHandle close;
    private MethodHandle openLibs;
    private MethodHandle loadFileX;
    private MethodHandle pCallK;

    public Lua(LibraryLookup liblua) {
        this.liblua = liblua;
        this.loadSymbols();
    }

    public Lua(Path libraryPath) {
        this(LibraryLookup.ofPath(libraryPath));
    }

    public Lua(int version) {
        this(LibraryLookup.ofLibrary("lua" + version));
    }

    private void loadSymbols() {
        this.newState = CLinker.getInstance()
            .downcallHandle(
                this.liblua.lookup("luaL_newstate").orElseThrow(),
                MethodType.methodType(MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_POINTER)
            );
        this.close = CLinker.getInstance()
            .downcallHandle(
                this.liblua.lookup("lua_close").orElseThrow(),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER)
            );
        this.openLibs = CLinker.getInstance()
            .downcallHandle(
                this.liblua.lookup("luaL_openlibs").orElseThrow(),
                MethodType.methodType(void.class, MemoryAddress.class),
                FunctionDescriptor.ofVoid(CLinker.C_POINTER)
            );
        this.loadFileX = CLinker.getInstance()
            .downcallHandle(
                this.liblua.lookup("luaL_loadfilex").orElseThrow(),
                MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER)
            );
        this.pCallK = CLinker.getInstance()
            .downcallHandle(
                this.liblua.lookup("lua_pcallk").orElseThrow(),
                MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
                FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER)
            );
    }

    public LuaState newState() {
        try {
            MemoryAddress internal = (MemoryAddress) this.newState.invokeExact();
            return new LuaState(this, internal);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void close(LuaState state) {
        MemoryAddress internal = state.internal();
        try {
            this.close.invokeExact(internal);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void openLibs(LuaState state) {
        MemoryAddress internal = state.internal();
        try {
            this.openLibs.invokeExact(internal);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public int loadFileX(LuaState state, String path, String mode) {
        MemoryAddress internal = state.internal();
        if(mode == null) {
            try (MemorySegment pathStr = CLinker.toCString(path)) {
                return (int) this.loadFileX.invokeExact(internal, pathStr.address(), MemoryAddress.NULL);
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        } else {
            try (MemorySegment pathStr = CLinker.toCString(path); MemorySegment modeStr = CLinker.toCString(mode)) {
                return (int) this.loadFileX.invokeExact(internal, pathStr.address(), modeStr.address());
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        }
    }

    public int loadFile(LuaState state, String path) {
        return loadFileX(state, path, null);
    }

    // TODO: Figure out a replacement for context
    public int pCallK(LuaState state, int numArgs, int numResults, int messageHandler, MemoryAddress context, LuaKFunctionPtr k) {
        MemoryAddress internal = state.internal();
        MemoryAddress upcastRunAddress;
        if (k == null) {
            upcastRunAddress = MemoryAddress.NULL;
        } else {
            upcastRunAddress = k.getSegment().address();
        }
        try {
            return (int) this.pCallK.invokeExact(internal, numArgs, numResults, messageHandler, context, upcastRunAddress);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public int pCall(LuaState state, int numArgs, int numResults, int messageHandler) {
        // TODO: Are intptr_t and C_POINTER the same width? Is NULL guaranteed to be 0?
        return pCallK(state, numArgs, numResults, messageHandler, MemoryAddress.NULL, null);
    }

    public int doFile(LuaState state, String path) {
        int result = loadFile(state, path);
        if (result == 0) {
            return pCall(state, 0, Lua.MULTRET, 0);
        } else {
            return result;
        }
    }

}
