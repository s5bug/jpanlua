# jpanlua

Java bindings to liblua using
[JEP 398: Foreign Linker API](https://openjdk.java.net/jeps/389).

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
