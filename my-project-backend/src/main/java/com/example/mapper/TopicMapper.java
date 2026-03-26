package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Topic;
import com.example.entity.TopicA;
import com.example.entity.dto.InteractDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 帖子mapper
 */
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
//    @Select("""
//             select * from db_topic left join db_account on uid=db_account.id
//             order by time desc limit ${start}, 10
//             """)
//    List<TopicA> topicList(int start);
//    @Select("select * from db_topic join db_account on uid=db_account.id where" +
//            " type=#{type} order by time desc limit ${start}, 10")
//    List<TopicA> topicListByType(int start,int type);

    @Insert("""
            <script>
                insert ignore into db_topic_interact_${type} values
                <foreach collection ="interacts" item="item" separator =",">
                    (#{item.tid}, #{item.uid}, #{item.time})
                </foreach>
            </script>
            """)
    void addInteract(List<InteractDTO> interacts, String type);
        @Delete("""
                <script>
                    delete from db_topic_interact_${type} where
                    <foreach collection="interacts" item="item" separator=" or ">
                        (tid = #{item.tid} and uid = #{item.uid})
                    </foreach>
                </script>
                """)
        int deleteInteract(List<InteractDTO> interacts, String type);

    /**
     * 获取点赞数量
     * @param tid
     * @param type
     * @return
     */
    @Select("""
            select count(*) from db_topic_interact_${type} where tid = #{tid}
            """)
    int interactCount(int tid, String type);

    /**
     * 获取评论数量
     * @param tid
     * @return
     */
    @Select("""
            select count(*) from db_topic_comment where tid=#{tid}
             """)
    int comments(int tid);
    /**
     * 判断当前用户是否点赞
     * @param tid
     * @param uid
     * @param type
     * @return
     */
    @Select("""
            select count(*) from db_topic_interact_${type} where tid = #{tid} and uid = #{uid}
            """)
    int userInteractCount(int tid, int uid, String type);

    /**
     * 获取收藏列表
     * @param uid
     * @return
     */
    @Select("""
            select * from db_topic_interact_collect left join db_topic on tid = db_topic.id
             where db_topic_interact_collect.uid = #{uid}
            """)
    List<Topic> collectTopics(int uid);

    /**
     * 点赞的视频
     * @param uid
     * @return
     */
    @Select("""
            select * from db_topic_interact_like left join db_topic on tid = db_topic.id
             where db_topic_interact_like.uid = #{uid}
            """)
    List<Topic> likeTopics(int uid);

    /**
     * 获取我的帖子1
     * @param uid
     * @return
     */
    @Select("""
                select * from db_topic where uid=#{uid}
             """)
    List<Topic> myTopics(int uid);

    /**
     * 删除删除帖子后对应的点赞
     * @param id
     */
    @Delete("""
                delete from db_topic_interact_like where tid=#{id}
                """)
    void deleteLike(int id);
    /**
     * 删除删除帖子后对应的收藏
     * @param id
     */
    @Delete("""
                delete from db_topic_interact_collect where tid=#{id}
                """)
    void deleteCollect(int id);
    /**
     * 删除删除帖子后对应的评论
     * @param id
     */
    @Delete("""
                delete from db_topic_comment where tid=#{id}
                """)
    void deleteComment(int id);
}
