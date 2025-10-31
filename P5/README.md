java -jar jtb132.jar microIR.jj
javacc jtb.out.jj
javac P5.java
java P5 < P5.microIR
