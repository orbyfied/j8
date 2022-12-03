package net.orbyfied.j8.expr.ast.exec;

public class EvalObject {

    // the object ID
    protected final int objectID;

    public EvalObject(int objectID) {
        this.objectID = objectID;
    }

    public int getObjectID() {
        return objectID;
    }

    // object types
    public static final byte OBJECT_TYPE_ARRAY    = 0;
    public static final byte OBJECT_TYPE_TABLE    = 1;
    public static final byte OBJECT_TYPE_LIST     = 2;
    public static final byte OBJECT_TYPE_INSTANCE = 3;

}
