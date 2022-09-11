package net.orbyfied.j8.util.math.expr;

public class StringLocation {

    int startIndex;
    int endIndex;

    public StringLocation(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex   = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

}
