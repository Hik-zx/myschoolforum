package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Account;
import com.example.entity.AccountDetails;
import com.example.entity.AccountPrivacy;
import com.example.entity.dto.*;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountService;
import com.example.util.Const;
import com.example.util.FlowUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *认证相关实现
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Autowired
    AmqpTemplate amqptemplate;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    FlowUtils flowUtils;
    @Autowired
    AccountPrivacyMapper accountPrivacyMapper;
    @Autowired
    AccountDetailsMapper accountDetailsMapper;
    //返回userdetails对象,获取用户名和密码自动比较输入的能不能对上

    /**
     * 用户登录
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account =this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
            return User
                    //有可能是邮箱或者用户名
                    .withUsername(username)
                    .password(account.getPassword())
                    .roles(account.getRole())
                    .build();
    }

    /**
     * 通过邮箱或者账号查找用户
     * @param text
     * @return
     */
    public Account findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    /**
     * 通过id查找用户
     * @param id
     * @return
     */
    public Account fingAccountById(int id) {
        //return this.baseMapper.selectById(id);
        return this.query().eq("id",id).one();
    }

    /**
     * 邮箱验证码
     * @param type
     * @param email
     * @param ip
     * @return
     */
    @Override
    public String registerEmailCode(String type, String email, String ip) {
        synchronized (ip.intern()){
            //通过验证
            if (!this.verifyLimit(ip))
                return "请求频繁，稍等重试";

            //生成6位数随机验证码
            Random random=new Random();
            int code = random.nextInt(899999) + 100000;
            //生成一个不可变的map
            Map<String,Object> data=Map.of("type",type,"email",email,"code",code);
            //加入消息队列
            amqptemplate.convertAndSend("mail",data);
            //存入redis,用于验证用户输入是否正确,有效时间为3分钟
            redisTemplate.opsForValue().
                    set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;

        }
    }

    /**
     * 邮箱注册
     * @param emailRegisterDTO
     * @return
     */
    @Override
    public String registerEmailAccount(EmailRegisterDTO emailRegisterDTO) {
        String email = emailRegisterDTO.getEmail();
        String username = emailRegisterDTO.getUsername();
        //从redis取验证码
        String code = redisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null) return "请先获取验证码";
        if(!code.equals(emailRegisterDTO.getCode())) return "验证码错误，请重新输入";
        if(this.existAccountByemail(email)) return "该邮箱已被注册！";
        if(this.existAccountByUsername(username)) return "用户名已存在，请重新输入!";
        //密码加密
        String password = passwordEncoder.encode(emailRegisterDTO.getPassword());
        //构造Account，进行插入数据库
        Account account=new Account(null,username,password,email,"user",null,new Date());
        if(this.save(account)){
            //删除redis中用过的验证码
            redisTemplate.delete(Const.VERIFY_EMAIL_DATA+email);
            //给用户增加默认隐私信息
            accountPrivacyMapper.insert(new AccountPrivacy(account.getId()));
            AccountDetails accountDetails=new AccountDetails();
            accountDetails.setId(account.getId());
            accountDetailsMapper.insert(accountDetails);
            return null;
        }else {
            return "出错啦，请联系管理员";
        }

    }

    /**
     * 是否确认重置密码
     * @param confirmResetDTO
     * @return
     */
    @Override
    public String confirmReset(ConfirmResetDTO confirmResetDTO) {
        //获取输入的邮箱
        String email = confirmResetDTO.getEmail();
        //获取redis该邮箱的code
        String code = redisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null) return "请先获取验证码";
        if(!code.equals(confirmResetDTO.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    /**
     * 重置密码
     * @param resetPasswordDTO
     * @return
     */
    @Override
    public String resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String email = resetPasswordDTO.getEmail();
        String code = resetPasswordDTO.getCode();
        String verify = this.confirmReset(new ConfirmResetDTO(email, code));
        if(verify!=null) return verify;//没有确认重置密码
        //对密码进行加密
        String encodepassword = passwordEncoder.encode(resetPasswordDTO.getPassword());
        boolean update = this.update().eq("email", email).set("password", encodepassword).update();
        if(update){
            //删除存储在redis的验证码
            redisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        }
        return null;
    }

    /**
     * 修改邮箱
     * @param id
     * @param modifyEmailDTO
     * @return
     */
    @Override
    public String modifEmail(int id, ModifyEmailDTO modifyEmailDTO) {
        String email = modifyEmailDTO.getEmail();
        //从redis中获取验证码
        String code =  redisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA+email);
        if (code==null) return "请先获取验证码";
        if(!code.equals(modifyEmailDTO.getCode())) return "验证码错误，请重新输入";
        //删除存储在redis的验证码
        redisTemplate.delete(Const.VERIFY_EMAIL_DATA + email);
        Account account = this.findAccountByNameOrEmail(email);
        if(account !=null && account.getId() !=id)
            return "该电子邮箱已经被注册，无法完成操作!";
        //修改
         this.update().eq("id",id)
                      .set("email",email)
                      .update();
        return null;
    }

    /**
     * 修改密码
     * @param id
     * @param modifyEmailDTO
     * @return
     */
    @Override
    public String modifyPassword(int id, ModifyPasswordDTO modifyEmailDTO) {
        //取出用户的密码
        String password = this.query().eq("id", id).one().getPassword();
        //把密码和前端传过来的密码进行比较
        if(!passwordEncoder.matches(modifyEmailDTO.getPassword(),password))
            return "原密码错误，请重新输入!";
        String new_password = passwordEncoder.encode(modifyEmailDTO.getNew_password());
        boolean update = this.update().eq("id", id).set("password", new_password).update();
        return update? null:"未知错误，请联系管理员";
    }

    /**
     * 判断是否重复操作（限流）,60s内不可重复操作
     */

    public boolean verifyLimit(String ip){
        String verifyEmailLimit = Const.VERIFY_EMAIL_LIMIT+ip;
        return flowUtils.limitOneCheck(verifyEmailLimit,60);

    }

    /**
     * 判断邮箱是否被注册
     * @param email
     * @return
     */
    public boolean existAccountByemail(String email){
        return this.baseMapper.exists(Wrappers.<Account>query().eq("email",email));
    }

    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean existAccountByUsername(String username){
       return this.baseMapper.exists(Wrappers.<Account>query().eq("username",username));
    }
}
