import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayDeque;
import java.util.Deque;

public class VirtualMemoryManager {
	
/*In this memory system, the logical memory is at the same size of physical memory*/
	
	public static int FRAME_SIZE = 256;	//Size of each frame
	public static int FRAMES = 256;	//Number of frames in physical memory
	public static int PAGES = 256;	//Number of pages in logical memory
	public static int TLB_SIZE = 16;	//Size of TLB table
	
	public int[] physicalMem;	//Physical memory (1x256*256)
	public int[] pageTable;		//Page table			
	public int frame;			//Current frame number
	
	/*TLB table. if TLBP[i]=pageNumber, the related frame to that page number is TLBF[i]*/
	public int[] TLBP;			//TLB table, page section
	public int[] TLBF;			//TLB table, frame section
	public Deque<Integer> TLBStack;	//stores the index of TLBP and TLBF
	
	/*Initializing the VMmanager*/
	public VirtualMemoryManager(){
		this.physicalMem = new int[FRAMES*FRAME_SIZE];
		this.pageTable = new int[PAGES];
		this.TLBP = new int[TLB_SIZE];
		this.TLBF = new int[TLB_SIZE];
		this.TLBStack = new ArrayDeque<Integer>();
		this.frame = 0;
		/*initialize page table*/
		for(int i=0;i<this.pageTable.length;i++){
			this.pageTable[i] = -1;
		}
		/*initialize TLB*/
		for(int i=0;i<TLB_SIZE;i++){
			TLBP[i] = -1;
			TLBF[i] = -1;
		}
	}
	
	/*
	 * Our program starts here
	 */
	public static void main(String[] args) {
		VirtualMemoryManager vm = new VirtualMemoryManager();
		if(args.length!=1){
			System.out.println("Please specify a file name");
			System.out.println("Usage: java fileName addresses.txt");
		}else{
			String fileName = args[0];
			vm.readAddressesFromFile(fileName);	//read the provided addresses and translate them
		}
	}
	
	/*
	 * This method will change the given decimal number to a 16 bits binary string
	 */
	public String toFixedBinary(int number){
		return Integer.toBinaryString(0x10000|number).substring(1);
	}
	
	/*
	 * This method will read the value from swap area and return it
	 */
	public int readBackingStore(String fileName,int seek){
		try{
			RandomAccessFile bs = new RandomAccessFile(fileName,"r");
			bs.seek(seek);
			int value = bs.readByte();
			bs.close();
			return value;
		}catch (IOException e){
			System.out.println("Error occurred when reading random access file");
			return -1;
		}
	}
	
	/*
	 * This method will search for the given page number in TLB table and return 
	 * related frame number. If it can't be found, this method returns -1 
	 */
	public int searchTLB(int pageNumber){
		for(int i=0;i<TLB_SIZE;i++){
			if(TLBP[i]==pageNumber){
				//find page number in TLB
				TLBStack.removeFirstOccurrence(i);
				TLBStack.push(i);	//update page Number
				return i;
			}
		}
		return -1;
	}
	
	/*
	 * This method uses LRU to update TLB page and returns the new table pointer 
	 */
	public int updateTLB(int TLBPointer, int frameNumber,int pageNumber){
		
		if(TLBStack.size()>=TLB_SIZE){
			//TLB Stack is full
			TLBPointer = TLBStack.removeLast();
		}else{
			TLBPointer++;
		}
		TLBP[TLBPointer] = pageNumber;
		TLBF[TLBPointer] = frameNumber;
		TLBStack.push(TLBPointer);
		return TLBPointer;
	}
	
	public void printTLB(){
		for(int i=0;i<TLB_SIZE;i++){
			System.out.println("Page: "+TLBP[i]+" Frame: "+TLBF[i]);
		}
	}
	
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
//				writeOutputToFile("out1.txt",output);
			}
			bf.close();
			System.out.println("Page fault rate: "+(float)pageFault/totalAddress*100+'%');
			System.out.println("TLB hit rate: "+(float)TLBHit/totalAddress*100+'%');
		}catch (IOException e) {
			System.out.println("Error: specified file or line cannot be found");
		}
	}
	
	/*
	 * This method reads a certain portion of a binary String and returns it in decimal
	 */
	public int getBitRange(String binaryNumber,int start,int end){
		String bitRange = binaryNumber.substring(start,end);
		return Integer.parseInt(bitRange,2);
	}
	
	public void writeOutputToFile(String fileName,String output) throws IOException{
		FileWriter fw = new FileWriter(fileName,true); //option "true" lets us append data to file
		fw.write(output+"\n");
		fw.close();
	}
}