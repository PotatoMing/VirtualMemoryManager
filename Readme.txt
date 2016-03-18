CS543 – Operating Systems
Winter 2015 - 2016
Professor Mike Kain
Homework # 4 – Virtual Memory Manager
Due Date: Monday, February 29th, 2016 at 11:59pm EST

Author: Mingchuan Dong id:13889963

Application
--------------------------------------------------------------------
These programs are designed to be able to translate logical to physical 
addresses for a virtual address space of size 2^16. I used LRU policy
to update TLB and replace pages. These programs have following 
functionalities:
1. VirtualMemoryManager.java can do the address translation if physical 
memory has the same size as logical memory.
2. Modification.java can do the address translation if physical memory 
is smaller than the logical memory (physical memory has 128 frames). 
LRU was used for page replacement.
3. AdditionalFunctionality.java can handle addresses with read/write 
indication. This means that when page replacement happens, it will
write the data back to the swap area if the dirty bit for the page to
be replaced is write.
4. All of the above programs output the result as well as the statistics
on the screen.

Development
--------------------------------------------------------------------
The program was developed in Java on Windows 10. 
Programs were tested in jre1.8.0_45

How to run
---------------------------------------------------------------------
1. First, please complie the file:
javac filename.java
for example:
javac VirtualMemoryManager.java
2. Second, please run the file and specify the address file with command:
java filename addressesFile
for example:
java VirtualMemoryManager addresses.txt


Submitted Files
---------------------------------------------------------------------
The programs required are:
VirtualMemoryManager.java(original code), Modification.java, 
AdditionalFunctionality.java

The answers to additional questions and Statistics results are saved into: 
mdong_hw4.pdf
