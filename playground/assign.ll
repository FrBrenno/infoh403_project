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
    %a = alloca i32
    call void @println(i32 %6)
    call void @println(i32 %7)
    ret i32 0   
}
