package com.example.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.*;
import com.example.entity.Topic;
import com.example.entity.dto.AddCommentDTO;
import com.example.entity.dto.InteractDTO;
import com.example.entity.dto.TopicCreatDTO;
import com.example.entity.dto.TopicUpdateDTO;
import com.example.entity.vo.response.CommentVO;
import com.example.entity.vo.response.TopicDetailsVO;
import com.example.entity.vo.response.TopicPreviewVO;
import com.example.entity.vo.response.TopicTopVO;
import com.example.mapper.*;
import com.example.service.AccountPrivacyService;
import com.example.service.NotificationService;
import com.example.service.TopicService;
import com.example.util.CacheUtil;
import com.example.util.Const;
import com.example.util.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 帖子相关类
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    @Autowired
    FlowUtils flowUtils;
    @Resource
    StringRedisTemplate template;

    @Resource
    TopicTypeMapper mapper;
    @Resource
    AccountMapper accountMapper;
    @Resource
    AccountDetailsMapper accountDetailsMapper;
    @Resource
    AccountPrivacyMapper accountPrivacyMapper;
    @Resource
    TopicCommentMapper topicCommentMapper;
    @Autowired
    CacheUtil cacheUtil;
    @Autowired
    NotificationService notificationService;
   //types变量将存储一个不包含重复id的集合
    //收集所有帖子类型对应的id
    @PostConstruct
    private void initTypes(){
        types=this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    private  Set<Integer> types=null;
    @Override
    public List<TopicType> listTypes() {
        return mapper.selectList(null);
    }

    /**
     * 创建帖子
     * @param topicCreatDTO
     * @param uid
     * @return
     */
    @Override
    public String creatTopic(TopicCreatDTO topicCreatDTO, int uid) {
        if(!textLimitCheck(topicCreatDTO.getContent(),20000))
            return "文章内容太多！";
        if(!types.contains(topicCreatDTO.getType()))
            return "文章类型非法";
        String key= Const.FORUM_TOPIC_CREATE_COUNTER;
        if(!flowUtils.limitPeriodCounterCheck(key,10,3600))
            return "帖子发送频繁。稍后重试";
        Topic topic=new Topic();
        BeanUtils.copyProperties(topicCreatDTO,topic);
        topic.setContent(topicCreatDTO.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save(topic)){
            //删除缓存
            cacheUtil.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE+"*");
            return null;
        }else {
            return "内部错误，请联系管理员";
        }
    }

    /**
     * 获取帖子
     * @param pageNumber
     * @param type
     * @return
     */
    @Override
    public List<TopicPreviewVO> listTopicByPage(int pageNumber, int type) {
        String key=Const.FORUM_TOPIC_PREVIEW_CACHE+pageNumber+":"+type;
        //List<Topic> topics;
        Page<Topic> page=Page.of(pageNumber,10);
        List<TopicPreviewVO> topicPreviewVOS=cacheUtil.takeListFromCache(key, TopicPreviewVO.class);
        if (topicPreviewVOS!=null) return topicPreviewVOS;
        if(type==0)
         baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        else
         baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        List<Topic> topics = page.getRecords();
        if(topics.isEmpty()) return null;
        //将 topics 集合中的每个元素通过 resolveToPreview 方法进行转换，并将转换后的结果收集到一个列表中。
        topicPreviewVOS=topics.stream().map(this::resolveToPreview).toList();
        cacheUtil.saveListToCache(key,topicPreviewVOS,10);
        return topicPreviewVOS;
    }

    /**
     * 通过标题搜索帖子
     * @param title
     * @return
     */
    @Override
    public List<TopicPreviewVO> listTopicByTitle( String title) {
        List<TopicPreviewVO> vo;
        List<Topic> topics =   baseMapper.selectList(Wrappers.<Topic>query().like("title",title).orderByDesc("time"));
        if(topics.isEmpty()) return null;
        //将 topics 集合中的每个元素通过 resolveToPreview 方法进行转换，并将转换后的结果收集到一个列表中。
        vo=topics.stream().map(this::resolveToPreview).toList();
        return vo;
    }

    /**
     * 获取置顶帖子
     * @return
     */
    @Override
    public List<TopicTopVO> listtopTopic() {
        List<Topic> topics=baseMapper.selectList(Wrappers.<Topic>query().select("id","title","time").eq("top",1));
        List<TopicTopVO> list = topics.stream().map(topic -> {
            TopicTopVO vo = new TopicTopVO();
            BeanUtils.copyProperties(topic, vo);
            return vo;
        }).toList();
        return list;
    }

    /**
     * 获取帖子详细信息
     * @param tid
     * @return
     */
    public TopicDetailsVO getTopicDetails(int tid,int uid) {
        TopicDetailsVO vo=new TopicDetailsVO();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic,vo);
        TopicDetailsVO.Interact interact = new TopicDetailsVO.Interact(
                hasInteract(tid, uid, "like"),
                hasInteract(tid, uid, "collect")
        );
        vo.setInteract(interact);
        TopicDetailsVO.User user=new TopicDetailsVO.User();
        vo.setUser(this.fillUserDetailsByPrivacy(user,topic.getUid()));
        vo.setComment(topicCommentMapper.selectCount(Wrappers.<TopicComments>query().eq("tid",tid)));
        return vo;
    }

    /**
     * 判断用户对该条帖子是否进行了点赞和收藏
     * @param tid
     * @param uid
     * @param type
     * @return
     */
    private boolean hasInteract(int tid, int uid, String type) {
        //如果缓存中没有找到，再进行数据库查询(有可能刚点赞，数据没有同步到mysql中)
        String key = tid + ":" + uid;
        if (template.opsForHash().hasKey(type, key))
            return Boolean.parseBoolean(template.opsForHash().entries(type).get(key).toString());
        return baseMapper.userInteractCount(tid, uid, type) > 0;
    }



    /**
     * 获取收藏列表
     * @param uid
     * @return
     */
    @Override
    public List<TopicPreviewVO> getCollectList(int uid) {
//        List<Topic> topics = baseMapper.collectTopics(uid);
//        List<TopicPreviewVO> vos = new ArrayList<>();
//        topics.forEach(topic -> {
//            TopicPreviewVO vo = new TopicPreviewVO();
//            BeanUtils.copyProperties(topic, vo);
//            vos.add(vo);
//        });
//        return vos;
        return baseMapper.collectTopics(uid).stream()
                .map(topic -> {
                    TopicPreviewVO vo =new TopicPreviewVO();
                    BeanUtils.copyProperties(topic,vo);
                    return vo;
                }).toList();
    }

    /**
     * 获取点赞视频
     * @param uid
     * @return
     */
    @Override
    public List<TopicPreviewVO> getLikeList(int uid) {
        return baseMapper.likeTopics(uid).stream()
                .map(topic -> {
                    TopicPreviewVO vo =new TopicPreviewVO();
                    BeanUtils.copyProperties(topic,vo);
                    return vo;
                }).toList();

    }

    /**
     * 修改帖子
     * @param dto
     * @param uid
     */
    public String updateTopic(TopicUpdateDTO dto, int uid) {
        if(!textLimitCheck(dto.getContent(),20000))
            return "文章内容太多！";
        if(!types.contains(dto.getType()))
            return "文章类型非法";
        baseMapper.update(Wrappers.<Topic>update()
                .eq("uid",uid)
                .eq("id",dto.getId())
                .set("title",dto.getTitle())
                .set("content",dto.getContent().toString())
                .set("type",dto.getType()));
        return null;

    }

    /**
     * 发表评论
     * @param dto
     * @param uid
     * @return
     */
    public String addcomment(AddCommentDTO dto, int uid) {
        String key=Const.FORUM_TOPIC_COMMENT_COUNTER+uid;
        if(!textLimitCheck(JSONObject.parseObject(dto.getContent()),2000))
            return "评论内容太多,发送失败！";
        if(!flowUtils.limitPeriodCounterCheck(key,6,60))
            return "评论发表频繁，请稍后重试";
        TopicComments topicComments=new TopicComments();
        topicComments.setUid(uid);
        BeanUtils.copyProperties(dto,topicComments);
        topicComments.setTime(new Date());
        topicCommentMapper.insert(topicComments);
        //获取评论的帖子
        Topic topic=baseMapper.selectById(dto.getTid());
        //获取评论的用户
        Account account=accountMapper.selectById(uid);
        //判断评论是否被回复
        if(dto.getQuote() > 0) {
            //获取被回复的评论
            TopicComments com = topicCommentMapper.selectById(dto.getQuote());
            //判断是否是自己回复自己的评论
            if(!Objects.equals(account.getId(), com.getUid())) {
                //发送消息提醒
                notificationService.addNotification(
                        com.getUid(),
                        "您有新的帖子评论回复",
                        account.getUsername()+" 回复了你发表的评论，快去看看吧！",
                        "success", "/index/topic-detail/"+com.getTid()
                );
            }
        }
        //判断是不是自己评论自己的帖子
        else if (!Objects.equals(account.getId(), topic.getUid())) {
            notificationService.addNotification(
                    topic.getUid(),
                    "您有新的帖子回复",
                    account.getUsername()+" 回复了你发表的帖子: "+topic.getTitle()+"，快去看看吧！",
                    "success", "/index/topic-detail/"+topic.getId()
            );
        }
        return null;
    }

    /**
     * 获取评论
     * @param tid
     * @param page
     * @return
     */
    public List<CommentVO> comments(int tid, int page) {
        Page<TopicComments> pagesize = Page.of(page, 10);
        topicCommentMapper.selectPage(pagesize,Wrappers.<TopicComments>query().eq("tid",tid));
        List<TopicComments> records = pagesize.getRecords();
        List<CommentVO> list = records.stream().map(dto -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(dto, vo);
            if (dto.getQuote() > 0) {
                TopicComments comment = topicCommentMapper.selectOne(Wrappers.<TopicComments>query()
                        .eq("id", dto.getQuote()).orderByAsc("time"));
                //判断评论是否被删除
                if (comment != null) {
                    JSONObject object = JSONObject.parseObject(comment.getContent());
                    StringBuilder builder = new StringBuilder();
                    this.shortContent(object.getJSONArray("ops"), builder, ignore -> {
                    });
                    vo.setQuote(builder.toString());
                } else {
                    vo.setQuote("此评论已被删除");
                }
            }
            CommentVO.User user = new CommentVO.User();
            this.fillUserDetailsByPrivacy(user, dto.getUid());
            vo.setUser(user);
            return vo;
        }).toList();
        return list;
    }

    /**
     * 删除评论
      * @param id
     * @param uid
     */
    public void deleteComment(int id, int uid) {
         topicCommentMapper.delete(Wrappers.<TopicComments>query().eq("id", id).eq("uid", uid));
         //删除帖子下的评论，点赞，收藏

    }

    /**
     * 获取我的帖子
     * @param uid
     * @return
     */
    public List<TopicPreviewVO> getMyTopicList(int uid) {
        return  baseMapper.myTopics(uid).stream()
                .map(topic -> {
                    TopicPreviewVO vo =new TopicPreviewVO();
                    BeanUtils.copyProperties(topic,vo);
                    return vo;
                }).toList();

    }

    /**
     * 删除我的帖子
     * @param id
     * @param uid
     */
    public void deleteMyTopic(int id, int uid) {
        baseMapper.delete(Wrappers.<Topic>query().eq("id",id).eq("uid",uid));
        //删除帖子对应的点赞和收藏
        baseMapper.deleteLike(id);
        baseMapper.deleteCollect(id);
        baseMapper.deleteComment(id);
    }

    /**
     * 点赞收藏
     * 由于论坛交互数据（点赞，收藏等）更新会非常频繁
     * 更新信息实时到 mysql有点不太现实，所以需要redis作缓存，并在合适时间一次性入库一段时间内的全部数据
     * 当数据更新到来时，会创建一个定时任务，此任务会在一段时间后执行
     * 将全部redis暂时缓存的信息一次性加入到数据库，从合缓解 mysql的压力
     * 如果在定时任务已经设定期间又有新的更新到来，仅更新到redis不创建新的延时任务
     *新到达的数据会被安全地存储在 Redis 中，不会因当前定时任务执行时的删除操作而丢失。
     * 在下一次定时任务执行时，这些新数据将被读取、处理并同步到数据库。
     * @param interactDTO
     * @param state
     */
    public void interact(InteractDTO interactDTO, boolean state) {
        String type=interactDTO.getType();
        synchronized (type.intern()){
            //用hash过滤相同的数据，第二次的值覆盖第一次的值
            template.opsForHash().put(type,interactDTO.toKey(),Boolean.toString(state));
            this.saveInteractSchedule(type);
        }
    }
    /**
     * 创建定时任务
     */
    //记录是否开始计时
    private final Map<String, Boolean> state = new HashMap<>();
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private void saveInteractSchedule(String type) {
        System.out.println(state.get(type));
        //表示定时任务开始
        if(!state.getOrDefault(type, false)) {
            //把type放进map，value为true
            state.put(type, true);
            //执行定时任务
            service.schedule(() -> {
                this.saveInteract(type);
                //更新type的value为false
                state.put(type, false);
            }, 3, TimeUnit.SECONDS);
        }
    }
    /**
     * 从redis取出点赞，收藏的数据然后存入mysql
     */
    public void saveInteract(String type){
        synchronized (type.intern()){
            List<InteractDTO> check=new LinkedList<>();
            List<InteractDTO> uncheck=new LinkedList<>();
            //遍历哈希表中的每个键值对 (k, v)：
            template.opsForHash().entries(type).forEach((k,v)->{
                //如果为true，则说明选中
                if(Boolean.parseBoolean(v.toString()))
                    check.add(InteractDTO.parseInteract(k.toString(),type));
                else
                    uncheck.add(InteractDTO.parseInteract(k.toString(),type));
            });
            //把选中的的批量插入mysql
            if(!check.isEmpty())
                baseMapper.addInteract(check,type);
            //把未选中的从mysql中删除
            if(!uncheck.isEmpty())
                baseMapper.deleteInteract(uncheck,type);
            template.delete(type);
        }
    }

    /**
     * 返回个人隐私（不包含已隐藏）
     * @param target
     * @param uid
     * @return
     * @param <T>
     */
    private <T> T fillUserDetailsByPrivacy(T target,int uid){
        AccountDetails accountDetails = accountDetailsMapper.selectById(uid);
        Account account = accountMapper.selectById(uid);
        AccountPrivacy privacy = accountPrivacyMapper.selectById(uid);
        //获取隐私中为false的字段，代表隐藏，忽略
        String[] ignores = privacy.hiddenFields();
        BeanUtils.copyProperties(account,target,ignores);
        BeanUtils.copyProperties(accountDetails,target,ignores);
        return target;
    }
    /**
     * 构造TopicPreview
     * @param topic
     * @return
     */
    private TopicPreviewVO resolveToPreview(Topic topic) {
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()),vo);
        BeanUtils.copyProperties(topic, vo);
        vo.setLike(baseMapper.interactCount(topic.getId(),"like"));
        vo.setCollect(baseMapper.interactCount(topic.getId(),"collect"));
        vo.setComments(baseMapper.comments(topic.getId()));
        List<String> images = new ArrayList<>();
        //不考虑线程安全用StringBuilder
        StringBuilder previewText = new StringBuilder();
        //提取出键为"ops"的JSON数组
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        this.shortContent(ops,previewText,obj->images.add(obj.toString()));
//        for (Object op : ops) {
//            //每个元素转换为JSONObject，然后获取这个新JSONObject的"insert"键对应的值
//            Object insert = JSONObject.from(op).get("insert");
//            if(insert instanceof String text){
//                //超过了 300，那么继续下一次循环。
//                if(previewText.length()>300) continue;
//                previewText.append(text);
//            } else if (insert instanceof Map<?,?> map) {
//                Optional.ofNullable(map.get("image")).ifPresent(obj->images.add(obj.toString()));
//            }
//        }
        vo.setText(previewText.length() > 300 ? previewText.substring(0, 300) : previewText.toString());
        vo.setImages(images);
        return vo;
    }
    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandler){
        for (Object op : ops) {
            Object insert = JSONObject.from(op).get("insert");
            if(insert instanceof String text) {
                if(previewText.length() >= 300) continue;
                previewText.append(text);
            } else if(insert instanceof Map<?, ?> map) {
                Optional.ofNullable(map.get("image")).ifPresent(imageHandler);
            }
        }
    }
    /**
     * 字数检测
     * @param object
     * @param max
     * @return
     */
    private boolean textLimitCheck(JSONObject object, int max) {
        if(object == null) return false;
        long length = 0;
        for (Object op : object.getJSONArray("ops")) {
            length += JSONObject.from(op).getString("insert").length();
            if(length > max) return false;
        }
        return true;
    }
}
