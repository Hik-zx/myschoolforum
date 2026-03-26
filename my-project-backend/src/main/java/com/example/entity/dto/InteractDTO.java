package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class InteractDTO {
    Integer tid;
    Integer uid;
    Date time;
    String type;

    public String toKey(){
        return tid+":"+uid;
    }

    /**
     * 分隔key  tid+":"+uid;
     * @param str
     * @param type
     * @return
     */
    public static InteractDTO parseInteract(String str,String type){
        String [] keys= str.split(":");
        return new InteractDTO(Integer.parseInt(keys[0]),Integer.parseInt(keys[1]),new Date(),type);
    }
}
