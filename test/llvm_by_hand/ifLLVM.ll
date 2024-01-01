@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1 
@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

declare i32 @scanf(i8*, ...) #1
define i32 @readInt() #0 {
   %1 = alloca i32, align 4
   %2 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %1)
   %3 = load i32, i32* %1, align 4
   ret i32 %2
}

declare i32 @printf(i8*, ...) #1
define void @println(i32 %x) #0 {
   %1 = alloca i32, align 4
   store i32 %x, i32* %1, align 4
   %2 = load i32, i32* %1, align 4
   %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
   ret void
}

define i32 @main() {
    %1 = add i32 0, 1
    %a = alloca i32 
    store i32 %1, i32* %a

    %2 = add i32 0, 666
    %b = alloca i32 
    store i32 %2, i32* %b

    ; if 
    ; condition => if_true or if_false
    %3 = load i32, i32* %a
    %4 = add i32 0, 3
    %5 = icmp slt i32 %3, %4
    br i1 %5, label %if_true_0, label %if_false_0
    if_true_0:
        %6 = load i32, i32* %a
        store i32 %6, i32* %b

        %7 = load i32, i32* %b
        call void @println(i32 %7)

        br label %if_end_0
    if_false_0:
        %8 = load i32, i32* %b
        store i32 %8, i32* %a

        %9 = load i32, i32* %a
        call void @println(i32 %9)

        br label %if_end_0
    if_end_0:
        %10 = load i32, i32* %a
        call void @println(i32 %10)

        %11 = load i32, i32* %b
        call void @println(i32 %11)
        ret i32 0    
}