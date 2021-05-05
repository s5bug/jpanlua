package tf.bug.jpanlua;

public class Main {

    public static void main(String[] args) {
        Lua lua = new Lua(54);
        try(LuaState l = lua.newState()) {
            l.openLibs();
            l.doFile("test.lua");
        }
    }

}
