/*
This program is written in Source 3 Non-Det.
Run the program in the online editor here: 
https://source-academy.github.io/playground 
(Select the Source 3 Non-Det language in the menu)

It is a recursive descent parser for the following grammar:
L ::= id = E ; 
E ::= ( E ) E2
	| - E
	| number E2 
E2 ::= - E
	| ε 

The grammar is LL(1) and unambiguous.
*/

function tokensToStr(lst) {
    let s = "";
    for_each((p) => { s = s + tail(p); }, lst);
    return s;
}

function getToken() {
    if (length(tokens) > 0) {
        const c = list_ref(tokens, 0);
        return c;
    } else {
        amb();  
    }
}

function E2() {
    display("E2");
    let node_E2 = list();
    function first() {
        let c = consume(MINUS);
        const node_E2_E = E();

        node_E2 = append(node_E2, list(head(c), node_E2_E));
        return node_E2;
    }
    
    return amb(first(), (() => { 
            display("E2 second"); 
            return node_E2;
        })()
    );
}

function E() {
    display("E");
    let node_E = list();
    
    let c = consume(list(LPAREN, RPAREN, MINUS, NUMBER));
    let ct = head(c);
    if (ct === LPAREN) {
        const node_E_E = E();
        node_E = append(node_E, list(node_E_E));
        
        c = consume(RPAREN);
        const node_E_E2 = E2();
        if (node_E_E2 !== null) {
            node_E = append(node_E, list(node_E_E2));
        } else {}
        
        return node_E;
    } else if (ct === MINUS) {
        const node_E_E = E();
        node_E = append(node_E, list(ct, node_E_E));
        
        return node_E;
    } else if (ct === NUMBER) {
        const node_E_E2 = E2();
        if (node_E_E2 !== null) {
            node_E = append(node_E, list(c, node_E_E2));
        } else {
            node_E = append(node_E, list(c));
        }
        
        return node_E;
    } else {
        amb();
    }
}

function consume(tokenTypes) {
    if (is_null(tokenTypes)) {
        amb();
    } else if (!is_list(tokenTypes)) {
        const c = getToken();
        tokens = remove(c, tokens);
        if (head(c) !== tokenTypes) {
            amb();
        } else {
            return c;
        }
    } else {
        const c = getToken();
        tokens = remove(c, tokens);
        const matched = filter((t) => { return head(c) === t; }, tokenTypes);
        if (length(matched) <= 0) {
            amb();
        } else {
            return c;
        }
    }
}

function L() {
    display("L");
    let node_L = list();
    
    let c = consume(ID);
    node_L = append(node_L, list(c));
    
    c = consume(EQ);
    
    const node_L_E = E();
    node_L = append(node_L, list(head(c), node_L_E));
    
    consume(SEMICOLON);
    return node_L;
}

function parse() {
    let input = tokensToStr(tokens);
    const out = L();
    require(length(tokens) === 0);
    display("is accepted by the grammar", input);
    display(out, "\nOutput:\n");
}

let LPAREN = "LPAREN";
let RPAREN = "RPAREN";
let SEMICOLON = "SEMICOLON";
let EQ = "EQ";
let MINUS = "MINUS";
let ID = "ID";
let NUMBER = "NUMBER";

let tokens = list(
    pair(ID, "A"), pair(EQ, "="), pair(LPAREN, "("), pair(NUMBER, "1"), pair(MINUS, "-"), 
    pair(NUMBER, "2"),  pair(RPAREN, ")"), pair(MINUS, "-"), pair(MINUS, "-"), pair(MINUS, "-"),
    pair(NUMBER, "3"), pair(SEMICOLON, ";")
);

parse();
