## Synopsis

This project aims at building programs that simulate the logical/physical address translation process in OS. LRU policy is used to update TLB and replace pages. The value stored in each logical address can be found in a binary file "BACKING_STORE.bin".

#These programs have following functionalities:

1. VirtualMemoryManager.java can do the address translation if physical memory has the same size as logical memory.
2. Modification.java can do the address translation if physical memory is smaller than the logical memory (physical memory has 128 frames). LRU is used for page replacement.
3. AdditionalFunctionality.java can handle addresses with read/write indication. This means that when page replacement happens, it will write the data back to the swap area("BACKING_STORE.bin") if the dirty bit for the page to be replaced is write.
4. All of the above programs output the result as well as the statistics on the screen.

## Code Example

	/*
	 * This method reads the addresses from file and converts them to physical addresses
	 * */
	public void readAddressesFromFile(String fileName){
		int pageNumber,offsetNumber,physicalAddress,TLBIndex,currentFrame,TLBPointer = -1;
		int pageFault = 0,TLBHit = 0,totalAddress = 0;
		try {
			FileInputStream fs = new FileInputStream(fileName);
			BufferedReader bf = new BufferedReader(new InputStreamReader(fs));
			String line;
			while((line = bf.readLine())!=null){
				int number = Integer.parseInt(line);			//Address in decimal
				String binaryNumber = toFixedBinary(number); 	//Change it to binary string
				pageNumber = getBitRange(binaryNumber,0,8);		//get page number
				offsetNumber = getBitRange(binaryNumber,8,16);	//get offset number
				totalAddress++;
				TLBIndex = searchTLB(pageNumber);				//search if it is in TLB
				if(TLBIndex!=-1){
					/*TLB hits!*/
					TLBHit++;	//for statistic purpose
					physicalAddress = TLBF[TLBIndex]*FRAME_SIZE + offsetNumber;	//physical address = frame*frame size + offset
				}else{
					if(pageTable[pageNumber]==-1){
						/*Handling page fault*/
						pageFault++;
						int frameStartPoint = frame*FRAME_SIZE;
						for(int i=0;i<FRAME_SIZE;i++){
							int value = readBackingStore("BACKING_STORE.bin",pageNumber*FRAME_SIZE + i);
							physicalMem[frameStartPoint+i] = value;	//update physical address
						}
						currentFrame = frame;
						pageTable[pageNumber] = frame;
						frame++;
					}else{
						currentFrame = pageTable[pageNumber];
					}
					physicalAddress = currentFrame*FRAME_SIZE + offsetNumber;
					TLBPointer = updateTLB(TLBPointer,currentFrame,pageNumber);	//update TLB table
				}
				String output = "Virtual address: "+number+" Physical address: "+physicalAddress+" Value: "+ physicalMem[physicalAddress];
				System.out.println(output);
			}
			bf.close();
			System.out.println("Page fault rate: "+(float)pageFault/totalAddress*100+'%');
			System.out.println("TLB hit rate: "+(float)TLBHit/totalAddress*100+'%');
		}catch (IOException e) {
			System.out.println("Error: specified file or line cannot be found");
		}
	}

## Motivation

This project was created after learning from chap8 & 9 of book[1] and was used to help to strengthen my understanding about the address translation.


## How to run

1. First, please complie the file:
javac filename.java
for example:
javac VirtualMemoryManager.java
2. Second, please run the file and specify the address file with command:
java filename addressesFile
for example:
java VirtualMemoryManager addresses.txt

Please note that AdditionalFunctionality.java should be tested with addresses file contain write/read indications (e.g addresses2.txt)

## References

[1] Galvin, P. B., Gagne, G., & Silberschatz, A. (2013). Operating system concepts. John Wiley & Sons, Inc..