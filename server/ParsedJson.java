class ParsedJson {
    public ParsedJson(String content, String name, String message, ContentType type, CursorPosition cursorPosition) {
        this.content = content;
        this.name = name;
        this.message = message;
        this.type = type;
        this.cursorPosition = cursorPosition;
    }

    public ParsedJson(String content, String name, ContentType type) {
        this.name = name;
        if (type == ContentType.CHAT) {
            this.message = content;
        }
        if (type == ContentType.TEXT) {
            this.content = content;
        }
        this.type = type;
    }

    public ParsedJson() {
        this.content = null;
        this.name = null;
        this.message = null;
        this.type = null;
        this.cursorPosition = null;
    }

    String content;
    String name;
    String message;
    ContentType type;
    CursorPosition cursorPosition;

    @Override
    public String toString() {
        String retval = "";
        retval += "ParsedJson{\n";
        retval += "\tcontent: " + content + "\n";
        retval += "\tname: " + name + "\n";
        retval += "\tmessage: " + message + "\n";
        retval += "\ttype: " + type + "\n";
        retval += "\tcursorPosition: " + cursorPosition + "\n";
        retval += "}";

        return retval;
    }
}

class CursorPosition {
    public CursorPosition(int lineNum, int columnNum) {
        this.lineNum = lineNum;
        this.columnNum = columnNum;
    }

    int lineNum;
    int columnNum;

    @Override
    public String toString() {
        String retval = "";
        retval += "CursorPosition{\n";
        retval += "\tlineNum: " + lineNum + "\n";
        retval += "\tcolumnNum: " + columnNum + "\n";
        retval += "}";
        return retval;
    }
}

enum ContentType {
    TEXT,
    CHAT,
    CURSOR,
}