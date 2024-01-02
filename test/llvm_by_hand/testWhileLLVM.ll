@.strR = private unnamed_addr constant [3 x i8] c"%d\00", align 1 
define i32 @readInt() #0 {
   %x = alloca i32, align 4
   %1 = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @.strR, i32 0, i32 0), i32* %x)
   %2 = load i32, i32* %x, align 4
   ret i32 %2
}

declare i32 @scanf(i8*, ...) #1
@.strP = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
define void @println(i32 %x) #0 {
   %1 = alloca i32, align 4
   store i32 %x, i32* %1, align 4
   %2 = load i32, i32* %1, align 4
   %3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @.strP, i32 0, i32 0), i32 %2)
   ret void
}

declare i32 @printf(i8*, ...) #1
define i32 @main() {
   %1 = add i32 0, 0
   %a = alloca i32 
   store i32 %1, i32* %a

   %2 = load i32, i32* %a ; éval de la condition
   %3 = add i32 0, 10
   %4 = icmp slt i32 %2, %3
   br i1 %4, label %while_loop_1, label %while_end_1             
   ; jusqu'ici comme le if

   while_loop_1:
   %5 = load i32, i32* %a ; print(a)
   call void @println(i32 %5)

   %6 = load i32, i32* %a ; a := a+1
   %7 = add i32 0, 1
   %8 = add i32 %6, %7
   store i32 %8, i32* %a

   %9 = load i32, i32* %a ; re-éval de la cond
   %10 = add i32 0, 10
   %11 = icmp slt i32 %9, %10
   br i1 %11, label %while_loop_1, label %while_end_1         ;jusqu'ici comme le if

   while_end_1:
   %12 = add i32 0, 666
   %b = alloca i32 
   store i32 %12, i32* %b

   %13 = load i32, i32* %b
   call void @println(i32 %13)

   ret i32 0
}