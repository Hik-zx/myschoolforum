package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * 隐私实体类
 */
@Data
@TableName("db_account_privacy")
public class AccountPrivacy {
    @TableId(type = IdType.AUTO)
    final Integer id;
    boolean phone=true;
    boolean email =true;
    boolean wx =true;
    boolean qq=true;
    boolean gender =true;

    /**
     * 返回为false的字段
     * @return
     */
    public String[] hiddenFields(){
        List<String> strings = new LinkedList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                //检查当前属性是否为布尔类型，并且其值是否为 false
                if(field.getType().equals(boolean.class) && !field.getBoolean(this))
                    strings.add(field.getName());
            } catch (Exception ignored) {}
        }
        return strings.toArray(String[]::new);
    }
}
