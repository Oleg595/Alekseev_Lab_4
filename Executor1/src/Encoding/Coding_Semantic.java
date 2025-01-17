package Encoding;

import ru.spbstu.pipeline.RC;

import java.util.TreeMap;
import java.util.logging.Logger;

public class Coding_Semantic {
    private String data;
    public RC Semantic(TreeMap<String, String> map, Logger logger){
        RC rc = Check(map, logger);
        if(rc != RC.CODE_SUCCESS){
            return rc;
        }
        for (String str : map.keySet()) {
            if(str.equals(CodingGrammar.TokenCoding.FILE.toString())){
                data = map.get(str);
            }
        }
        return RC.CODE_SUCCESS;
    }

    private RC Check(TreeMap<String, String> map, Logger logger){
        if(map.get(CodingGrammar.TokenCoding.FILE.toString()) == null){
            logger.warning("Not file with/for data");
            return RC.CODE_INVALID_ARGUMENT;
        }
        return RC.CODE_SUCCESS;
    }

    public String FileName(){
        return data;
    }
}
