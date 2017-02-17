jmp testing

adder:
push ax
jno adder_done
mov ax, 1
shl ax, cl
or bx, ax
adder_done:
pop ax
ret


testing:
mov bx, 0
;0: p-p=p
mov cl, 0
mov al, 5
sub al, 3
call adder
;1: p-p=n
mov cl, 1
mov al, 5
sub al, 7
call adder
;2: p-n=p
mov cl, 2
mov al, 5
sub al, -2
call adder
;3: p-n=n
mov cl, 3
mov al, 120
sub al, -30
call adder
;4: n-p=p
mov cl, 4
mov al, -30
sub al, 120
call adder
;5: n-p=n
mov cl, 5
mov al, -30
sub al, 3
call adder
;6: n-n=p
mov cl, 6
mov al, -5
sub al, -10
call adder
;7: n-n=n
mov cl, 7
mov al, -5
sub al, -3
call adder



;0: p+p=p
mov cl, 8
mov al, 5
add al, 3
call adder
;1: p+p=n
mov cl, 9
mov al, 120
add al, 20
call adder
;2: p+n=p
mov cl, 10
mov al, 5
add al, -2
call adder
;3: p+n=n
mov cl, 11
mov al, 5
add al, -10
call adder
;4: n+p=p
mov cl, 12
mov al, -30
add al, 40
call adder
;5: n+p=n
mov cl, 13
mov al, -30
add al, 3
call adder
;6: n+n=p
mov cl, 14
mov al, -120
add al, -20
call adder
;7: n+n=n
mov cl, 15
mov al, -5
add al, -3
call adder


;bx = 0100001000011000
;set: p-n=n & n-p=p & p+p=n & n+n=p