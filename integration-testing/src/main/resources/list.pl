%   File   : list.pl
%   Authors: Mutsunori Banbara (banbara@kobe-u.ac.jp)
%   Updated: 14 February 2008
%   Purpose: List processing
:- op(1150,  fx, (package)). 
package(_). 
:- package 'prolog.example'.

append([], Zs, Zs).
append([X|Xs], Ys, [X|Zs]) :- append(Xs, Ys, Zs).

nrev([], []).
nrev([X|Xs], Y) :-
    nrev(Xs, Ys), append(Ys, [X], Y).

range(I, N, []) :- I > N, !.
range(I, N, [I|L]) :- I =< N, I1 is I+1, range(I1, N, L).

