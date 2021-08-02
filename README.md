# jpanlua

Java bindings to liblua using
[JEP 389: Foreign Linker API](https://openjdk.java.net/jeps/389).

Requires JDK 16 or above.

Requires `-Dforeign.restricted=permit` JRE flag.

Exports package `tf.bug.jpanlua` under module `tf.bug.jpanlua`.

## usage

### running a file

```c
lua_State *L = luaL_newstate();
luaL_openlibs(L);
luaL_dofile(L, "script.lua");
lua_close(L);
```

```java
// `new Lua(54)` searches for Lua 5.4.
// - On Windows, this means lua54.dll
// - On Linux, this means liblua54.so
//
// On other systems, implementation is dependent on either
// - The dlfcn.h implementation
// - The JVM
Lua lua = new Lua(54);

// LuaState implements AutoClosable
try(LuaState state = lua.newState()) {
    state.openLibs();
    state.doFile("script.lua");
}
```

## assumptions

Below is a list of assumptions put in place due to either JVM limitations or
Lua's default configuration. In other words, support is only planned for
the intersection of platforms that both mainline Lua is already built to and
official versions of JDKs are built to.

- `sizeof(intptr_t) == sizeof(size_t) == sizeof(void*)`
- `LUA_FLOAT_TYPE == double`
