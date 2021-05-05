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

    private final MethodHandle newState;
    private final MethodHandle close;
    private final MethodHandle openLibs;
    private final MethodHandle loadFileX;
    private final MethodHandle pCallK;
    private final MethodHandle getTable;
    private final MethodHandle setTable;
    private final MethodHandle getTop;
    private final MethodHandle createTable;
    private final MethodHandle newUserDataUV;
    private final MethodHandle pushString;
    private final MethodHandle pushNumber;
    private final MethodHandle setField;
    private final MethodHandle setGlobal;

    public Lua(LibraryLookup liblua) {
        this.liblua = liblua;

        CLinker c = CLinker.getInstance();

        this.newState = c.downcallHandle(
            this.liblua.lookup("luaL_newstate").orElseThrow(),
            MethodType.methodType(MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER)
        );
        this.close = c.downcallHandle(
            this.liblua.lookup("lua_close").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER)
        );
        this.openLibs = c.downcallHandle(
            this.liblua.lookup("luaL_openlibs").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER)
        );
        this.loadFileX = c.downcallHandle(
            this.liblua.lookup("luaL_loadfilex").orElseThrow(),
            MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER)
        );
        this.pCallK = c.downcallHandle(
            this.liblua.lookup("lua_pcallk").orElseThrow(),
            MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, int.class, MemoryAddress.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT, CLinker.C_INT, CLinker.C_POINTER, CLinker.C_POINTER)
        );
        this.getTable = c.downcallHandle(
            this.liblua.lookup("lua_gettable").orElseThrow(),
            MethodType.methodType(int.class, MemoryAddress.class, int.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER, CLinker.C_INT)
        );
        this.setTable = c.downcallHandle(
            this.liblua.lookup("lua_settable").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class, int.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT)
        );
        this.getTop = c.downcallHandle(
            this.liblua.lookup("lua_gettop").orElseThrow(),
            MethodType.methodType(int.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_INT, CLinker.C_POINTER)
        );
        this.createTable = c.downcallHandle(
            this.liblua.lookup("lua_createtable").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class, int.class, int.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_INT)
        );
        this.newUserDataUV = c.downcallHandle(
            this.liblua.lookup("lua_newuserdatauv").orElseThrow(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class, int.class),
            FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_INT)
        );
        this.pushString = c.downcallHandle(
            this.liblua.lookup("lua_pushstring").orElseThrow(),
            MethodType.methodType(MemoryAddress.class, MemoryAddress.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_POINTER, CLinker.C_POINTER, CLinker.C_POINTER)
        );
        this.pushNumber = c.downcallHandle(
            this.liblua.lookup("lua_pushnumber").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class, double.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_DOUBLE)
        );
        this.setField = c.downcallHandle(
            this.liblua.lookup("lua_setfield").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class, int.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER)
        );
        this.setGlobal = c.downcallHandle(
            this.liblua.lookup("lua_setglobal").orElseThrow(),
            MethodType.methodType(void.class, MemoryAddress.class, MemoryAddress.class),
            FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_POINTER)
        );
    }

    public Lua(Path libraryPath) {
        this(LibraryLookup.ofPath(libraryPath));
    }

    public Lua(int version) {
        this(LibraryLookup.ofLibrary("lua" + version));
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

    // TODO should this be a boolean?
    public int doFile(LuaState state, String path) {
        int result = loadFile(state, path);
        if (result == 0) {
            return pCall(state, 0, Lua.MULTRET, 0);
        } else {
            return result;
        }
    }

    // TODO add enum for Lua type
    public int getTable(LuaState state, int index) {
        MemoryAddress internal = state.internal();
        try {
            return (int) this.getTable.invokeExact(internal, index);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void setTable(LuaState state, int index) {
        MemoryAddress internal = state.internal();
        try {
            this.setTable.invokeExact(internal, index);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public int getTop(LuaState state) {
        MemoryAddress internal = state.internal();
        try {
            return (int) this.getTop.invokeExact(internal);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void createTable(LuaState state, int numSeq, int numEnt) {
        MemoryAddress internal = state.internal();
        try {
            this.createTable.invokeExact(internal, numSeq, numEnt);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void newTable(LuaState state) {
        createTable(state, 0, 0);
    }

    // TODO mark this as unsafe/externally garbage-collectable
    public MemoryAddress newUserDataUV(LuaState state, MemoryAddress size, int numUserValues) {
        MemoryAddress internal = state.internal();
        try {
            return (MemoryAddress) this.newUserDataUV.invokeExact(internal, size, numUserValues);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    // TODO mark as an external string somehow?
    public MemoryAddress pushString(LuaState state, String str) {
        MemoryAddress internal = state.internal();
        if(str == null) {
            try {
                return (MemoryAddress) this.pushString.invokeExact(internal, MemoryAddress.NULL);
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        } else {
            try (MemorySegment cstr = CLinker.toCString(str)) {
                return (MemoryAddress) this.pushString.invokeExact(internal, cstr.address());
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        }
    }

    public void pushNumber(LuaState state, double num) {
        MemoryAddress internal = state.internal();
        try {
            this.pushNumber.invokeExact(internal, num);
        } catch (Throwable throwable) {
            throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
        }
    }

    public void setField(LuaState state, int index, String fieldName) {
        MemoryAddress internal = state.internal();
        if(fieldName == null) {
            try {
                this.setField.invokeExact(internal, index, MemoryAddress.NULL);
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        } else {
            try (MemorySegment str = CLinker.toCString(fieldName)) {
                this.setField.invokeExact(internal, index, str.address());
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        }
    }

    public void setGlobal(LuaState state, String name) {
        MemoryAddress internal = state.internal();
        if(name == null) {
            try {
                this.setGlobal.invokeExact(internal, MemoryAddress.NULL);
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        } else {
            try (MemorySegment str = CLinker.toCString(name)) {
                this.setGlobal.invokeExact(internal, str.address());
            } catch (Throwable throwable) {
                throw new RuntimeException("Java Foreign Linker threw an exception", throwable);
            }
        }
    }

}
