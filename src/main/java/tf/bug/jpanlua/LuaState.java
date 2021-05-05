package tf.bug.jpanlua;

import jdk.incubator.foreign.MemoryAddress;

public record LuaState(Lua lua, MemoryAddress internal) implements AutoCloseable {

    @Override
    public void close() {
        this.lua.close(this);
    }

    public void openLibs() {
        this.lua.openLibs(this);
    }

    public int loadFile(String path) {
        return this.lua.loadFile(this, path);
    }

    public int pCall(int numArgs, int numResults, int messageHandler) {
        return this.lua.pCall(this, numArgs, numResults, messageHandler);
    }

    public int doFile(String path) {
        return this.lua.doFile(this, path);
    }

    public int getTable(int index) {
        return this.lua.getTable(this, index);
    }

    public void setTable(int index) {
        this.lua.setTable(this, index);
    }

    public int getTop() {
        return this.lua.getTop(this);
    }

    public void createTable(int numSeq, int numEnt) {
        this.lua.createTable(this, numSeq, numEnt);
    }

    public void newTable() {
        this.lua.newTable(this);
    }

    public MemoryAddress newUserDataUV(MemoryAddress size, int numUserValues) {
        return this.lua.newUserDataUV(this, size, numUserValues);
    }

}
