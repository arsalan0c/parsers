/*
This program is a parser for the following grammar:

L ::= id = E ;
E ::= ( E ) E2
    | - E
    | number E2
E2 ::= - E
    | ϵ

Example query:
parse("A = (1 - 2) - - 3;", T).

The grammar is LL(1) and unambiguous.
*/

:- set_prolog_flag(double_quotes, chars).

s(assign(ID, E)) --> id(ID), space, [=], space, e(E), space, [;], space.
e(expr(E)) --> ['('], space, e(E), space, [')'], space, e2([]), space.
e(b_expr(E, -, E2)) --> ['('], space, e(E), space, [')'], space, e2(E2), space.
e(u_expr(-, E)) --> [-], space, e(E), space.
e(b_expr(N, -, E2)) --> number(N), space, e2(E2), { E2 \= []}, space.
e(N) --> number(N), space, e2([]), space.
e2(E) --> [-], space, e(E), space.
e2([]) --> [].
id(id([C | T])) --> [C | T], { maplist(is_char, [C | T]) }, space.
number(number(N)) --> [C | T], { maplist(is_digit, [C | T]), atomic_list_concat([C | T], '', N) }, space.

space --> " ", space.
space --> "".

is_char(C) :-
	char_type(C, alpha).

is_digit(D) :-
	char_type(D, digit).

parse(X, T) :-
	s(T, X, []), !.
