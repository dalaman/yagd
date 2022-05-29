import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MessageParser {
    public static void main(String[] args) {
        // Token[] hoge = generateTokenList("    {      \"aa\\\"   a\"      :   \n        \"aaaaaa\"
        // }"); for (Token aaa : hoge) { System.out.println(aaa);
        // }
        // HashMap<String, Object> parsed = generateJsonPropertyTree(hoge);
        // System.out.println("parsed: " + parsed);
        // System.out.println("");

        System.out.println("given json:");
        System.out.println(args[0]);
        Token[] hoge = generateTokenList(args[0]);
        for (Token aaa : hoge) {
            System.out.println(aaa);
        }
        HashMap<String, Object> parsed = generateJsonPropertyTree(hoge);
        System.out.println("parsed: " + parsed);
        System.out.println("");

        MessageType fuga = extractMessageTypeFromJson(args[0]);
        System.out.println(fuga);
        System.out.println("");
    }

    // public static ParsedJson getParsedMessage(String rawJson) {
    //     Token[] tokenList = generateTokenList(rawJson);
    //     HashMap<String, Object> jsonMap = generateJsonPropertyTree(tokenList);
    //     HashMap<String, Object> header = (HashMap<String, Object>) jsonMap.get("header");
    //     switch (header.get("type").toString()) {
    //         case "TEXT":
    //             return new ParsedJson(jsonMap.get("content").toString(),
    //             header.get("name").toString(), ContentType.TEXT);
    //         case "CHAT":
    //             return new ParsedJson(jsonMap.get("content").toString(),
    //             header.get("name").toString(), ContentType.CHAT);
    //     }
    //     return new ParsedJson();
    // }

    // parse json => return get which type
    public static MessageType extractMessageTypeFromJson(String rawJson) {
        Token[] tokenList = generateTokenList(rawJson);
        HashMap<String, Object> jsonMap = generateJsonPropertyTree(tokenList);
        if (jsonMap.get("header") == null) {
            return MessageType.ERROR;
        }

        HashMap<?, ?> header = (HashMap<?, ?>) jsonMap.get("header");
        if (header == null) {
            return MessageType.ERROR;
        }

        var unsafeType = header.get("hoge");
        if (unsafeType == null) {
            return MessageType.ERROR;
        }

        String type = unsafeType.toString();

        switch (type) {
            case "TEXT":
                return MessageType.TEXT;
            case "CHAT":
                return MessageType.CHAT;
            case "CURSOR":
                return MessageType.CURSOR;
            case "EXIT":
                return MessageType.EXIT;
            default:
                return MessageType.ERROR;
        }
    }

    private static Token[] generateTokenList(String rawJson) {
        final ArrayList<Token> tmpTokenList = new ArrayList<Token>();

        String tmpContent = "";
        Boolean isInString = false;
        Boolean isInNumber = false;
        Boolean isDouble = false;

        for (int i = 0; i < rawJson.length(); i++) {
            final char focused = rawJson.charAt(i);

            if (isInString && i != 0) {
                final Boolean escaped = rawJson.charAt(i - 1) == '\\';
                if (!escaped && focused == '"') {
                    isInString = !isInString;
                    tmpTokenList.add(new Token(TokenType.string, tmpContent));
                    tmpContent = "";
                } else {
                    tmpContent = tmpContent + focused;
                }
                continue;
            }
            if (isInNumber) {
                switch (focused) {
                    case '.':
                        isDouble = true;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        tmpContent = tmpContent + focused;
                        continue;
                    default:
                        tmpTokenList.add(
                            new Token(isDouble ? TokenType.doubleNumber : TokenType.integerNumber,
                                tmpContent));
                        tmpContent = "";
                        isInNumber = false;
                        isDouble = false;
                }
            }

            switch (focused) {
                case '{':
                    tmpTokenList.add(new Token(TokenType.openedParentheses, "{"));
                    break;
                case '}':
                    tmpTokenList.add(new Token(TokenType.closedParentheses, "}"));
                    break;
                case ':':
                    tmpTokenList.add(new Token(TokenType.colon, ":"));
                    break;
                case ',':
                    tmpTokenList.add(new Token(TokenType.comma, ","));
                    break;
                case '"':
                    isInString = !isInString;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    tmpContent = tmpContent + focused;
                    isInNumber = true;
                    break;
            }
        }

        return (Token[]) tmpTokenList.toArray(new Token[] {});
    }

    private static HashMap<String, Object> generateJsonPropertyTree(Token[] tokenList) {
        HashMap<String, Object> content = new HashMap<String, Object>();

        if (tokenList.length > 1) {
            for (int i = 1; i < tokenList.length; i++) {
                Token focusedToken = tokenList[i];

                if (focusedToken.type == TokenType.string
                    || focusedToken.type == TokenType.integerNumber
                    || focusedToken.type == TokenType.doubleNumber) {
                    if (tokenList[i + 1].type == TokenType.colon) {
                        switch (tokenList[i + 2].type) {
                            case openedParentheses:
                                int startIndex = i + 2;
                                int closeIndex = findCloseParenthesis(tokenList, startIndex);
                                Token[] subTokenList =
                                    Arrays.copyOfRange(tokenList, startIndex, closeIndex + 1);
                                HashMap<String, Object> tmp =
                                    generateJsonPropertyTree(subTokenList);
                                content.put(focusedToken.content, tmp);
                                i = closeIndex;
                                break;
                            case string:
                                content.put(focusedToken.content, tokenList[i + 2].content);
                                break;
                            case integerNumber:
                                content.put(focusedToken.content,
                                    Integer.parseInt(tokenList[i + 2].content));
                                break;
                            case doubleNumber:
                                content.put(focusedToken.content,
                                    Double.parseDouble(tokenList[i + 2].content));
                                break;
                        }
                    }
                }
            }
        }

        // return new JsonProperty(content);
        return content;
    }

    private static int findCloseParenthesis(Token[] tokenList, int startIndex) {
        int nestedNum = 1;

        for (int i = startIndex + 1; i < tokenList.length; i++) {
            if (tokenList[i].type == TokenType.openedParentheses) {
                nestedNum++;
            }

            if (tokenList[i].type == TokenType.closedParentheses) {
                nestedNum--;
            }

            if (nestedNum == 0) {
                return i;
            };
        }

        return -1;
    }

    private static class Token {
        Token(TokenType _type, String _content) {
            this.type = _type;
            this.content = _content;
        }

        TokenType type;
        String content;

        @Override
        public String toString() {
            return "Token(type: " + this.type.toString() + ", content: " + this.content + ")";
        }
    }

    private enum TokenType {
        string,
        integerNumber,
        doubleNumber,
        closedParentheses,
        openedParentheses,
        colon,
        comma,
    }
}
