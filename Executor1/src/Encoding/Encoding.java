package Encoding;

import ru.spbstu.pipeline.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class Encoding implements IExecutor {
    private Logger logger;
    private CodingGrammar grammar;
    private Double code;
    private int num;
    private Symbols symb;
    private BufferedWriter encoded;
    private TYPE[] types;
    private IConsumer consumer;
    private IProducer producer;
    private IMediator mediator;
    private byte[] ans;

    public Encoding(Logger log){
        grammar = new CodingGrammar();
        logger = log;
        types = new TYPE[]{TYPE.SHORT, TYPE.BYTE};
    };


    private byte[] DoubleToByte(double d){
        byte[] ans = new byte[Double.SIZE / Byte.SIZE];
        long num = Double.doubleToLongBits(d);
        for(int i = 0; i < ans.length; i++){
            ans[i] = (byte)(num & 0xFF);
            num >>>= Byte.SIZE;
        }
        return ans;
    }

    private byte[] Input_Data(){
        TYPE t = Cast.Input_Type(types, producer.getOutputTypes());
        if(mediator.getData() == null){
            return null;
        }
        if(t == TYPE.SHORT) {
            return Cast.ShortToByte((short[])mediator.getData());
        }
        if(t == TYPE.BYTE) {
            return (byte[]) mediator.getData();
        }
        return null;
    }

    public RC setConsumer(IConsumer var1){
        if(var1 == null){
            System.out.println("Not consumer in Coding");
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        consumer = var1;
        return RC.CODE_SUCCESS;
    }

    public RC setProducer(IProducer var1){
        if(var1 == null){
            logger.warning("Not producer in Coding");
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        producer = var1;
        mediator = var1.getMediator(Cast.Input_Type(types, producer.getOutputTypes()));
        if(mediator == null){
            logger.warning("Not mediator in coding");
            return RC.CODE_FAILED_PIPELINE_CONSTRUCTION;
        }
        return RC.CODE_SUCCESS;
    }

    public TYPE[] getOutputTypes(){
        return types;
    }

    public IMediator getMediator(TYPE var1){
        if(var1 == TYPE.SHORT) {
            return new Short_Mediator();
        }
        if(var1 == TYPE.BYTE){
            return new Byte_Mediator();
        }
        return null;
    }

    public RC execute(){
        try {
            byte[] data = Input_Data();
            if(data == null){
                ans = null;
                return consumer.execute();
            }
                int Size_Elem = data.length;
                symb = new Symbols(data);
                Init(symb, data, Size_Elem);
                ans = new byte[Double.SIZE / Byte.SIZE];
                encoded.write(((Integer) symb.length()).toString() + "\n");
                for (int i = 0; i < symb.length(); i++) {
                    encoded.write(symb.Key(i).toString() + " " + symb.Value(i).toString() + "\n");
                }
                encoded.write(((Integer) data.length).toString() + "\n");
                byte[] d = new byte[Double.SIZE / Byte.SIZE];
                d = DoubleToByte(code);
                for (int i = 0; i < Double.SIZE / Byte.SIZE; i++) {
                    ans[i] = d[i];
                }
                encoded.flush();
                return consumer.execute();
        }
        catch(IOException e){
            logger.warning("Can't read line");
            return RC.CODE_INVALID_ARGUMENT;
        }
    }

    class Byte_Mediator implements IMediator {

        public Object getData(){
            if(ans == null){
                return null;
            }
            return Cast.Copy(ans);
        }

    }

    class Short_Mediator implements IMediator {

        public Object getData(){
            if(ans == null){
                return null;
            }
            return Cast.ByteToShort(Cast.Copy(ans));
        }

    }

    public RC setConfig(String configFileName){
        CodingGrammar grammar = new CodingGrammar();
        try{
            Syntaksis synt = new Syntaksis();
            RC rc = synt.Synt(new BufferedReader(new FileReader(configFileName)), logger, grammar.delimiter());
            if(rc != RC.CODE_SUCCESS){
                return rc;
            }
            Coding_Semantic seman = new Coding_Semantic();
            rc = seman.Semantic(synt.Result(), logger);
            if(rc != RC.CODE_SUCCESS){
                return rc;
            }
            encoded = new BufferedWriter(new FileWriter(seman.FileName()));
        }
        catch(FileNotFoundException e){
            logger.warning("Can't make reader");
            return RC.CODE_INVALID_ARGUMENT;
        }
        catch(IOException e){
            logger.warning("Can't make writer");
            return RC.CODE_INVALID_ARGUMENT;
        }
        return RC.CODE_SUCCESS;
    }

    private void Init(Symbols elements, byte[] elem, int size){
        byte[] arr = new byte[size];
        for(int j = 0; j < size; j++){
            arr[j] = elem[j];
        }
        code = Code(elements, arr);
    }

    private double Code(Symbols elements, byte[] arr){
        double a = 0.;
        double b = 1.;
        for(int i = 0; i < arr.length; i++){
            double a1 = a + (b - a) * elements.Prev_Val_Key(arr[i]);
            b = a + (b - a) * elements.Val_Key(arr[i]);
            a = a1;
        }
        return (a + b) / 2;
    }
}