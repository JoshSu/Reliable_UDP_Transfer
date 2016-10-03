import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by sujiaxu on 16/8/6.
 */
public class Receiver {

    static int probability =80;


    public static byte[] trim (byte[] bytes){
        int i = bytes.length -1;
        while (i>=0 && bytes[i]==0){
            --i;
        }
        return Arrays.copyOf(bytes,i+1);
    }

    public static int randomnum(){
        Random rand = new Random();

        int n = rand.nextInt(100) + 0;
        //System.out.println("n is" +n);
        return n;
    }

    public static int first_unavailebale_node(pkts_in_list [] bigarr, int size,int start, int end){
        for(int i = start;i<=end;i++){
            if (bigarr[i]==null){
                return i;
            }
        }
        return end;

    }

    public static void main(String[] args) {



        DatagramSocket skt;

        try{
            skt = new DatagramSocket();

            String msg = "test message";

            byte [] b = msg.getBytes();

            InetAddress host = InetAddress.getByName("localhost");

            int serverSocket = 6700;

            DatagramPacket request = new DatagramPacket(b, b.length, host,serverSocket);

            //System.out.println(skt.getPort());

            skt.send(request);

            //-------------------------------------------------------

            byte [] buffer = new byte[1024];

            DatagramPacket reply = new DatagramPacket(buffer,buffer.length);


                Thread.sleep(500);
                skt.receive(reply);

            byte [] OBdata = reply.getData();
            ByteArrayInputStream in = new ByteArrayInputStream(OBdata);
            ObjectInputStream is = new ObjectInputStream(in);

            NewportAndpktsize newportAndpktsize = (NewportAndpktsize) is.readObject();
            System.out.println("filename: "+newportAndpktsize.newport);

                int newport = newportAndpktsize.newport;

                String Filename = newportAndpktsize.filename;

                int pktsize = newportAndpktsize.pktsize;

                int window = newportAndpktsize.window;

                pkts_in_list [] mybigarray = new pkts_in_list[pktsize];





                //we connect to the new port in here


            DatagramSocket secondskt = new DatagramSocket();

            String msg1 = "here is";



            byte [] b1 = msg1.getBytes();
            byte [] b2 = new byte[1024];

                DatagramPacket request1 = new DatagramPacket(b1,b1.length,host,newport);
                DatagramPacket rec = new DatagramPacket(b2,b2.length);

            String reply12 = null;

            secondskt.setSoTimeout(1000);
            System.out.println("daozhelil");

            while(true){

                try {
                    secondskt.receive(rec);
                }
                catch (SocketTimeoutException e){
                    secondskt.send(request1);
                    continue;
                }

                System.out.println(b2.length);
                b2 = trim(b2);
                System.out.println(b2.length);

                System.out.println(new String(b2));

                reply12 = new String(rec.getData());

                System.out.println("hahah : "+reply12);
                break;

            }
            Thread.sleep(500);

            //要注意最后几个packet不满window
                int limit = 0;
                int how_much_window = 0;
                int PktRecIncludeDroped = 0;
                int currentSequenceNumber=0;

            //socket 1000
            secondskt.setSoTimeout(1000);

                while (true){
                    try {//接受新的pkts以及resend的pkts
                        System.out.println("reveing new pkts");

                        byte[]  Objbuffer = new byte[1024];
                        DatagramPacket incomingObject = new DatagramPacket(Objbuffer,Objbuffer.length);
                        secondskt.receive(incomingObject);
                        byte [] data = incomingObject.getData();
                        ByteArrayInputStream in1 = new ByteArrayInputStream(data);
                        ObjectInputStream is1  = new ObjectInputStream(in1);
                        pkts_in_list pkts_in_list1 = (pkts_in_list) is1.readObject();

                        System.out.println("just reeived :"+pkts_in_list1.seqNum);

                        if (pkts_in_list1.seqNum>currentSequenceNumber){
                            currentSequenceNumber = pkts_in_list1.seqNum;
                        }

                        int numbergenerated = randomnum();

                        if (numbergenerated>=probability||pkts_in_list1.seqNum>(pktsize-2)){
                            mybigarray[pkts_in_list1.seqNum] = pkts_in_list1;

                            continue;
                        }else{

                            continue;
                        }
                    }catch (SocketTimeoutException e){

                        int start ;

                        //发送NACK
                        if (currentSequenceNumber-window-1<0){
                            start = 0;
                        }else{
                            start = currentSequenceNumber-window-1;
                        }


                        int firstUnavailable = first_unavailebale_node(mybigarray,pktsize,start,currentSequenceNumber);

                        System.out.println("firstUnavailable: " + firstUnavailable );

                        NACK nack = new NACK();

                        nack.NackNumber = firstUnavailable;

                        ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
                        ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(outputStream1);
                        System.out.println("1111111111211111111");

                        objectOutputStream1.writeObject(nack);

                        System.out.println("ffffffssssssssss");



                        byte [] nakbuffer = outputStream1.toByteArray();

                        DatagramPacket nackpacket = new DatagramPacket(nakbuffer,nakbuffer.length,host,newport);

                        secondskt.send(nackpacket);
                        System.out.println("recevier just send the nack");
                        Thread.sleep(200);
                        if (firstUnavailable == pktsize-1){
                            break;
                        }


                    }




                }





            System.out.println("Done transmissing!");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.out.println("1111111111111");

            System.out.println("the pkt size is: " + pktsize );



            for (int i = 0; i<pktsize;++i){
                System.out.println(mybigarray[i].seqNum+"sequenceNUM");
                outputStream.write(mybigarray[i].Data);
                System.out.println(mybigarray[i].seqNum+"sequenceNUM");

            }
            System.out.println("22222222222222");

            byte optbyte [] = outputStream.toByteArray();

            String filename = Integer.toString(newport)+Filename;

            System.out.println("3333333333333333");

            FileUtils.writeByteArrayToFile(new File(filename),optbyte);
            System.out.println("you want a new file?");
            Scanner scanner = new Scanner(System.in);
            String yes_or_no = scanner.nextLine();

            if (yes_or_no.equalsIgnoreCase("yes")) {
                outputStream.close();
                main(new String[]{});

            }else {
                return ;
            }






        }
        catch (Exception ex){

        }


    }

}
