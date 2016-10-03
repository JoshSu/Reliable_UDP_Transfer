import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


/**
 * Created by sujiaxu on 16/8/7.
 *
 * 0.1 version will create the file and put bytes in the arratlist full of objects that contains the sequence number and the fielname
 *
 * 0.2 version will send each object to the receiver and test if they can be received in there.
 *
 * 0.3 version will do the thread, and use differnt port number for different client
 *
 * 0.4 version will deal with timeout, deal with newobject sending NewportanfdPJTSIZE ,start to do the resend stuff
 *
 * 0.5 version will do the stop and wait.
 *
 * 0.8 will add the checksum and support mutiple files
 */


public class Sender {


    public static int  pktsize;


    public ArrayList <MyChannel> all = new ArrayList<>();

    public static ArrayList <pkts_in_list> allpkts;

    public static int windowsize = 8;











    public static ArrayList put_pkts_in_list(Path path , int bytesinpkt) throws IOException {



        //put bytes in pkts

        ArrayList pkts = new ArrayList<DatagramPacket>();

        byte [] data = Files.readAllBytes(path);




        int file_length = data.length;

        System.out.println(data.length/bytesinpkt);

       // System.out.println("!!!!!!!!!!!!!!!!!"+file_length);
        for (int i = 0; i<data.length;i++) {


            byte data1 = data[i];


            pkts_in_list node = new pkts_in_list();

            node.seqNum = i / bytesinpkt;

            node.filename = path.toString();


            if (file_length < bytesinpkt) {
                byte[] newarr = Arrays.copyOfRange(data, i, i + file_length);

                node.Data = newarr;

                //System.out.println(new String(node.singlePkt));


                i = i + bytesinpkt - 1;

                file_length = 0;

            } else {

                byte[] newarr = Arrays.copyOfRange(data, i, i + bytesinpkt);


                node.Data = newarr;

                //System.out.println(new String(node.singlePkt));

                i = i + bytesinpkt - 1;

                file_length = file_length - bytesinpkt;

            }

            pkts.add(node);
        }


        System.out.println(pkts.size());

        for (int i =0;i<pkts.size();i++){
            String pt = new String(((pkts_in_list)pkts.get(i)).Data);

           // System.out.println(pt+"!!!!!!!!");
        }

        for (int i = 0;i<pkts.size();i++){
            ((pkts_in_list)pkts.get(i)).how_many_paks_in_list = pkts.size();

        }
        //System.out.println(((pkts_in_list)pkts.get(5)).how_many_paks_in_list +"haha");


        return pkts;

    }


//--------------------------------------------------------------------------



    public static void main(String[] args) throws IOException {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Path path = Paths.get(args[0]);


        int bytesinpkt = 100;
        allpkts = put_pkts_in_list(path ,bytesinpkt );

        pktsize = allpkts.size();



        DatagramSocket skt = null;

        DatagramSocket skt1 = null;




        new Sender().start(args[0]);


    }

    public int randomnum(){
        Random rand = new Random();

        int n = rand.nextInt(50000) + 10000;
        //System.out.println("n is" +n);
        return n;
    }

    public void start(String Fname) throws IOException{




        DatagramSocket skt = null;

        DatagramSocket skt1 = null;

        skt = new DatagramSocket(6700);



        skt.setSoTimeout(6000);

        byte [] buffer = new byte[20];

        while (true){

            try {

                int randomport = randomnum();

                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                skt.receive(request);

                String msg = new String(request.getData());

                //byte [] sendmsg = (Integer.toString(randomport).substring(0,5)+Integer.toString(pktsize)).getBytes();

                //DatagramPacket reply = new DatagramPacket(sendmsg,sendmsg.length, request.getAddress(),request.getPort());

                System.out.println(request.getPort() + " " + request.getAddress());

                //如何判定接受的是新的加入申请还是ack   在这里只处理申请, 传给receiver一个port, 以后的东西都发到那个port去

                NewportAndpktsize newportAndpktsize = new NewportAndpktsize();

                newportAndpktsize.newport = randomport;
                newportAndpktsize.pktsize = pktsize;
                newportAndpktsize.filename = Fname;
                newportAndpktsize.window = windowsize;

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(outputStream);
                os.writeObject(newportAndpktsize);
                byte[] reply = outputStream.toByteArray();
                DatagramPacket sendpkt = new DatagramPacket(reply, reply.length, request.getAddress(), request.getPort());

                //System.out.println("hhh "+reply.getPort());
                skt.send(sendpkt);
                MyChannel channel = new MyChannel(randomport);
                all.add(channel);
                new Thread(channel).start();
            }catch (SocketTimeoutException e){
                int counter  = 0;
                for(MyChannel other :all){
                    if (other.DoneWithOneFile == false){
                        System.out.println("we still have someone transmissiong");
                        counter++;

                    }

                }
                if(counter==0) {
                    System.out.println("everyone done transmissing, give me a new filenanme");
                    Scanner scan = new Scanner(System.in);

                    String newfilename = scan.nextLine();
                    skt.close();

                    main(new String[]{newfilename, "20"});
                }

            }

            //check if all done transmissing




        }
        //System.out.println("haha");







    }

