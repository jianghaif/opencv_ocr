package com.yz.edu.answercard.common;

import java.util.HashMap;
import java.util.List;

public class R extends HashMap<String,Object>
{

    public static R ok(){
        R r = new R();
        r.put("code", 0);
        r.put("msg", "success");
        return r;
    }


    public static R error(String s)
    {
        R r = new R();
        r.put("code", 999);
        r.put("msg", s);
        return r;
    }

    public R add(String s, Object obj)
    {
        this.put(s,obj);
        return this;
    }
}
