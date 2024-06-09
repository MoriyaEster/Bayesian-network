## input1 (Noam)
alarm_net.xml
B-E|
B-E|J=T
P(B=T|J=T,M=T) A-E
P(B=T|J=T,M=T) E-A
P(J=T|B=T) A-E-M
P(J=T|B=T) M-E-A
END_INPUT
yes
no
0.28417,7,16
0.28417,7,16
0.84902,7,12
0.84902,5,8
END_OUTPUT
## input2 (Noam)
big_net.xml
B0-C2|A2=T,A3=T
A1-D1|C3=T,B2=F,B3=F
P(B0=v3|C3=T,B2=F,C2=v3) A2-D1-B3-C1-A1-B1-A3
END_INPUT
yes
no
0.42307,10,21
END_OUTPUT
## input3
net3.xml
S-N|F=nice
P(M=Y|F=nice) N-S
P(M=Y|F=nice,S=bad) N
P(M=F|S=good) F-N
P(F=nice|N=T) M-S
P(F=boring|S=ok) M-N
END_INPUT
yes
0.75680,11,18
0.38000,3,4
0.00000,11,18
0.70588,2,3
0.30000,0,0
END_OUTPUT
## input4
net4.xml
D-L|S=T
P(I=T|G=fine) D-S-L
P(L=F|D=T) I-G-S
P(G=low|D=F) I-L-S
P(G=low|D=F,I=T) L-S
P(S=F|G=high,L=F) D-I
P(I=T|L=T) D-G-S
P(G=fine|S=T) L-D-I
END_INPUT
no
0.70155,3,6
0.41970,8,12
0.78000,5,6
0.90000,0,0
0.44950,5,10
0.56532,11,20
0.27183,11,20
END_OUTPUT
## input5
net5.xml
B-G|F=always
P(H=T|G=high) B-F
P(H=T|F=never,B=F) G
P(G=medium|B=T) H-F
P(F=always|B=T,H=T) G
P(G=medium|H=T) B-F
P(G=low|F=always) B-H
END_INPUT
no
0.20000,0,0
0.27000,5,6
0.26000,8,9
0.47222,8,12
0.10214,17,30
0.70000,5,6
END_OUTPUT
## input6
net6.xml
A-C|B=noset
A-B|
P(C=run|B=set,A=T) 
P(A=T|C=run) B
P(A=F|C=stay) B
END_INPUT
no
yes
0.05000,0,0
0.07429,5,8
0.82111,5,8
END_OUTPUT
## input7
net7.xml
F-G|C=T,E=two,H=yes
P(A=T|E=two,F=two) B-C-D-G-I-H
P(A=F|B=T,C=F,D=T) E-H-F-I-G
P(G=one|B=T,I=ken) H-E-A-C-D-F
P(D=T|E=one) H-I-A-C-D-F-B
END_INPUT
no
0.11906,15,34
0.96183,1,6
0.71608,21,44
0.39705,7,16
END_OUTPUT
## input8
net8.xml
A-B|
G-F|D=T,E=F,A=F
P(B=T|D=T) A-C-E-F-G
P(D=F|A=T,E=F) B-C-F-G
P(A=T|C=T,F=T,E=F) D-B-G
END_INPUT
yes
yes
0.29344,3,6
0.47264,3,6
1.00000,7,18
END_OUTPUT
## input9
net9.xml
S-R|
P(S=T|C=F) R-W
P(S=T|C=F,W=F) R
P(S=T|W=T) R-W
P(R=F|S=T,W=F) C
END_INPUT
no
0.50000,0,0
0.09091,3,6
0.42976,7,12
0.95890,3,8
END_OUTPUT
## input10
net10.xml
R-S|
P(R=T|S=T) G
P(R=F|S=T) G
P(S=T|G=T) R
P(G=F|R=F) S
END_INPUT
no
0.00621,1,2
0.99379,1,2
0.64673,3,8
0.64000,3,4
END_OUTPUT