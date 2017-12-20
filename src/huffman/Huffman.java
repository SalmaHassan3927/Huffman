package huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Huffman {

    public static HashMap<Character, Integer> map = new HashMap<>();
    public static HashMap<Character, String> codesMap = new HashMap<>();
    public static HashMap<Character, String> codesMap2 = new HashMap<>();
    public static PriorityQueue<Node> queue = new PriorityQueue<>(new Comparator<Node>() {
        public int compare(Node node1, Node node2) {
            if (node1.getValue() < node2.getValue()) {
                return -1;
            }
            if (node1.getValue() > node2.getValue()) {
                return 1;
            }
            return 0;
        }
    });

    public static void read() {

        String fileName = "inputfile.txt";
        String line = null;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int ch;
            char c;
            int freq = 1;
            while ((ch = bufferedReader.read()) != -1) {
                c = (char) ch;
                if (map.containsKey(c)) {
                    freq = map.get(c) + 1;
                    map.put(c, freq);
                } else {
                    map.put(c, 1);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void insertToHeap() {
        for (char key : map.keySet()) {
            Node node = new Node();
            node.setValue(map.get(key));
            node.setCharacter(key);
            queue.add(node);
        }

    }

    public static Node buildHuffmanTree() {
        while (queue.size() != 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            Node n = new Node(left.getValue() + right.getValue(), '$', left, right);
            queue.add(n);
        }
        return queue.poll();
    }

    public static void printMap(HashMap<Character, String> map) {
        for (char key : map.keySet()) {
            System.out.println(key + ":  " + map.get(key));
        }
    }

    public static void getHuffmanCodes(Node root, String code) {
        if (root == null) {
            return;
        }

        if (root.getLeft()==null&&root.getRight()==null) {
            codesMap.put(root.getCharacter(), code);
        }

        getHuffmanCodes(root.getLeft(), code + "0");
        getHuffmanCodes(root.getRight(), code + "1");
    }
    public static int getCodeSize(){
        int size=0;
        for(char key: codesMap.keySet()){
            size+=codesMap.get(key).length()*map.get(key);
        }
        return size;
    }
    public static void compress() {
        String inputFile = "inputfile.txt";
        String outputFile = "compressed";
        FileReader fr = null;
        BufferedReader br = null;
        FileOutputStream stream = null;
        try {
            fr = new FileReader(inputFile);
            br = new BufferedReader(fr);
           stream = new FileOutputStream(outputFile);
            try {
                //saving huffman codes in the header
                byte[] CodeSize=ByteBuffer.allocate(4).putInt(getCodeSize()).array();
                System.out.println("size of code:"+getCodeSize());
                stream.write(CodeSize);
                byte [] mapSizeBytes=ByteBuffer.allocate(4).putInt(codesMap.size()).array();
                System.out.println("size of map:"+codesMap.size());
                    stream.write(mapSizeBytes);
                for(char key: codesMap.keySet()){
                    String character=new String();
                    character+=key;
                    byte [] charBytes=character.getBytes();
                    stream.write(charBytes);
                    byte [] sizeBytes=ByteBuffer.allocate(4).putInt(codesMap.get(key).length()).array();
                    stream.write(sizeBytes);
                    byte[] codeBytes=codesMap.get(key).getBytes();
                    stream.write(codeBytes);
                }
                //saving compressed file
                String code= new String();
                int ch;
                char c;
                while ((ch = br.read()) != -1) {
                    c = (char) ch;
                    code += codesMap.get(c);
                    if(code.length()%8==0&&code.length()!=0){
                        int length=code.length();
                        byte[] bytes = new byte[(code.length()+Byte.SIZE-1)/Byte.SIZE];
                        char s;
                        for(int i=0;i<length;i++){
                        if((s=code.charAt(i))=='1')
                         bytes[i/Byte.SIZE]=(byte)(bytes[i/Byte.SIZE]|(0x00 >>>(i%Byte.SIZE)));
                        }
                         
                        stream.write(bytes);
                         code="";
                    }
                   
                }
              if(code.length()!=0){
                  int length=code.length();
                        byte[] bytes = new byte[(code.length()+Byte.SIZE-1)/Byte.SIZE];
                        char s;
                        for(int i=0;i<length;i++){
                        if((s=code.charAt(i))=='1')
                            bytes[i/Byte.SIZE]=(byte)(bytes[i/Byte.SIZE]|(0x00 >>>(i%Byte.SIZE)));
                        }
                        
                        stream.write(bytes);
              }
             stream.close();
            } catch (FileNotFoundException ex) {
                System.out.println("Unable to open file '" + outputFile + "'");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + inputFile + "'");
        } 
    }
    public static void decompress(){
        String inputFile = "compressed";
        File file = new File(inputFile);
        FileInputStream stream = null;
        int i,j,m,size,n; char c;
        try{
            stream = new FileInputStream(file);
            byte fileContent[] = new byte[(int)file.length()];
            stream.read(fileContent);
            stream.close();
            String s=new String();
            int sizeOfCode,sizeOfMap;
            for(i=0;i<4;i++){
            s+=String.format("%02x", fileContent[i]);
            }
            sizeOfCode = Integer.parseInt(s,16);
            System.out.println("size of code:"+sizeOfCode);
            s="";
            for(j=i;j<i+4;j++){
                s+=String.format("%02x", fileContent[j]);
            }
            sizeOfMap=Integer.parseInt(s,16);
            System.out.println("size of map:"+sizeOfMap);
            s="";
            int count=j;
            for(int k=0;k<sizeOfMap;k++){
                 c=(char)fileContent[count];
                 count++;
                 for(m=count;m<count+4;m++){
                     s+=String.format("%02x", fileContent[m]);
                 }
                size=Integer.parseInt(s,16);
                s="";
                String code=new String();
                for(n=m;n<m+size;n++){
                    code+=(char)fileContent[n];
                }
                codesMap2.put(c, code);
                count=n;
            }
            s="";
//            for(int y=count;y<fileContent.length;y++){
//                s+= String.format("%8s", Integer.toBinaryString(fileContent[y] & 0xFF)).replace(' ', '0');
//                System.out.println(fileContent[y]);
//            }
        }
        catch (FileNotFoundException ex) {
                System.out.println("Unable to open file '" + inputFile + "'");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }
    public static void main(String[] args) {
        read();
        insertToHeap();
        Node root = buildHuffmanTree();
        getHuffmanCodes(root, "");
        printMap(codesMap);
        compress();
        decompress();
        printMap(codesMap2);
        }
}