    class MyChannel implements Runnable{
        public boolean isRunning = true;
        int newport;
        boolean DoneWithOneFile=false;

        public MyChannel(int newport ){
            this.newport = newport;

        }




        @Override
        public void run() {

            try {
                DatagramSocket skt = new DatagramSocket(newport);
                byte [] buffer = new byte[1024];

                DatagramPacket request = new DatagramPacket(buffer,buffer.length);

                skt.receive(request);

                String message = new String(request.getData());

                System.out.println("message is :"+ request.getPort()+message);

                byte [] sendback = "back from server".getBytes();
                Thread.sleep(500);
                System.out.println("port: " +request.getPort());

                DatagramPacket reply  = new DatagramPacket(sendback,sendback.length,request.getAddress(),request.getPort());

                skt.send(reply);

                System.out.println("already sent the stuff");

                skt.setSoTimeout(2000);
                int index = 0;


                //-----------------------first send 8 pkts
                for (int counter = 0;counter< windowsize;counter++){
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(outputStream);
                    os.writeObject(allpkts.get(counter));
                    byte[] reply1 = outputStream.toByteArray();
                    DatagramPacket sendpkt = new DatagramPacket(reply1,reply1.length,request.getAddress(),request.getPort());


                    skt.send(sendpkt);
                    System.out.println("just sent pkt "+counter);
                    index = counter;

                    Thread.sleep(200);

                }

                //--------------------

                skt.setSoTimeout(2000);
                    //socket 2000
                while (true){
                    try {
                        /*byte [] b2 = new byte[1024];
                        DatagramPacket rec = new DatagramPacket(b2,b2.length);
                        skt.receive(rec);
                        index++;*/


                        byte [] NackBuffer = new byte[1024];
                        DatagramPacket NackGram = new DatagramPacket(NackBuffer,NackBuffer.length);
                        System.out.println("Waitning for Nacks");
                        skt.receive(NackGram);
                        System.out.println("reciveed NACKs");



                        byte [] NakData = NackGram.getData();
                        ByteArrayInputStream in =  new ByteArrayInputStream(NakData);
                        ObjectInputStream is = new ObjectInputStream(in);
                        NACK nack = (NACK)is.readObject();

                        int Nacknumber = nack.NackNumber;



                        if (Nacknumber>=allpkts.size()-1){
                            break;
                        }
                        if (Nacknumber==index){
                            throw new SocketTimeoutException();
                        }
                        //do the resend
                        for (int i = Nacknumber;i<=index;i++){
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ObjectOutputStream os = new ObjectOutputStream(outputStream);
                            os.writeObject(allpkts.get(i));
                            byte[] reply1 = outputStream.toByteArray();
                            DatagramPacket sendpkt = new DatagramPacket(reply1,reply1.length,request.getAddress(),request.getPort());


                            skt.send(sendpkt);
                            Thread.sleep(300);
                            System.out.println("just resent the packet!!!!!! "+ i);
                        }





                        //---------------------------------------------------



                    }catch (SocketTimeoutException e){


                        if (index>=pktsize){
                            continue;
                        }
                        //在这里发8个包
                        int counter = 0;
                        System.out.println("I am in the Catch");

                        while (counter<=windowsize){
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            ObjectOutputStream os = new ObjectOutputStream(outputStream);
                            os.writeObject(allpkts.get(index));
                            byte[] reply1 = outputStream.toByteArray();
                            DatagramPacket sendpkt = new DatagramPacket(reply1,reply1.length,request.getAddress(),request.getPort());


                            skt.send(sendpkt);
                            System.out.println("just sent pkt "+index);
                            if (index+1>=pktsize){
                                break;
                            }
                            index++;
                            counter++;
                            Thread.sleep(200);

                        }

                        /*if (index==allpkts.size()){
                            break;
                        }

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(outputStream);
                        os.writeObject(allpkts.get(index));
                        byte[] reply1 = outputStream.toByteArray();
                        DatagramPacket sendpkt = new DatagramPacket(reply1,reply1.length,request.getAddress(),request.getPort());


                        skt.send(sendpkt);
                        System.out.println("just sent pkt "+index);*/
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                }
                System.out.println("done transmissing!!!!!!");
                DoneWithOneFile = true;
                skt.close();





            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

}
