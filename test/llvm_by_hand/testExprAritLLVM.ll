@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1

; Function Attrs: nounwind uwtable
define i32 @readInt() #0 {
  %x = alloca i32, align 4
  %1 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
  %2 = load i32, i32* %x, align 4
  ret i32 %2
}

declare i32 @scanf(i8*, ...) #1

@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: nounwind uwtable
define void @println(i32 %x) #0 {
  %1 = alloca i32, align 4
  store i32 %x, i32* %1, align 4
  %2 = load i32, i32* %1, align 4
  %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
  ret void
}

declare i32 @printf(i8*, ...) #1

; define i32 @main() {
;   entry:
;    %0 = add i32 0,1
;    %a = alloca i32 
;    store i32 %0, i32* %a

;    %b = call i32 @readInt()

;    %1 = load i32, i32* %a
;    call void @println(i32 %1)

;   ;  %2 = load i32, i32* %b
;    call void @println(i32 %b)

;   ret i32 0
; }

define i32 @main() {
    %1 = add i32 0,12
    %2 = add i32 0,3 ;child(1)
    %3 = add i32 0,4 ;child(1)
    %4 = add i32 0,5 ;child(1)
    %5 = mul i32 %4, %3 ; varcount-1, varcount-xxxx 2
    %6 = mul i32 %5, %2 ; varcount-1, varcount-xxxxx 3
    %7 = sub i32 %1, %6 ; varcount-1, varcount-xxxxx 7
    %8 = add i32 0,67

    %9 = add i32 %7, %8 ; varcount-1, varcount-xxxxx 11
    %b = alloca i32
    store i32 %9, i32* %b
    %10 = load i32, i32* %b
    call void @println(i32 %10)
    ret i32 0
}