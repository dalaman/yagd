const PORT = "8080"; // FIXME: port num
const rand = Math.floor((Math.random() * 10000) + 1);
const WHOAMI = "Alice" + rand; // FIXME: name

const DEFAUTL_TEXT = 'function func(){\n\tconsole.log("Howdy!");\n}';
const DEFAULT_LANG = "javascript";


// init
const contents = document.querySelector(".contents");
const webSocket = new WebSocket(`ws://127.0.0.1:${PORT}`);
const editor = monaco.editor.create(document.getElementById("editor"), {
    value: DEFAUTL_TEXT,
    language: DEFAULT_LANG,
});
let nameCursorDict = {}; // e.g. {Alice: 5:2, Bob: 10:2}


// monaco-editor
editor.onDidChangeModelContent((event) => {
    sendWrapper( /* type= */ "TEXT", /* content= */ editor.getValue());
});
editor.onDidChangeCursorPosition((event) => {
    nameCursorDict["WHOAMI"] = editor.getPosition();
    sendWrapper( /* type= */ "CURSOR", /* content= */ nameCursorDict);
});

// end monaco-editor

webSocket.onopen = () => {
    const myname = "Alice"; // FIXME: name
    sendChat(myname + " joined");
}


webSocket.onmessage = (event) => {
    console.log(event.data);
    const parsed = JSON.parse(event.data);
    if (parsed.header.type === "CHAT") receive(parsed.header.name, parsed.content);
    else if (parsed.header.type === "TEXT") updateText(parsed.content);
    else if (parsed.header.type === "CURSOR") updateCursor(parsed.content);
    else console.error("Unknown type " + parsed.type);
}


const sendWrapper = (type, content) => {
    const req = JSON.stringify({
        "header": {
            "name": WHOAMI,
            "type": type,
        },
        "content": content,
    });
    webSocket.send(req);
}


const sendChat = (message) => {
    if (!message) message = document.getElementById("chat-input").value;
    if (message === "") return;

    sendWrapper( /* type= */ "CHAT", /* content= */ message);

    moveToBottom();
    document.getElementById("chat-input").value = ""; // clear
}

const receive = (name, message) => {
    // name
    const nameElem = document.createElement("div");
    nameElem.className = "opponent_name";
    const nameContent = document.createTextNode(name);
    nameElem.appendChild(nameContent);

    // message
    const messageElem = document.createElement("div");
    messageElem.className = "message";
    const messageContent = document.createTextNode(message);
    messageElem.appendChild(messageContent);

    // unite name, message
    const nameMessageElem = document.createElement("div");
    nameMessageElem.className = "content";
    nameMessageElem.appendChild(nameElem);
    nameMessageElem.appendChild(messageElem);

    const opponentPost = document.createElement("div");
    opponentPost.className = "post";

    // unite all child elements
    opponentPost.appendChild(nameMessageElem);
    contents.appendChild(opponentPost);

    moveToBottom();
}

const updateText = (newText) => {
    if (editor.getValue() !== newText) {
        editor.getModel().setValue(newText);
        console.log("updateText");
    }
}

const updateCursor = (newNameCursorDict) => {
    // if new text
    if (!isSameDict(nameCursorDict, newNameCursorDict)) {
        // editor.getModel().setValue(text);
        // editor.setPosition(cursorPos);
        newNameCursorDict = newNameCursorDict;

        // TODO: show name...
        // for (name in nameCursorDict) {
        //     const range = {
        //         startLineNumber: nameCursorDict[name].lineNumber,
        //         startColumn: nameCursorDict[name].column,
        //         endLineNumber: nameCursorDict[name].lineNumber + 1,
        //         endColumn: nameCursorDict[name].column,
        //     };
        //
        //     const deco = {
        //         range: range,
        //         options: {
        //             className: "cursor",
        //             hoverMessage: {
        //                 value: name,
        //             }
        //         }
        //     };
        //
        //     editor.deltaDecorations(name, deco);
        // }
        console.log("updateCursor");
    }
}


const moveToBottom = () => {
    contents.scrollTop = contents.scrollHeight;
}

const isSameDict = (dict1, dict2) => {
    return JSON.stringify(dict1) === JSON.stringify(dict2);
}